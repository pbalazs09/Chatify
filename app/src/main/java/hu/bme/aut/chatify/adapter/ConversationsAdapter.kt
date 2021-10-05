package hu.bme.aut.chatify.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import hu.bme.aut.chatify.R
import hu.bme.aut.chatify.model.Conversation
import hu.bme.aut.chatify.model.Message
import hu.bme.aut.chatify.model.User
import java.text.SimpleDateFormat

class ConversationsAdapter(options: FirestoreRecyclerOptions<Conversation>, private val listener: ItemClickListener) : FirestoreRecyclerAdapter<Conversation, ConversationsAdapter.ConversationsViewHolder>(options) {

    inner class ConversationsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var conversation: Conversation? = null
        var partner: TextView = itemView.findViewById(R.id.tvPartner)
        val lastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        val lastMessageTime: TextView = itemView.findViewById(R.id.tvTime)
        var civ: CircleImageView = itemView.findViewById(R.id.circleImageView)
        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
            for(key in conversation!!.participants.keys){
                if(key != Firebase.auth.currentUser?.uid.toString()){
                    listener.onItemClicked(key)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationsAdapter.ConversationsViewHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.conversation_list_item, parent, false)
        return ConversationsViewHolder(itemView)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ConversationsViewHolder, position: Int, model: Conversation) {
        holder.conversation = model
        if(model.lastMessage.length > 25){
            holder.lastMessage.text = model.lastMessage.substring(0, 22) + "..."
        }
        else{
            holder.lastMessage.text = model.lastMessage
        }
        val simpleDateFormat = SimpleDateFormat("HH:mm")
        holder.lastMessageTime.text = simpleDateFormat.format(model.lastMessageTime)
        for(userId in model.participants.keys){
            if(userId != Firebase.auth.currentUser?.uid.toString()){
                Firebase.firestore.collection("Users").document(userId).get().addOnSuccessListener {
                    val photoUrl = it.data?.get("photoUrl") as String?
                    if(!it.data.isNullOrEmpty()){
                        holder.partner.text = it.data!!["name"] as String
                    }
                    if(!photoUrl.isNullOrEmpty()){
                        Picasso.get().load(photoUrl).into(holder.civ)
                    }
                }
            }
        }
    }
    interface ItemClickListener {
        fun onItemClicked(userId: String)
    }
}