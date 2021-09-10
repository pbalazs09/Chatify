package hu.bme.aut.chatify.ui.login

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputLayout
import hu.bme.aut.chatify.R
import hu.bme.aut.chatify.databinding.FragmentLoginBinding
import hu.bme.aut.chatify.ui.main.MainFragment
import hu.bme.aut.chatify.ui.pwreset.PasswordResetFragment
import hu.bme.aut.chatify.ui.signup.SignUpFragment
import kotlinx.android.synthetic.main.fragment_login.*
import java.util.*


class LoginFragment : RainbowCakeFragment<LoginViewState, LoginViewModel>() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    companion object{
        private const val RC_SIGN_IN = 120
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        binding.btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        binding.btnFacebook.setOnClickListener {
            callbackManager = CallbackManager.Factory.create()

            LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile"))
            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult?> {
                    override fun onSuccess(loginResult: LoginResult?) {
                        viewModel.facebookAuth(loginResult!!.accessToken, requireActivity())
                    }
                    override fun onCancel() {
                        // App code
                    }
                    override fun onError(exception: FacebookException) {
                        // App code
                    }
                })
        }

        binding.btnRegister.setOnClickListener {
            navigator?.add(
                SignUpFragment(),
                R.anim.slide_right_new,
                R.anim.slide_right_old,
                R.anim.slide_right_new,
                R.anim.slide_right_old
            )
        }
        binding.btnLogin.setOnClickListener {
            val email = binding.tietEmail.text.toString()
            val password = binding.tietPassword.text.toString()
            if(isEditTextsEmpty()) {
                return@setOnClickListener
            }
            viewModel.signIn(email, password)
        }
        binding.btnForgot.setOnClickListener {
            navigator?.add(
                PasswordResetFragment(),
                R.anim.slide_right_new,
                R.anim.slide_right_old,
                R.anim.slide_right_new,
                R.anim.slide_right_old
            )
        }
        resetEditText(binding.tilEmail)
        resetEditText(binding.tilPassword)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val exception = task.exception
            if (task.isSuccessful) {
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    viewModel.googleAuth(account, requireActivity())
                } catch (e: ApiException) {
                    Log.w("Firebase/GOOGLE", "Sign in failed", e)
                }
            } else {
                Log.w("Firebase/GOOGLE", exception.toString())
            }
        }
        else{
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun resetEditText(til: TextInputLayout){
        til.editText?.addTextChangedListener {
            til.error = ""
            til.defaultHintTextColor = AppCompatResources.getColorStateList(
                requireContext(),
                R.color.app_color
            )
        }
    }

    private fun isEditTextsEmpty(): Boolean {
        if(binding.tietEmail.text!!.isEmpty()) {
            editTextError(binding.tilEmail, getString(R.string.empty_email))
            return true
        }
        else if(binding.tietPassword.text!!.isEmpty()) {
            editTextError(binding.tilPassword, getString(R.string.empty_password))
            return true
        }
        return false
    }

    private fun editTextError(til: TextInputLayout, errorMessage: String) {
        til.error = errorMessage
        til.defaultHintTextColor = AppCompatResources.getColorStateList(
            requireContext(),
            R.color.red
        )
        til.requestFocus()
    }

    override fun getViewResource(): Int = R.layout.fragment_login

    override fun provideViewModel(): LoginViewModel = getViewModelFromFactory()

    override fun render(viewState: LoginViewState) {
        when (viewState) {
            Initialize -> {
                binding.pbLogin.visibility = View.GONE
            }

            Loading -> {
                binding.pbLogin.visibility = View.VISIBLE
                ObjectAnimator.ofInt(binding.pbLogin, "progress", 100)
                    .setDuration(2000)
                    .start()
            }
            is LoginReady -> {
                Toast.makeText(requireContext(), viewState.response, Toast.LENGTH_SHORT).show()
                navigator?.replace(
                    MainFragment(),
                    R.anim.slide_right_new,
                    R.anim.slide_right_old,
                    R.anim.slide_right_new,
                    R.anim.slide_right_old
                )
                viewModel.init()
            }
            is NetworkError -> {
                Toast.makeText(requireContext(), viewState.response, Toast.LENGTH_SHORT).show()
                viewModel.init()
            }
        }.exhaustive
    }
}