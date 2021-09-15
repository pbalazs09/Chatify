package hu.bme.aut.chatify.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import hu.bme.aut.chatify.R
import hu.bme.aut.chatify.model.Message
import hu.bme.aut.chatify.model.User

class MessageAdapter(options: FirestoreRecyclerOptions<Message>) : FirestoreRecyclerAdapter<Message, MessageAdapter.MessageViewHolder>(options) {

    companion object{
        private val MSG_TYPE_LEFT = 0
        private val MSG_TYPE_RIGHT = 1
    }

    var messageNumber = 0

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var message: Message? = null
        var text: TextView = itemView.findViewById(R.id.tvMessage)
        //var civ: CircleImageView = itemView.findViewById(R.id.civProfilePicture)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val itemView: View
        if(viewType == MSG_TYPE_RIGHT){
            itemView = LayoutInflater.from(parent.context).inflate(R.layout.message_right, parent, false)
        }
        else{
            itemView = LayoutInflater.from(parent.context).inflate(R.layout.message_left, parent, false)
        }
        return MessageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int, model: Message) {
        holder.message = model
        holder.text.text = model.message
        messageNumber++
    }

    override fun getItemViewType(position: Int): Int {
        if(getItem(position).sender == Firebase.auth.currentUser?.uid){
            return MSG_TYPE_RIGHT
        }
        return MSG_TYPE_LEFT
    }
}