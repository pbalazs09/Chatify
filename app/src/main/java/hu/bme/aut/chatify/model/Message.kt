package hu.bme.aut.chatify.model

data class Message(
    val sender: String = "",
    val receiver: String = "",
    val message: String = "",
    val date: Long = -1
)