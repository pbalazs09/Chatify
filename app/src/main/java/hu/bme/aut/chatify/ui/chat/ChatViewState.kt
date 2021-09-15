package hu.bme.aut.chatify.ui.chat

sealed class ChatViewState

object Initialize : ChatViewState()

object Loading : ChatViewState()