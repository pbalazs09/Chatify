package hu.bme.aut.chatify

import android.os.Bundle
import co.zsmb.rainbowcake.navigation.SimpleNavActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import hu.bme.aut.chatify.navigation.BottomNavigationFragment
import hu.bme.aut.chatify.ui.login.LoginFragment
import hu.bme.aut.chatify.ui.main.MainFragment
import hu.bme.aut.chatify.ui.people.PeopleFragment

class NavigationActivity : SimpleNavActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val auth = FirebaseAuth.getInstance()
        if(auth.currentUser != null){
            navigator.setStack(BottomNavigationFragment())
        }
        else{
            navigator.setStack(LoginFragment())
        }
        //val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        //bottomNav.setOnNavigationItemSelectedListener(navListener)
    }
    private val navListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> navigator.add(MainFragment())
                R.id.nav_people -> navigator.add(PeopleFragment())
            }
            true
        }
}