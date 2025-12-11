package np.com.bimalkafle.easybot.view

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import np.com.bimalkafle.easybot.AppUtil
import np.com.bimalkafle.easybot.ui.theme.Purple80
import np.com.bimalkafle.easybot.viewModel.BluetoothViewModel
import np.com.bimalkafle.easybot.viewModel.ChatViewModel

@Composable
fun BluetoothDeviceListScreen(
    bluetoothViewModel: BluetoothViewModel,
    chatViewModel: ChatViewModel,
    messageToSend: String,
    onMessageSent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val devices by bluetoothViewModel.devices.collectAsState()
    val connectionStatus by bluetoothViewModel.connectionStatus.collectAsState()
    val isScanning by bluetoothViewModel.isScanning.collectAsState()
    val isPermissionGranted by bluetoothViewModel.isPermissionGranted.collectAsState()

    // Permission launcher
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.BLUETOOTH_SCAN] == true &&
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                permissions[Manifest.permission.BLUETOOTH_CONNECT] == true

        bluetoothViewModel.updatePermissionStatus(granted)

        if (granted) {
            bluetoothViewModel.startDiscoverySafe()
            chatViewModel.startBluetoothServer()
        } else {
            Toast.makeText(context, "Bluetooth/Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Check permission on launch
    LaunchedEffect(Unit) {
        val scanPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN)
        val locationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val connectPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)

        val granted = scanPermission == PackageManager.PERMISSION_GRANTED &&
                locationPermission == PackageManager.PERMISSION_GRANTED &&
                connectPermission == PackageManager.PERMISSION_GRANTED

        bluetoothViewModel.updatePermissionStatus(granted)

        if (granted) {
            bluetoothViewModel.startDiscoverySafe()
            // Start Bluetooth server to receive messages
            chatViewModel.startBluetoothServer()
        } else {
            launcher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        }
    }

    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isScanning)

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            if (isPermissionGranted) {
                bluetoothViewModel.startDiscoverySafe()
            } else {
                Toast.makeText(context, "Bluetooth or Location permission not granted", Toast.LENGTH_SHORT).show()
            }
        },
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .offset(y = (-49).dp)
                    .border(
                        width = 1.7.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Purple80.copy(alpha = 0.5f),
                                Color.Gray.copy(alpha = 0.15f)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .fillMaxWidth()
                    .height(175.dp)
                    .clip(RoundedCornerShape(bottomEnd = 22.dp, bottomStart = 22.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Purple80.copy(alpha = 0.25f),
                                Color.White.copy(alpha = 0.05f)
                            ),
                            start = Offset(0f, .05f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    ),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    text = "Select Device",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 33.sp,
                    fontWeight = FontWeight.W800,
                    color = Purple80
                )
            }
            if (devices.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isScanning) "Scanning for devices..." else "No nearby devices found",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(-6.dp),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {                    items(devices, key = { it.address }) { device ->
                        val connectedDevice = chatViewModel.connectedDevice
                        DeviceCard(
                            device = device,
                            chatViewModel = chatViewModel,
                            message = messageToSend,
                            onMessageSent = onMessageSent,
                            isConnected = device.address == connectedDevice?.address
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = connectionStatus,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun DeviceCard(
    device: BluetoothDevice,
    chatViewModel: ChatViewModel,
    message: String,
    onMessageSent: () -> Unit,
    isConnected: Boolean
) {
    val context = LocalContext.current
    val hasPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.BLUETOOTH_CONNECT
    ) == PackageManager.PERMISSION_GRANTED

    val deviceName = if (hasPermission) device.name ?: "Unknown Device" else "Permission required"
    val deviceAddress = if (hasPermission) device.address ?: "Unknown Address" else "Permission required"

    var bondState by remember { mutableStateOf(if (hasPermission) device.bondState else BluetoothDevice.BOND_NONE) }
    var isPaired by remember { mutableStateOf(if (hasPermission) device.bondState == BluetoothDevice.BOND_BONDED else false) }

    // Monitor bond state changes to auto-send message after successful pairing
    LaunchedEffect(device) {
        if (!hasPermission) return@LaunchedEffect

        // Poll bond state to detect when pairing completes
        while (true) {
            delay(500)
            val currentBondState = device.bondState

            if (currentBondState != bondState) {
                bondState = currentBondState
                isPaired = currentBondState == BluetoothDevice.BOND_BONDED

                // Auto-send message when pairing succeeds
                if (currentBondState == BluetoothDevice.BOND_BONDED && message.isNotEmpty()) {
                    delay(1000) // Small delay to ensure connection is stable
                    chatViewModel.connectToDevice(device)
                    chatViewModel.sendMessageToBluetooth(message)
                    AppUtil.showToast(context, "Message sent to ${deviceName}")
                    onMessageSent()
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .clip(RoundedCornerShape(28.dp))
            .border(1.7.dp, Color.Gray.copy(0.2f), RoundedCornerShape(28.dp))
            .fillMaxWidth()
            .padding(5.dp),
        colors = CardDefaults.cardColors(Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(deviceName, style = MaterialTheme.typography.bodyLarge)
                Text(deviceAddress, style = MaterialTheme.typography.bodySmall)
                Text(
                    text = when (bondState) {
                        BluetoothDevice.BOND_BONDED -> "Paired"
                        BluetoothDevice.BOND_BONDING -> "Pairing..."
                        else -> "Not Paired"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (bondState == BluetoothDevice.BOND_BONDED) Color.Green else Color.Red
                )
            }

            Switch(
                checked = isPaired,
                onCheckedChange = { checked ->
                    if (!hasPermission) {
                        Toast.makeText(context, "Bluetooth permission required", Toast.LENGTH_SHORT).show()
                        return@Switch
                    }
                    try {
                        if (checked) {
                            if (device.bondState != BluetoothDevice.BOND_BONDED) {
                                // Start pairing - message will be sent automatically via LaunchedEffect
                                val method = device.javaClass.getMethod("createBond")
                                method.invoke(device)
                                Toast.makeText(context, "Pairing started...", Toast.LENGTH_SHORT).show()
                                isPaired = true
                            } else {
                                // Already paired -> connect and send message immediately
                                if (message.isNotEmpty()) {
                                    chatViewModel.connectToDevice(device)
                                    chatViewModel.sendMessageToBluetooth(message)
                                    AppUtil.showToast(context, "Message sent to ${deviceName}")
                                    onMessageSent()
                                } else {
                                    AppUtil.showToast(context, "No message to send")
                                }
                            }
                        } else {
                            // Unpair device
                            val method = device.javaClass.getMethod("removeBond")
                            method.invoke(device)
                            Toast.makeText(context, "Device unpaired", Toast.LENGTH_SHORT).show()
                            isPaired = false
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Purple80,
                    checkedTrackColor = Purple80.copy(alpha = 0.2f)
                )
            )
        }
    }
}
