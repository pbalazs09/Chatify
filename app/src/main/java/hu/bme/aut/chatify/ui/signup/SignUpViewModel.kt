package hu.bme.aut.chatify.ui.signup

import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import hu.bme.aut.chatify.model.User
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

class SignUpViewModel @Inject constructor(

) : RainbowCakeViewModel<SignUpViewState>(Initialize) {
    fun init() = execute {
        viewState = Initialize
    }

    fun signUp(name: String, email: String, password: String) = viewModelScope.launch{
        viewState = Loading
        val auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
            it.user?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build())
            val users = Firebase.database.reference.child("Users")
            users.child(it.user?.uid.toString()).setValue(User(it.user?.uid.toString())).addOnSuccessListener {
                viewState = SignUpReady("Successful registration")
            }.addOnFailureListener {
                viewState = NetworkError("Network error")
            }
        }.addOnFailureListener {
            viewState = NetworkError("Network error")
        }
    }
}