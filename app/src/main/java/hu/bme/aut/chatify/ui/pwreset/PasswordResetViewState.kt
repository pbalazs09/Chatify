package hu.bme.aut.chatify.ui.pwreset

sealed class PasswordResetViewState

object Initialize : PasswordResetViewState()

object Loading : PasswordResetViewState()

data class PasswordResetReady(val response: String) : PasswordResetViewState()

data class NetworkError(val response: String) : PasswordResetViewState()