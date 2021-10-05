package hu.bme.aut.chatify.ui.chat

sealed class ChatViewState

object Initialize : ChatViewState()

object Loading : ChatViewState()

data class ChatReady(val response: String) : ChatViewState()

data class InitChat(val response: String) : ChatViewState()

data class NetworkError(val response: String) : ChatViewState()