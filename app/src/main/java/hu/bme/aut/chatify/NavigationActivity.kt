package hu.bme.aut.chatify

import android.os.Bundle
import co.zsmb.rainbowcake.navigation.SimpleNavActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import hu.bme.aut.chatify.navigation.BottomNavigationFragment
import hu.bme.aut.chatify.ui.chat.ChatFragment
import hu.bme.aut.chatify.ui.login.LoginFragment
import hu.bme.aut.chatify.ui.main.MainFragment
import hu.bme.aut.chatify.ui.people.PeopleFragment

class NavigationActivity : SimpleNavActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val auth = FirebaseAuth.getInstance()
        if(auth.currentUser != null){
            val sender = intent.data?.pathSegments?.get(0)
            if(sender.isNullOrEmpty()){
                navigator.setStack(BottomNavigationFragment())
            }
            else{
                if(intent.data?.pathSegments?.size!! < 2){
                    navigator.setStack(BottomNavigationFragment(), ChatFragment(sender))
                }
                else{
                    navigator.setStack(BottomNavigationFragment(), ChatFragment(sender, true))
                }
            }
        }
        else{
            navigator.setStack(LoginFragment())
        }
    }
}