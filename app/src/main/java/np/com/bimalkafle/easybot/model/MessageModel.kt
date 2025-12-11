package np.com.bimalkafle.easybot.model

data class MessageModel(
    val id: String = "",
    val message: String = "",
    val role: String = "", // "user" or "model"
    var isFavorite: Boolean = false,
    val userId: String = "", // link message to specific user
    val timestamp: Long = System.currentTimeMillis() // helpful for sorting
)