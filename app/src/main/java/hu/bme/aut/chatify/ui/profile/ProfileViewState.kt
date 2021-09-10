package hu.bme.aut.chatify.ui.profile

sealed class ProfileViewState

object Initialize : ProfileViewState()

object Loading : ProfileViewState()

data class ProfileReady(val response: String) : ProfileViewState()

data class NetworkError(val response: String) : ProfileViewState()