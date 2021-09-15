package hu.bme.aut.chatify.navigation

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.navigation.navigator
import com.google.android.material.bottomnavigation.BottomNavigationView
import hu.bme.aut.chatify.R
import hu.bme.aut.chatify.ui.main.MainFragment
import hu.bme.aut.chatify.ui.people.PeopleFragment

class BottomNavigationFragment : RainbowCakeFragment<BottomNavigationViewState, BottomNavigationViewModel>() {

    private var lastSelected: Int = R.id.nav_chat

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomNav = view.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navListener =
            BottomNavigationView.OnNavigationItemSelectedListener { item ->
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.setCustomAnimations(
                    R.anim.slide_right_new,
                    R.anim.slide_right_old,
                    R.anim.slide_right_new,
                    R.anim.slide_right_old)
                when (item.itemId) {
                    R.id.nav_chat -> {
                        transaction.replace(R.id.fragment_container, MainFragment())
                    }
                    R.id.nav_people -> {
                        transaction.replace(R.id.fragment_container, PeopleFragment())
                    }
                }
                lastSelected = item.itemId
                transaction.commit()
                true
            }
        bottomNav.setOnNavigationItemSelectedListener(navListener)
    }

    override fun onResume() {
        super.onResume()
        val bottomNav = view?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav?.selectedItemId = lastSelected
    }

    override fun getViewResource(): Int = R.layout.fragment_bottom_navigation

    override fun provideViewModel(): BottomNavigationViewModel = getViewModelFromFactory()

    override fun render(viewState: BottomNavigationViewState) {
        when (viewState) {
            Initialize -> {

            }

            Loading -> {

            }
        }.exhaustive
    }
}