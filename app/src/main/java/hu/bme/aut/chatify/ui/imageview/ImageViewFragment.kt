package hu.bme.aut.chatify.ui.imageview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.navigation.navigator
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import hu.bme.aut.chatify.R
import hu.bme.aut.chatify.databinding.FragmentImageviewBinding

class ImageViewFragment(private val imageName: String) : RainbowCakeFragment<ImageViewViewState, ImageViewViewModel>() {

    private lateinit var binding: FragmentImageviewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImageviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Firebase.storage.getReference("Images").child(imageName).downloadUrl.addOnSuccessListener {
            Picasso.get().load(it).into(binding.tivImage)
        }
    }

    override fun getViewResource(): Int = R.layout.fragment_imageview

    override fun provideViewModel(): ImageViewViewModel = getViewModelFromFactory()

    override fun render(viewState: ImageViewViewState) {
        when (viewState) {
            Initialize -> {

            }

            Loading -> {

            }
        }.exhaustive
    }
}