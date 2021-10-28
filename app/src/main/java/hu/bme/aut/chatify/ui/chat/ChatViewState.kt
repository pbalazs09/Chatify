package hu.bme.aut.chatify.ui.chat

sealed class ChatViewState

object Initialize : ChatViewState()

object Loading : ChatViewState()

data class ChatReady(val response: String) : ChatViewState()

data class MessageSent(val response: String) : ChatViewState()

data class ImageSent(val response: String) : ChatViewState()

data class ThemeApplied(val response: String) : ChatViewState()

data class InitChat(val response: String) : ChatViewState()

data class NetworkError(val response: String) : ChatViewState()