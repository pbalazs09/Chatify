package hu.bme.aut.chatify.ui.profile

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.navigation.navigator
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import hu.bme.aut.chatify.R
import hu.bme.aut.chatify.databinding.FragmentProfileBinding
import hu.bme.aut.chatify.ui.login.LoginFragment

class ProfileFragment : RainbowCakeFragment<ProfileViewState, ProfileViewModel>() {

    private lateinit var binding: FragmentProfileBinding
    private val PICK_IMAGE = 1

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
            AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.signout_question))
                .setPositiveButton(getString(R.string.yes)){ _: DialogInterface, _: Int ->
                    viewModel.signOut()
                    navigator?.setStack(LoginFragment())
                }
                .setNegativeButton(getString(R.string.no),null)
                .show()
        }
        binding.btnDeleteProfile.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.delete_question))
                .setPositiveButton(getString(R.string.yes)){ _: DialogInterface, _: Int ->
                    viewModel.deleteProfile(requireContext())
                }
                .setNegativeButton(getString(R.string.no),null)
                .show()
        }
        binding.btnChangeProfilePicture.setOnClickListener{
            ImagePicker.with(this)
                .crop(1f, 1f)	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(128, 128)	//Final image resolution will be less than 1080 x 1080(Optional)
                .galleryOnly()
                .start()
            //val intent = Intent()
            //intent.type = "image/*"
            //intent.action = Intent.ACTION_GET_CONTENT
            //startActivityForResult(Intent.createChooser(intent, "Choose a photo"), PICK_IMAGE)
        }
        if(Firebase.auth.currentUser?.photoUrl != null){
            Picasso.get().load(Firebase.auth.currentUser?.photoUrl).into(binding.civProfilePicture)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uri = data?.data
        if (uri != null) {
            viewModel.setPhoto(uri, requireContext())
        }
        /*if(requestCode == 1){
            val uri = data?.data
            if (uri != null) {
                viewModel.setPhoto(uri)
            }
        }*/
    }

    override fun getViewResource(): Int = R.layout.fragment_profile

    override fun provideViewModel(): ProfileViewModel = getViewModelFromFactory()

    override fun render(viewState: ProfileViewState) {
        when (viewState) {
            Initialize -> {
                binding.pbProfile.visibility = View.GONE
            }

            Loading -> {
                binding.pbProfile.visibility = View.VISIBLE
                ObjectAnimator.ofInt(binding.pbProfile, "progress", 100)
                        .setDuration(2000)
                        .start()
            }
            is ProfileReady -> {
                Toast.makeText(requireContext(), viewState.response, Toast.LENGTH_SHORT).show()
                navigator?.setStack(LoginFragment())
                viewModel.init()
            }
            is PhotoReady -> {
                Toast.makeText(requireContext(), viewState.response, Toast.LENGTH_SHORT).show()
                if(Firebase.auth.currentUser?.photoUrl != null){
                    Picasso.get().load(Firebase.auth.currentUser?.photoUrl).into(binding.civProfilePicture)
                }
                viewModel.init()
            }
            is NetworkError -> {
                Toast.makeText(requireContext(), viewState.response, Toast.LENGTH_SHORT).show()
                viewModel.init()
            }
        }.exhaustive
    }
}