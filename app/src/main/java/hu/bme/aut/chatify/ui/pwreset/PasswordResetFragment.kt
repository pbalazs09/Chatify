package hu.bme.aut.chatify.ui.pwreset

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.navigation.navigator
import hu.bme.aut.chatify.R
import hu.bme.aut.chatify.databinding.FragmentPasswordresetBinding

class PasswordResetFragment :
    RainbowCakeFragment<PasswordResetViewState, PasswordResetViewModel>() {

    private lateinit var binding: FragmentPasswordresetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPasswordresetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnReset.setOnClickListener {
            viewModel.sendEmail(binding.tietEmail.text.toString())
        }
    }

    override fun getViewResource(): Int = R.layout.fragment_passwordreset

    override fun provideViewModel(): PasswordResetViewModel = getViewModelFromFactory()

    override fun render(viewState: PasswordResetViewState) {
        when (viewState) {
            Initialize -> {
                binding.pbReset.visibility = View.GONE
            }

            Loading -> {
                binding.pbReset.visibility = View.VISIBLE
                ObjectAnimator.ofInt(binding.pbReset, "progress", 100)
                    .setDuration(2000)
                    .start()
            }
            is PasswordResetReady -> {
                Toast.makeText(requireContext(), viewState.response, Toast.LENGTH_SHORT).show()
                navigator?.pop()
                viewModel.init()
            }
            is NetworkError -> {
                Toast.makeText(requireContext(), viewState.response, Toast.LENGTH_SHORT).show()
                viewModel.init()
            }
        }.exhaustive
    }
}