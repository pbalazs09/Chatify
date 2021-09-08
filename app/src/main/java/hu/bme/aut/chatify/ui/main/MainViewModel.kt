package hu.bme.aut.chatify.ui.main

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import javax.inject.Inject

class MainViewModel @Inject constructor(
) : RainbowCakeViewModel<MainViewState>(Initialize) {
        fun init() = execute {
                viewState = Initialize
        }
}
