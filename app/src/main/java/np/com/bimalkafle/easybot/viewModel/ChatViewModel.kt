package np.com.bimalkafle.easybot.viewModel

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import np.com.bimalkafle.easybot.Constants
import np.com.bimalkafle.easybot.model.MessageModel
import np.com.bimalkafle.easybot.model.bluetooth.BluetoothClient
import np.com.bimalkafle.easybot.model.bluetooth.BluetoothServer
import java.util.UUID

class ChatViewModel(
    private val authViewModel: AuthViewModel,
    private val appContext: Context
) : ViewModel() {

    private val firestore: FirebaseFirestore = Firebase.firestore
    private val bluetoothClient = BluetoothClient()
    private var bluetoothServer: BluetoothServer? = null

    // 🔹 AI Chat messages
    private val _messageList = MutableStateFlow<List<MessageModel>>(emptyList())
    val messageList: StateFlow<List<MessageModel>> = _messageList

    // 🔹 Bluetooth Chat messages (Unified)
    private val _bluetoothMessageList = MutableStateFlow<List<MessageModel>>(emptyList())
    val bluetoothMessageList: StateFlow<List<MessageModel>> = _bluetoothMessageList

    // 🔹 Favorites (persistent)
    private val _favoriteList = MutableStateFlow<List<MessageModel>>(emptyList())
    val favoriteList: StateFlow<List<MessageModel>> = _favoriteList

    private val _bluetoothStatus = MutableStateFlow("Not connected")
    val bluetoothStatus: StateFlow<String> = _bluetoothStatus

    private val _bluetoothError = MutableStateFlow<String?>(null)
    val bluetoothError: StateFlow<String?> = _bluetoothError

    // Track deleted favorites while offline
    private val deletedFavoriteIds = mutableSetOf<String>()

    val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash-latest",
        apiKey = Constants.apiKey
    )
    private val chat = generativeModel.startChat()

    // Track connected device
    var connectedDevice by mutableStateOf<BluetoothDevice?>(null)
        private set

    init {
        // Enable Firestore offline persistence
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        _messageList.value = emptyList()
        authViewModel.currentUserId()?.let { userId ->
            // Set up listeners for all data sources
            listenForFavorites(userId)
            listenForAIChatMessages(userId)
            listenForBluetoothChatMessages(userId)
        }
    }

    /** Utility: check if device is online */
    private fun isOnline(): Boolean {
        return try {
            val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork
            val capabilities = cm.getNetworkCapabilities(network)
            capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    )
        } catch (e: Exception) {
            false
        }
    }

    // ================== 🔹 GENERATIVE AI CHAT ==================
    fun sendMessage(question: String) {
        val currentUserId = authViewModel.currentUserId() ?: return
        viewModelScope.launch {
            try {
                val userMsg = MessageModel(
                    id = UUID.randomUUID().toString(),
                    message = question,
                    role = "user",
                    isFavorite = false,
                    userId = currentUserId,
                    timestamp = System.currentTimeMillis()
                )
                // Optimistically update UI
                _messageList.value = _messageList.value + userMsg

                // Save to Firestore
                firestore.collection("users").document(currentUserId)
                    .collection("messages").document(userMsg.id)
                    .set(userMsg)

                val typingMsg = MessageModel(
                    id = UUID.randomUUID().toString(),
                    message = "Typing....",
                    role = "model",
                    isFavorite = false,
                    userId = currentUserId,
                    timestamp = System.currentTimeMillis()
                )
                // Optimistically update UI
                _messageList.value = _messageList.value.filterNot { it.role == "model" && it.message == "Typing...." } + typingMsg

                val response = chat.sendMessage(question)

                val modelMsg = MessageModel(
                    id = response.text?.hashCode().toString(),
                    message = response.text.orEmpty(),
                    role = "model",
                    isFavorite = _favoriteList.value.any { it.id == response.text?.hashCode().toString() },
                    userId = currentUserId,
                    timestamp = System.currentTimeMillis()
                )

                // Update UI with final response
                _messageList.value = _messageList.value.filterNot { it.id == typingMsg.id } + modelMsg

                // Save model response to Firestore
                firestore.collection("users").document(currentUserId)
                    .collection("messages").document(modelMsg.id)
                    .set(modelMsg)

            } catch (e: Exception) {
                val errorMsg = MessageModel(
                    id = UUID.randomUUID().toString(),
                    message = "Error: ${e.message}",
                    role = "model",
                    isFavorite = false,
                    userId = authViewModel.currentUserId() ?: "",
                    timestamp = System.currentTimeMillis()
                )
                _messageList.value = _messageList.value.filterNot { it.role == "model" && it.message == "Typing...." } + errorMsg
            }
        }
    }

    // ================== 🔹 BLUETOOTH CHAT ==================

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToDevice(device: BluetoothDevice) {
        // TODO: Implement the connection logic in your BluetoothClient
        // This should establish a persistent socket connection.
        // bluetoothClient.connect(device) { message ->
        //     onBluetoothMessageReceived(message)
        // }
        connectedDevice = device
        _bluetoothStatus.value = "Connected to ${device.name}"
    }

    fun disconnect() {
        // TODO: Implement disconnect logic
        // bluetoothClient.disconnect()
        connectedDevice = null
        _bluetoothStatus.value = "Not connected"
    }

    fun startBluetoothServer() {
        if (bluetoothServer == null) {
            bluetoothServer = BluetoothServer { message ->
                // When a message is received from a client, save it directly.
                onBluetoothMessageReceived(message)
            }
            bluetoothServer?.startServer()
            _bluetoothStatus.value = "Server started, ready to receive messages."
        }
    }

    // Called when a message is received from the connected device or server
    private fun onBluetoothMessageReceived(message: String) {
        val currentUserId = authViewModel.currentUserId() ?: return
        viewModelScope.launch {
            val msg = MessageModel(
                id = UUID.randomUUID().toString(),
                message = message,
                role = "model", // Treat incoming as "model" or "other_user"
                isFavorite = false,
                userId = currentUserId,
                timestamp = System.currentTimeMillis()
            )
            // Save to the unified "bluetooth_chat" collection
            firestore.collection("users").document(currentUserId)
                .collection("bluetooth_chat").document(msg.id)
                .set(msg)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun sendMessageToBluetooth(message: String) {
        val targetDevice = connectedDevice ?: run {
            _bluetoothError.value = "No device connected."
            return
        }
        val currentUserId = authViewModel.currentUserId() ?: return
        viewModelScope.launch {
            try {
                // TODO: Ensure your bluetoothClient.sendMessage uses the active connection
                bluetoothClient.sendMessage(targetDevice, message)
                _bluetoothStatus.value = "Message sent to ${targetDevice.name ?: "Unknown"}"

                // Save sent message to the unified Firestore collection
                val msg = MessageModel(
                    id = UUID.randomUUID().toString(),
                    message = message,
                    role = "user", // "user" is the sender
                    isFavorite = false,
                    userId = currentUserId,
                    timestamp = System.currentTimeMillis()
                )
                firestore.collection("users").document(currentUserId)
                    .collection("bluetooth_chat").document(msg.id)
                    .set(msg)

            } catch (e: Exception) {
                _bluetoothError.value = "Failed to send message: ${e.message}"
                e.printStackTrace()
            }
        }
    }


    // ================== 🔹 FAVORITES ==================
    fun addToFavorites(message: MessageModel) {
        val userId = authViewModel.currentUserId() ?: return
        val userFavoritesRef = firestore.collection("users")
            .document(userId)
            .collection("favorites")
            .document(message.id)

        viewModelScope.launch {
            try {
                val favoritedMessage = message.copy(isFavorite = true)
                if (isOnline()) {
                    userFavoritesRef.set(favoritedMessage).await()
                }
                // Optimistically update lists
                _favoriteList.update { current ->
                    if (current.any { it.id == favoritedMessage.id }) current else current + favoritedMessage
                }
                // Update both AI and Bluetooth lists
                _messageList.update { current ->
                    current.map { if (it.id == message.id) it.copy(isFavorite = true) else it }
                }
                _bluetoothMessageList.update { current ->
                    current.map { if (it.id == message.id) it.copy(isFavorite = true) else it }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteFavorite(message: MessageModel) {
        val userId = authViewModel.currentUserId() ?: return
        val userRef = firestore.collection("users")
            .document(userId)
            .collection("favorites")
            .document(message.id)

        viewModelScope.launch {
            try {
                if (isOnline()) {
                    userRef.delete().await()
                } else {
                    deletedFavoriteIds.add(message.id)
                }

                _favoriteList.update { current -> current.filter { it.id != message.id } }

                // Update both AI and Bluetooth lists
                _messageList.update { current ->
                    current.map { if (it.id == message.id) it.copy(isFavorite = false) else it }
                }
                _bluetoothMessageList.update { current ->
                    current.map { if (it.id == message.id) it.copy(isFavorite = false) else it }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ================== 🔹 FIRESTORE LISTENERS ==================

    private fun listenForFavorites(userId: String) {
        firestore.collection("users")
            .document(userId)
            .collection("favorites")
            .addSnapshotListener { snapshot, _ ->
                val favorites = snapshot?.documents?.mapNotNull { doc ->
                    val msg = doc.toObject(MessageModel::class.java) ?: return@mapNotNull null
                    if (deletedFavoriteIds.contains(msg.id)) return@mapNotNull null
                    msg.copy(isFavorite = true)
                } ?: emptyList()

                _favoriteList.value = favorites
            }
    }

    private fun listenForAIChatMessages(userId: String) {
        firestore.collection("users")
            .document(userId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    val msg = doc.toObject(MessageModel::class.java) ?: return@mapNotNull null
                    msg.copy(isFavorite = _favoriteList.value.any { fav -> fav.id == msg.id })
                } ?: emptyList()

                _messageList.value = messages
            }
    }

    private fun listenForBluetoothChatMessages(userId: String) {
        firestore.collection("users")
            .document(userId)
            .collection("bluetooth_chat") // Listen to the unified collection
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    val msg = doc.toObject(MessageModel::class.java) ?: return@mapNotNull null
                    msg.copy(isFavorite = _favoriteList.value.any { fav -> fav.id == msg.id })
                } ?: emptyList()

                _bluetoothMessageList.value = messages
            }
    }
}
