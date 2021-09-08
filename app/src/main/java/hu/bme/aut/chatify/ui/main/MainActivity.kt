package hu.bme.aut.chatify.ui.main

import co.zsmb.rainbowcake.base.RainbowCakeActivity
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory

class MainActivity : RainbowCakeActivity<MainViewState, MainViewModel>() {

    override fun provideViewModel(): MainViewModel = getViewModelFromFactory()

    override fun render(viewState: MainViewState) {

    }
}