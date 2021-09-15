package hu.bme.aut.chatify.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.navigation.navigator
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import hu.bme.aut.chatify.R
import hu.bme.aut.chatify.databinding.FragmentMainBinding
import hu.bme.aut.chatify.ui.login.LoginFragment
import hu.bme.aut.chatify.ui.people.PeopleFragment
import hu.bme.aut.chatify.ui.profile.ProfileFragment

class MainFragment : RainbowCakeFragment<MainViewState, MainViewModel>() {

    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.civProfilePicture.setOnClickListener {
            navigator?.add(ProfileFragment(),
                R.anim.slide_right_new,
                R.anim.slide_right_old,
                R.anim.slide_right_new,
                R.anim.slide_right_old
            )
        }
        val currentUser = Firebase.auth.currentUser
        binding.tvDisplayName.text = currentUser?.displayName
        if(currentUser?.photoUrl != null){
            Picasso.get().load(currentUser.photoUrl).into(binding.civProfilePicture)
        }
    }

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