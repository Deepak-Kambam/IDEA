package np.com.bimalkafle.easybot.viewModel

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BluetoothViewModel(application: Application) : AndroidViewModel(application) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val _devices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val devices: StateFlow<List<BluetoothDevice>> = _devices

    private val _connectionStatus = MutableStateFlow("Not connected")
    val connectionStatus: StateFlow<String> = _connectionStatus

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private val discoveredDevices = mutableSetOf<BluetoothDevice>()

    private val _isPermissionGranted = MutableStateFlow(false)
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted

    private var isReceiverRegistered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        if (discoveredDevices.add(it)) {
                            _devices.value = discoveredDevices.toList()
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _connectionStatus.value = "Discovery finished"
                    _isScanning.value = false
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        // refresh this device in list
                        discoveredDevices.removeAll { d -> d.address == device.address }
                        discoveredDevices.add(device)
                        _devices.value = discoveredDevices.toList()
                    }
                }
            }
        }
    }

    private fun appContext(): Context = getApplication<Application>().applicationContext

    fun isLocationEnabled(): Boolean {
        val locationManager =
            appContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    /**
     * Enable Bluetooth programmatically
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun enableBluetooth() {
        if (bluetoothAdapter?.isEnabled == false) {
            try {
                bluetoothAdapter.enable()
                Toast.makeText(appContext(), "Bluetooth enabled", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(appContext(), "Failed to enable Bluetooth", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    /**
     * Open Location settings to enable GPS
     */
    fun openLocationSettings() {
        try {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            appContext().startActivity(intent)
            Toast.makeText(appContext(), "Please enable Location", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(appContext(), "Cannot open location settings", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Safe entry point → checks isPermissionGranted before calling discovery
     */
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    fun updatePermissionStatus(granted: Boolean) {
        _isPermissionGranted.value = granted

        // Auto-enable Bluetooth and Location when permissions are granted
        if (granted) {
            enableBluetooth()
            if (!isLocationEnabled()) {
                openLocationSettings()
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startDiscoverySafe() {
        if (_isPermissionGranted.value) {
            startDiscovery()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startDiscovery() {
        if (!isLocationEnabled()) {
            Toast.makeText(
                appContext(),
                "Enable location to scan Bluetooth devices",
                Toast.LENGTH_LONG
            ).show()
            _isScanning.value = false
            return
        }

        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter.cancelDiscovery()
        }

        discoveredDevices.clear()
        _devices.value = emptyList()

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        }

        if (!isReceiverRegistered) {
            try {
                appContext().registerReceiver(receiver, filter)
                isReceiverRegistered = true
            } catch (_: Exception) {
            }
        }

        if (bluetoothAdapter?.startDiscovery() == true) {
            _connectionStatus.value = "Scanning..."
            _isScanning.value = true
        } else {
            _connectionStatus.value = "Failed to start discovery"
            _isScanning.value = false
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
        if (isReceiverRegistered) {
            try {
                appContext().unregisterReceiver(receiver)
            } catch (_: Exception) {
            }
            isReceiverRegistered = false
        }
        _connectionStatus.value = "Discovery stopped"
        _isScanning.value = false
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun onCleared() {
        super.onCleared()
        stopDiscovery()
    }
}
