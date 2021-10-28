package hu.bme.aut.chatify.ui.chat

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import hu.bme.aut.chatify.model.ClientToken
import hu.bme.aut.chatify.model.Conversation
import hu.bme.aut.chatify.model.Message
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

class ChatViewModel @Inject constructor(

) : RainbowCakeViewModel<ChatViewState>(Initialize) {
    fun init() = execute {
        viewState = Initialize
    }

    fun sendMessage(sender: String, receiver: String, message: String, requireContext: Context) = viewModelScope.launch{
        val conversations = Firebase.firestore.collection("Conversations")
        val query = Firebase.firestore.collection("Conversations")
                .whereEqualTo("participants.$sender", true)
                .whereEqualTo("participants.$receiver", true)
        query.get().addOnSuccessListener {
            if(!it.isEmpty) {
                val id = it.documents[0].data?.get("id") as String
                val newMessage = conversations.document(id).collection("Messages").document()
                //conversations.document(id).collection("Messages").add(Message(id, sender, message, Date().time, false)).addOnSuccessListener {
                conversations.document(id).collection("Messages").document(newMessage.id).set(Message(newMessage.id, id, sender, message, Date().time, false)).addOnSuccessListener {
                    conversations.document(id).collection("Messages").get().addOnSuccessListener {
                        conversations.document(id).update("lastMessage", message)
                        conversations.document(id).update("lastMessageTime", Date().time)
                        if (it.documents.size < 2) {
                            viewState = InitChat("Init chat")
                        } else {
                            viewState = MessageSent("Message sent")
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

    fun getReceiverTokens(receiver: String, id: String, message: String, requireContext: Context) = viewModelScope.launch{
        Firebase.firestore.collection("ClientTokens").document(receiver).addSnapshotListener { snapshot, error ->
            if(snapshot != null && snapshot.exists()){
                val clientToken = ClientToken(snapshot.data?.get("tokens") as HashMap<String, Boolean>)
                val toReceiver = JSONObject()
                val data = JSONObject()
                data.put("conversationId", id)
                data.put("receiver", receiver)
                data.put("message", message)
                for (token in clientToken.tokens.keys){
                    toReceiver.put("to", token)
                    toReceiver.put("data", data)
                    sendNotification(toReceiver, requireContext)
                }
                viewState = ChatReady("Message Sent")
            }
        }
    }

    private fun sendNotification(toReceiver: JSONObject, requireContext: Context) {
        val request = object: JsonObjectRequest(
            Method.POST,
            "https://fcm.googleapis.com/fcm/send",
            toReceiver,
            Response.Listener { },
            Response.ErrorListener { }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val map = HashMap<String, String>()
                map["Authorization"] = "key=AAAA68poFh4:APA91bER7DPF_vU0jzDgX4_Rlx2TwHCpoHUVtVKtT5Hhc04V09LkeMO7fQ0sjsC67vJLN3OCB2ohcPiWHh85qdd2-K2974m1KlZ41v5_wQN1X2sll4pauJovFobml6iBw1H4OkGzg4Ll"
                map["Content-type"] = bodyContentType
                return map
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }

        val requestQueue = Volley.newRequestQueue(requireContext)
        request.retryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        requestQueue.add(request)
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
        conversations.document(newConversation.id).set(Conversation(newConversation.id, "", -1, "#1DB954", participants)).addOnSuccessListener {
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
                val newMessage = conversations.document(id).collection("Messages").document()
                val imageName = System.currentTimeMillis().toString() + "." + getFileExtension(uri, requireContext)
                val storage = Firebase.storage.getReference("Images").child(imageName)
                storage.putFile(uri).addOnSuccessListener {
                    //conversations.document(id).collection("Messages").add(Message(id, sender, message, Date().time, true, imageName)).addOnSuccessListener {
                    conversations.document(id).collection("Messages").document(newMessage.id).set(Message(newMessage.id, id, sender, message, Date().time, true, imageName)).addOnSuccessListener {
                        conversations.document(id).collection("Messages").get().addOnSuccessListener {
                            conversations.document(id).update("lastMessage", "Sent a photo")
                            conversations.document(id).update("lastMessageTime", Date().time)
                            if (it.documents.size < 2) {
                                viewState = InitChat("Init chat")
                            } else {
                                viewState = ImageSent("Message sent")
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

    fun changeColor(color: String, sender: String, receiver: String) = viewModelScope.launch{
        Firebase.firestore.collection("Conversations")
                .whereEqualTo("participants.$sender", true)
                .whereEqualTo("participants.$receiver", true).get().addOnSuccessListener {
                    val id = it.documents[0].data?.get("id")
                    Firebase.firestore.collection("Conversations").document(id.toString()).update("chatColor", color).addOnSuccessListener {
                        viewState = ThemeApplied("Theme applied")
                    }.addOnFailureListener { it1 ->
                        viewState = NetworkError(it1.message.toString())
                    }
                }
    }
}