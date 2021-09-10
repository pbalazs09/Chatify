package hu.bme.aut.chatify.ui.signup

sealed class SignUpViewState

object Initialize : SignUpViewState()

object Loading : SignUpViewState()

data class SignUpReady(val response: String) : SignUpViewState()

data class NetworkError(val response: String) : SignUpViewState()