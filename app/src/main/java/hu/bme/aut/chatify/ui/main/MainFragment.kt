package hu.bme.aut.chatify.ui.main

import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import hu.bme.aut.chatify.R

class MainFragment : RainbowCakeFragment<MainViewState, MainViewModel>() {

    override fun getViewResource(): Int = R.layout.fragment_main

    override fun provideViewModel(): MainViewModel = getViewModelFromFactory()

    override fun render(viewState: MainViewState) {
        when (viewState) {
            Initialize -> {

            }

            Loading -> {

            }
            else -> {}
        }.exhaustive
    }
}