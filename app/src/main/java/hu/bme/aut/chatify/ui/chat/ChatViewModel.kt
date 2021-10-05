package hu.bme.aut.chatify.ui.chat

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import hu.bme.aut.chatify.model.Conversation
import hu.bme.aut.chatify.model.Message
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class ChatViewModel @Inject constructor(

) : RainbowCakeViewModel<ChatViewState>(Initialize) {
    fun init() = execute {
        viewState = Initialize
    }

    fun sendMessage(sender: String, receiver: String, message: String) = viewModelScope.launch{
        val conversations = Firebase.firestore.collection("Conversations")
        val query = Firebase.firestore.collection("Conversations")
                .whereEqualTo("participants.$sender", true)
                .whereEqualTo("participants.$receiver", true)
        query.get().addOnSuccessListener {
            if(!it.isEmpty) {
                val id = it.documents[0].data?.get("id") as String
                conversations.document(id).collection("Messages").add(Message(sender, message, Date().time, false)).addOnSuccessListener {
                    conversations.document(id).collection("Messages").get().addOnSuccessListener {
                        conversations.document(id).update("lastMessage", message)
                        conversations.document(id).update("lastMessageTime", Date().time)
                        if (it.documents.size < 2) {
                            viewState = InitChat("Init chat")
                        } else {
                            viewState = ChatReady("Message sent")
                        }
                    }.addOnFailureListener {
                        viewState = NetworkError("Error")
                    }
                }.addOnFailureListener {
                    viewState = NetworkError("Error")
                }
            }
        }
    }

    fun getConversation(userId: String, sender: String, receiver: String) = viewModelScope.launch{
        val conversations = Firebase.firestore.collection("Conversations")

        conversations.get().addOnSuccessListener {
            if(it.isEmpty){
                createConversation(conversations, userId)
            }
            else {
                val query = Firebase.firestore
                    .collection("Conversations")
                    .whereEqualTo("participants.${Firebase.auth.currentUser?.uid.toString()}", true)
                    .whereEqualTo("participants.$receiver", true)
                query.get().addOnSuccessListener {
                    var size = it.size()
                    if(it.isEmpty){
                        createConversation(conversations, userId)
                    }
                    else{
                        val document = it.documents[0]
                        viewState = ChatReady(document.data?.get("id").toString())
                    }
                }.addOnFailureListener {
                    viewState = NetworkError("Error")
                }
            }
        }.addOnFailureListener {
            viewState = NetworkError("Error")
        }
    }

    private fun createConversation(conversations: CollectionReference, userId: String){
        val newConversation = conversations.document()
        val participants = hashMapOf(
                Firebase.auth.currentUser?.uid.toString() to true,
                userId to true
        )
        conversations.document(newConversation.id).set(Conversation(newConversation.id, "", -1, participants)).addOnSuccessListener {
            viewState = ChatReady(newConversation.id)
        }.addOnFailureListener {
            viewState = NetworkError("Error")
        }
    }

    fun sendImage(uri: Uri, sender: String, receiver: String, requireContext: Context) = viewModelScope.launch {
        val message = uri.toString()
        val conversations = Firebase.firestore.collection("Conversations")
        val query = Firebase.firestore.collection("Conversations")
                .whereEqualTo("participants.$sender", true)
                .whereEqualTo("participants.$receiver", true)
        query.get().addOnSuccessListener {
            if(!it.isEmpty) {
                val id = it.documents[0].data?.get("id") as String
                val imageName = System.currentTimeMillis().toString() + "." + getFileExtension(uri, requireContext)
                val storage = Firebase.storage.getReference("Images").child(imageName)
                storage.putFile(uri).addOnSuccessListener {
                    conversations.document(id).collection("Messages").add(Message(sender, message, Date().time, true, imageName)).addOnSuccessListener {
                        conversations.document(id).collection("Messages").get().addOnSuccessListener {
                            conversations.document(id).update("lastMessage", "Sent a photo")
                            conversations.document(id).update("lastMessageTime", Date().time)
                            if (it.documents.size < 2) {
                                viewState = InitChat("Init chat")
                            } else {
                                viewState = ChatReady("Message sent")
                            }
                        }.addOnFailureListener {
                            viewState = NetworkError("Error")
                        }
                    }.addOnFailureListener {
                        viewState = NetworkError("Error")
                    }
                }.addOnFailureListener{
                    viewState = NetworkError("Error")
                }
            }
        }
    }

    private fun getFileExtension(uri: Uri, context: Context): String? {
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(context.contentResolver.getType(uri))
    }
}