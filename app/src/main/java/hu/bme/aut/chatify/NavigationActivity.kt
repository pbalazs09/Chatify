package hu.bme.aut.chatify

import android.os.Bundle
import co.zsmb.rainbowcake.navigation.SimpleNavActivity
import hu.bme.aut.chatify.ui.main.MainFragment

class NavigationActivity : SimpleNavActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigator.add(MainFragment())
    }
}