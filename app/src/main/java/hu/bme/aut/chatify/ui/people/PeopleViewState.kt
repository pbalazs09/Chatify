package hu.bme.aut.chatify.ui.people

sealed class PeopleViewState

object Initialize : PeopleViewState()

object Loading : PeopleViewState()

data class PeopleReady(val response: String) : PeopleViewState()

data class NetworkError(val response: String) : PeopleViewState()