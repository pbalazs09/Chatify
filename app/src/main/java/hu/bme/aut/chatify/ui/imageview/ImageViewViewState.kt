package hu.bme.aut.chatify.ui.imageview

sealed class ImageViewViewState

object Initialize : ImageViewViewState()

object Loading : ImageViewViewState()