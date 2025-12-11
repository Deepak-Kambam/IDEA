package np.com.bimalkafle.easybot.model.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

class BluetoothServer(
    private val onMessageReceived: (String) -> Unit
) {
    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var serverSocket: BluetoothServerSocket? = null
    private var isRunning = false
    private var serverJob: Job? = null

    @SuppressLint("MissingPermission")
    fun startServer() {
        if (isRunning) return

        isRunning = true
        serverJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                serverSocket = adapter?.listenUsingRfcommWithServiceRecord("IdeaApp", uuid)

                while (isRunning) {
                    try {
                        val socket: BluetoothSocket? = serverSocket?.accept()
                        socket?.let {
                            handleClient(it)
                        }
                    } catch (e: IOException) {
                        if (isRunning) {
                            e.printStackTrace()
                        }
                        break
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun handleClient(socket: BluetoothSocket) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val input = socket.inputStream
                val buffer = ByteArray(1024)
                var bytes: Int

                // Read incoming messages
                while (true) {
                    bytes = input.read(buffer)
                    if (bytes > 0) {
                        val message = String(buffer, 0, bytes)
                        onMessageReceived(message)
                    } else {
                        break
                    }
                }
                socket.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun stopServer() {
        isRunning = false
        try {
            serverSocket?.close()
            serverSocket = null
        } catch (e: IOException) {
            e.printStackTrace()
        }
        serverJob?.cancel()
    }
}