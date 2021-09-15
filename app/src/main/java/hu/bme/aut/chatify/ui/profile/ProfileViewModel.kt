package hu.bme.aut.chatify.ui.profile

import android.net.Uri
import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProfileViewModel @Inject constructor(

) : RainbowCakeViewModel<ProfileViewState>(Initialize) {
    fun init() = execute {
        viewState = Initialize
    }

    fun deleteProfile() = viewModelScope.launch {
        viewState = Loading
        val currentUser = Firebase.auth.currentUser
        if(currentUser != null){
            //val reference = Firebase.database.getReference("Users")
            //reference.child(currentUser.uid).removeValue()
            val users = Firebase.firestore.collection("Users")
            users.document(currentUser.uid).delete()
            currentUser.delete()
            viewState = ProfileReady("Profile deleted")
        }
        else{
            viewState = NetworkError("Error")
        }
    }

    fun signOut() {
        Firebase.auth.signOut()
        viewState = ProfileReady("Successfully sign out")
    }

    fun setPhoto(uri: Uri) {
        viewState = Loading
        Firebase.auth.currentUser?.updateProfile(UserProfileChangeRequest.Builder().setPhotoUri(uri).build())
            ?.addOnSuccessListener {
                val users = Firebase.firestore.collection("Users")
                users.document(Firebase.auth.currentUser!!.uid).update("photoUrl", uri.toString()).addOnSuccessListener {
                //Firebase.database.reference.child("Users").child(Firebase.auth.currentUser!!.uid).child("photoUrl").setValue(uri.toString()).addOnSuccessListener {
                    viewState = PhotoReady("Photo updated", uri)
                }.addOnFailureListener {
                    viewState = NetworkError("Error")
                }
            }?.addOnFailureListener {
            viewState = NetworkError("Error")
        }
    }
}