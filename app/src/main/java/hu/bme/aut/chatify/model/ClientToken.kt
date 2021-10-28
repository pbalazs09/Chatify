package hu.bme.aut.chatify.model

data class ClientToken (
    val tokens: HashMap<String, Boolean> = hashMapOf()
)