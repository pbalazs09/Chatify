package hu.bme.aut.chatify.navigation

sealed class BottomNavigationViewState

object Initialize : BottomNavigationViewState()

object Loading : BottomNavigationViewState()