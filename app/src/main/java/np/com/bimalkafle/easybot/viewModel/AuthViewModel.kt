package np.com.bimalkafle.easybot.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthState(
    val isLoggedIn: Boolean = false,
    val error: String? = null
)

class AuthViewModel() : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    /** Returns current logged-in userId, null if logged out */
    fun currentUserId(): String? = auth.currentUser?.uid

    /** 🔹 Sign Up */
    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val userId = result.user?.uid ?: throw Exception("User ID is null")

                firestore.collection("users").document(userId)
                    .set(mapOf("email" to email, "createdAt" to System.currentTimeMillis()))
                    .await()

                // Ensure user is logged in
                _authState.value = AuthState(isLoggedIn = true)
            } catch (e: Exception) {
                _authState.value = AuthState(isLoggedIn = false, error = e.message)
            }
        }
    }

    /** 🔹 Login */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    _authState.value = AuthState(isLoggedIn = true)
                }
                .addOnFailureListener { e ->
                    _authState.value = AuthState(isLoggedIn = false, error = e.message)
                }
        }
    }

    /** 🔹 Logout */
    fun logout() {
        auth.signOut()
        _authState.value = AuthState(isLoggedIn = false)
    }
}
