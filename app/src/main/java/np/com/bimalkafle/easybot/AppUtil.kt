package np.com.bimalkafle.easybot

import android.content.Context
import android.widget.Toast

object AppUtil {
    fun showToast(context: Context, message: String) {
        Toast.makeText(context,message, Toast.LENGTH_SHORT).show()
    }

    fun isOnline(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("ping -c 1 google.com")
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }
}