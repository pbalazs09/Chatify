package hu.bme.aut.chatify.ui.login

import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import co.zsmb.rainbowcake.navigation.navigator
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import hu.bme.aut.chatify.R
import hu.bme.aut.chatify.model.User
import hu.bme.aut.chatify.ui.main.MainFragment
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoginViewModel @Inject constructor(

) : RainbowCakeViewModel<LoginViewState>(Initialize) {
    fun init() = execute {
        viewState = Initialize
    }

    fun signIn(email: String, password: String) = viewModelScope.launch{
        viewState = Loading
        val auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            viewState = LoginReady("Successful login")
        }.addOnFailureListener {
            viewState = NetworkError(it.message.toString())
        }
    }

    fun googleAuth(account: GoogleSignInAccount, requireActivity: FragmentActivity) {
        viewState = Loading
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        Firebase.auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    //val users = Firebase.database.reference.child("Users")
                    val database = Firebase.firestore
                    database.collection("Users").document(Firebase.auth.currentUser?.uid.toString()).get().addOnSuccessListener {
                        if(it.data == null){
                            val currentUser = Firebase.auth.currentUser
                            database.collection("Users").document(Firebase.auth.currentUser?.uid.toString()).set(
                                //User(currentUser?.uid.toString(), currentUser?.displayName.toString(), currentUser?.photoUrl.toString())
                                hashMapOf(
                                        "id" to currentUser?.uid.toString(),
                                        "name" to currentUser?.displayName.toString(),
                                        "photoUrl" to currentUser?.photoUrl.toString()
                                )
                            ).addOnSuccessListener {
                                viewState = LoginReady("Successful login")
                            }.addOnFailureListener {
                                viewState = NetworkError("Network error")
                            }
                        }
                        else{
                            viewState = LoginReady("Successful login")
                        }
                    }.addOnFailureListener {
                        viewState = NetworkError("Network error")
                    }
                } else {
                    viewState = NetworkError("Network error")
                }
            }.addOnFailureListener {
                viewState = NetworkError(it.message.toString())
            }
    }

    fun facebookAuth(token: AccessToken, requireActivity: FragmentActivity) {
        viewState = Loading
        val credential = FacebookAuthProvider.getCredential(token.token)
        Firebase.auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val database = Firebase.firestore
                    database.collection("Users").document(Firebase.auth.currentUser?.uid.toString()).get().addOnSuccessListener {
                        if(it.data == null){
                            val currentUser = Firebase.auth.currentUser
                            database.collection("Users").document(Firebase.auth.currentUser?.uid.toString()).set(User(currentUser?.uid.toString(), currentUser?.displayName.toString(), currentUser?.photoUrl.toString())).addOnSuccessListener {
                                viewState = LoginReady("Success")
                            }.addOnFailureListener {
                                viewState = NetworkError("Error")
                            }
                        }
                        else{
                            viewState = LoginReady("Success")
                        }
                    }.addOnFailureListener {
                        viewState = NetworkError("Error")
                    }
                    viewState = LoginReady("Success")
                } else {
                    // If sign in fails, display a message to the user.
                    viewState = NetworkError("Error")
                }
            }
    }
}