package hu.bme.aut.chatify.ui.main

sealed class MainViewState

object Initialize : MainViewState()

object Loading : MainViewState()

data class NetworkError(val response: String) : MainViewState()