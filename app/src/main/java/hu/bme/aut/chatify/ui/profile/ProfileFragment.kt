package hu.bme.aut.chatify.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.navigation.navigator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import hu.bme.aut.chatify.R
import hu.bme.aut.chatify.databinding.FragmentProfileBinding
import hu.bme.aut.chatify.ui.login.LoginFragment
import hu.bme.aut.chatify.ui.signup.SignUpReady

class ProfileFragment : RainbowCakeFragment<ProfileViewState, ProfileViewModel>() {

    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSignOut.setOnClickListener {
            Firebase.auth.signOut()
            navigator?.replace(LoginFragment(), R.anim.slide_right_new, R.anim.slide_right_old, R.anim.slide_right_new, R.anim.slide_right_old)
        }
        if(Firebase.auth.currentUser?.photoUrl != null){
            Picasso.get().load(Firebase.auth.currentUser?.photoUrl).into(binding.civProfilePicture)
        }
    }

    override fun getViewResource(): Int = R.layout.fragment_profile

    override fun provideViewModel(): ProfileViewModel = getViewModelFromFactory()

    override fun render(viewState: ProfileViewState) {
        when (viewState) {
            Initialize -> {

            }

            Loading -> {

            }
            is ProfileReady -> TODO()
            is NetworkError -> TODO()
        }.exhaustive
    }
}