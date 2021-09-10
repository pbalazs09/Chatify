package hu.bme.aut.chatify.ui.pwreset

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class PasswordResetViewModel @Inject constructor(

) : RainbowCakeViewModel<PasswordResetViewState>(Initialize) {
    fun init() = execute {
        viewState = Initialize
    }

    fun sendEmail(email: String) {
        viewState = Loading
        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnSuccessListener {
            viewState = PasswordResetReady("Password reset email sent to your email address")
        }.addOnFailureListener {
            viewState = NetworkError(it.message.toString())
        }
    }
}