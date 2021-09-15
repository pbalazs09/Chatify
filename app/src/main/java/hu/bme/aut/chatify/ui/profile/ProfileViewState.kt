package hu.bme.aut.chatify.ui.profile

import android.net.Uri

sealed class ProfileViewState

object Initialize : ProfileViewState()

object Loading : ProfileViewState()

data class ProfileReady(val response: String) : ProfileViewState()

data class PhotoReady(val response: String, val uri: Uri) : ProfileViewState()

data class NetworkError(val response: String) : ProfileViewState()