package hu.bme.aut.chatify

import android.os.Bundle
import co.zsmb.rainbowcake.navigation.SimpleNavActivity
import com.google.firebase.auth.FirebaseAuth
import hu.bme.aut.chatify.ui.login.LoginFragment
import hu.bme.aut.chatify.ui.main.MainFragment

class NavigationActivity : SimpleNavActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val auth = FirebaseAuth.getInstance()
        if(auth.currentUser != null){

        }
        navigator.add(LoginFragment())
    }
}