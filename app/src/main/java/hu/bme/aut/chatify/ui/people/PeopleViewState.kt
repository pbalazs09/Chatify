package hu.bme.aut.chatify.ui.people

sealed class PeopleViewState

object Initialize : PeopleViewState()

object Loading : PeopleViewState()