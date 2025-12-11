package np.com.bimalkafle.easybot

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import np.com.bimalkafle.easybot.ui.theme.EasyBotTheme
import np.com.bimalkafle.easybot.viewModel.AuthViewModel
import np.com.bimalkafle.easybot.viewModel.BluetoothViewModel
import np.com.bimalkafle.easybot.viewModel.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create AuthViewModel first as it's a dependency for ChatViewModel
        val authFactory = AuthViewModelFactory()
        val authViewModel = ViewModelProvider(this, authFactory)[AuthViewModel::class.java]

        // Now create ChatViewModel, passing the AuthViewModel instance
        val chatFactory = ChatViewModelFactory(authViewModel, applicationContext)
        val chatViewModel = ViewModelProvider(this, chatFactory)[ChatViewModel::class.java]

        val bluetoothFactory = BluetoothViewModelFactory(this)
        val bluetoothViewModel = ViewModelProvider(this, bluetoothFactory)[BluetoothViewModel::class.java]


        setContent {
            EasyBotTheme {
                // Check if user is logged in
                val authState by authViewModel.authState.collectAsState()
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                val isLoggedIn = authState.isLoggedIn || firebaseUser != null

                // Handle back button to exit app when logged in (prevent going back to auth)
                BackHandler(enabled = isLoggedIn) {
                    finish() // Exit the app
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavGraph(
                        innerPadding = innerPadding,
                        chatViewModel = chatViewModel,
                        authViewModel = authViewModel,
                        bluetoothViewModel = bluetoothViewModel
                    )
                }
            }
        }
    }
}

// Updated Factory to accept AuthViewModel
class ChatViewModelFactory(
    private val authViewModel: AuthViewModel,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Pass the provided AuthViewModel instance and context
            return ChatViewModel(authViewModel, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class AuthViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class BluetoothViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BluetoothViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BluetoothViewModel(context.applicationContext as Application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
