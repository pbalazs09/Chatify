package hu.bme.aut.chatify.model

data class Conversation(
    val id: String,
    val participants: MutableList<User> = mutableListOf(),
    val messages: MutableList<Message> = mutableListOf()
)