package hu.bme.aut.chatify.ui.login

sealed class LoginViewState

object Initialize : LoginViewState()

object Loading : LoginViewState()

data class LoginReady(val response: String) : LoginViewState()

data class NetworkError(val response: String) : LoginViewState()