package hu.bme.aut.chatify.model

data class Conversation(
    val id: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = -1,
    val chatColor: String = "",
    val participants: HashMap<String, Boolean> = hashMapOf()
)