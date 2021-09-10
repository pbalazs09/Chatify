package hu.bme.aut.chatify.ui.signup

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.addTextChangedListener
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.navigation.navigator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import hu.bme.aut.chatify.R
import hu.bme.aut.chatify.databinding.FragmentSignupBinding

class SignUpFragment : RainbowCakeFragment<SignUpViewState, SignUpViewModel>() {

    private lateinit var binding: FragmentSignupBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSignUp.setOnClickListener {
            val name = binding.tietName.text.toString()
            val email = binding.tietEmail.text.toString()
            val password = binding.tietPassword.text.toString()
            if (isEditTextsEmpty()){
                return@setOnClickListener
            }
            if(!isEmailAndPasswordMatch()){
                return@setOnClickListener
            }
            viewModel.signUp(name, email, password)
        }
        resetEditText(binding.tilName)
        resetEditText(binding.tilEmail)
        resetEditText(binding.tilConfirmEmail)
        resetEditText(binding.tilPassword)
        resetEditText(binding.tilConfirmPassword)
    }

    private fun resetEditText(til: TextInputLayout){
        til.editText?.addTextChangedListener {
            til.error = ""
            til.defaultHintTextColor = AppCompatResources.getColorStateList(requireContext(), R.color.app_color)
        }
    }

    private fun isEmailAndPasswordMatch(): Boolean {
        return when {
            binding.tietEmail.text.toString() != binding.tietConfirmEmail.text.toString() -> {
                editTextError(binding.tilConfirmEmail, getString(R.string.emails_not_matches))
                false
            }
            binding.tietPassword.text.toString() != binding.tietConfirmPassword.text.toString() -> {
                editTextError(binding.tilConfirmPassword, getString(R.string.passwords_not_matches))
                false
            }
            else -> true
        }
    }

    private fun isEditTextsEmpty(): Boolean {
        return when {
            binding.tietName.text!!.isEmpty() -> {
                editTextError(binding.tilName, getString(R.string.empty_name))
                true
            }
            binding.tietEmail.text!!.isEmpty() -> {
                editTextError(binding.tilEmail, getString(R.string.empty_email))
                true
            }
            binding.tietPassword.text!!.isEmpty() -> {
                editTextError(binding.tilPassword, getString(R.string.empty_password))
                true
            }
            else -> false
        }
    }

    private fun editTextError(til: TextInputLayout, errorMessage: String) {
        til.error = errorMessage
        til.defaultHintTextColor = AppCompatResources.getColorStateList(requireContext(), R.color.red)
        til.requestFocus()
    }

    override fun getViewResource(): Int = R.layout.fragment_signup

    override fun provideViewModel(): SignUpViewModel = getViewModelFromFactory()

    override fun render(viewState: SignUpViewState) {
        when (viewState) {
            Initialize -> {
                binding.pbSignUp.visibility = View.GONE
            }

            Loading -> {
                binding.pbSignUp.visibility = View.VISIBLE
                ObjectAnimator.ofInt(binding.pbSignUp, "progress", 100)
                    .setDuration(2000)
                    .start()
            }
            is SignUpReady -> {
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