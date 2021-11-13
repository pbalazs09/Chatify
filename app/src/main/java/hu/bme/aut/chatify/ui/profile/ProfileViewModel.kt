package hu.bme.aut.chatify.ui.profile

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProfileViewModel @Inject constructor(

) : RainbowCakeViewModel<ProfileViewState>(Initialize) {
    fun init() = execute {
        viewState = Initialize
    }

    fun deleteProfile(requireContext: Context) = viewModelScope.launch {
        viewState = Loading
        val currentUser = Firebase.auth.currentUser
        if(currentUser != null){
            //val reference = Firebase.database.getReference("Users")
            //reference.child(currentUser.uid).removeValue()
            val users = Firebase.firestore.collection("Users")
            val conversations = Firebase.firestore.collection("Conversations")
            users.document(currentUser.uid).delete()
            conversations.get().addOnSuccessListener {
                if(it.documents.isNotEmpty()) {
                    for (conversation in it.documents) {
                        val participants = conversation.data?.get("participants") as HashMap<*, *>
                        if (participants.contains(currentUser.uid)) {
                            val messages = Firebase.firestore
                                    .collection("Conversations")
                                    .document(conversation.data?.get("id").toString())
                                    .collection("Messages")
                            messages.get().addOnSuccessListener { it1 ->
                                for (message in it1.documents) {
                                    if (message.data?.get("imageName").toString().isNotEmpty()) {
                                        Firebase.storage.getReference("Images").child(message.data?.get("imageName").toString()).delete()
                                    }
                                    val messageId = message.id
                                    messages.document(messageId).delete()
                                }
                                conversations.document(conversation.data?.get("id").toString()).delete()
                            }.addOnFailureListener { it2 ->
                                viewState = NetworkError("Error")
                                Toast.makeText(requireContext, it2.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    currentUser.delete()
                    viewState = ProfileReady("Profile deleted")
                }
                else{
                    currentUser.delete()
                    viewState = ProfileReady("Profile deleted")
                }
            }.addOnFailureListener {
                viewState = NetworkError("Error")
            }
        }
        else{
            viewState = NetworkError("Error")
        }
    }

    fun signOut() {
        Firebase.auth.signOut()
        viewState = ProfileReady("Successfully sign out")
    }

    fun setPhoto(uri: Uri, requireContext: Context) {
        viewState = Loading
        val imageName = System.currentTimeMillis().toString() + "." + getFileExtension(uri, requireContext)
        val storage = Firebase.storage.getReference("Images").child(imageName)
        storage.putFile(uri).addOnSuccessListener {
            Firebase.storage.getReference("Images").child(imageName).downloadUrl.addOnSuccessListener { itUri ->
                Firebase.auth.currentUser?.updateProfile(UserProfileChangeRequest.Builder().setPhotoUri(itUri).build())
                    ?.addOnSuccessListener {
                        val users = Firebase.firestore.collection("Users")
                        users.document(Firebase.auth.currentUser!!.uid).update("photoUrl", itUri.toString()).addOnSuccessListener {
                            //Firebase.database.reference.child("Users").child(Firebase.auth.currentUser!!.uid).child("photoUrl").setValue(uri.toString()).addOnSuccessListener {
                            viewState = PhotoReady("Photo updated", itUri)
                        }.addOnFailureListener {
                            viewState = NetworkError("Error")
                        }
                    }?.addOnFailureListener {
                        viewState = NetworkError("Error")
                    }
            }.addOnFailureListener {
                viewState = NetworkError("Error")
            }
        }.addOnFailureListener{
            viewState = NetworkError("Error")
        }
    }

    private fun getFileExtension(uri: Uri, context: Context): String? {
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(context.contentResolver.getType(uri))
    }
}