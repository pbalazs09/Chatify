package hu.bme.aut.chatify.model

data class Message(
    val sender: String = "",
    val message: String = "",
    val date: Long = -1,
    val image: Boolean = false,
    val imageName: String = ""
)