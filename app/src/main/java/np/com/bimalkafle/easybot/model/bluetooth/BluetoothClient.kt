package np.com.bimalkafle.easybot.model.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

class BluetoothClient {

    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // 🔹 Callback for incoming messages
    var onMessageReceived: ((BluetoothDevice, String) -> Unit)? = null

    @SuppressLint("MissingPermission")
    fun sendMessage(device: BluetoothDevice, message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val socket: BluetoothSocket =
                    device.createRfcommSocketToServiceRecord(uuid)

                adapter?.cancelDiscovery()

                socket.use { s ->
                    s.connect()
                    val output = s.outputStream
                    output.write(message.toByteArray())
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // 🔹 New: Listen for incoming messages
    @SuppressLint("MissingPermission")
    fun listenForMessages(device: BluetoothDevice) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val socket: BluetoothSocket =
                    device.createRfcommSocketToServiceRecord(uuid)

                adapter?.cancelDiscovery()

                socket.connect()
                val inputStream = socket.inputStream
                val buffer = ByteArray(1024)
                var bytes: Int

                while (true) {
                    bytes = inputStream.read(buffer)
                    val message = String(buffer, 0, bytes)
                    // 🔹 Trigger callback in ChatViewModel
                    onMessageReceived?.invoke(device, message)
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}