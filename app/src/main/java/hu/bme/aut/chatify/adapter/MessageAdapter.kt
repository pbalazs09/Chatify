package hu.bme.aut.chatify.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import hu.bme.aut.chatify.R
import hu.bme.aut.chatify.model.Message
import hu.bme.aut.chatify.model.User
import java.text.FieldPosition

class MessageAdapter(options: FirestoreRecyclerOptions<Message>, private val listener: ItemClickListener) : FirestoreRecyclerAdapter<Message, MessageAdapter.MessageViewHolder>(options) {

    companion object{
        private const val MSG_TYPE_LEFT = 0
        private const val MSG_TYPE_RIGHT = 1
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var message: Message? = null
        var text: TextView = itemView.findViewById(R.id.tvMessage)
        var civ: CircleImageView = itemView.findViewById(R.id.civProfilePicture)
        var ivImage: ImageView = itemView.findViewById(R.id.ivImage)
        var cvImage: CardView = itemView.findViewById(R.id.cvImage)
        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if(message!!.image){
                listener.onItemClicked(message!!.imageName)
            }
        }
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
        if(model.sender != Firebase.auth.currentUser?.uid.toString()){
            Firebase.firestore.collection("Users").document(Firebase.auth.currentUser?.uid.toString()).get().addOnSuccessListener {
                if(!it.data.isNullOrEmpty()){
                    val photoUrl = it.data!!["photoUrl"].toString()
                    if(!photoUrl.isEmpty()){
                        Picasso.get().load(photoUrl).into(holder.civ)
                    }
                }
            }
        }
        holder.message = model
        if(model.image){
            Firebase.storage.getReference("Images").child(model.imageName).downloadUrl.addOnSuccessListener {
                if(model.message.isNotEmpty()){
                    Picasso.get().load(it).into(holder.ivImage)
                    holder.ivImage.visibility = View.VISIBLE
                    holder.cvImage.visibility = View.VISIBLE
                    holder.text.visibility = View.GONE
                }
            }.addOnFailureListener {
                if(model.message.isNotEmpty()){
                    Picasso.get().load(Uri.parse(model.message)).into(holder.ivImage)
                    holder.ivImage.visibility = View.VISIBLE
                    holder.cvImage.visibility = View.VISIBLE
                    holder.text.visibility = View.GONE
                }
            }
        }
        else {
            holder.text.visibility = View.VISIBLE
            holder.ivImage.visibility = View.GONE
            holder.cvImage.visibility = View.GONE
            holder.text.text = model.message
        }
    }

    override fun getItemViewType(position: Int): Int {
        if(snapshots[position].sender == Firebase.auth.currentUser?.uid.toString()){
            return MSG_TYPE_RIGHT
        }
        return MSG_TYPE_LEFT
    }

    interface ItemClickListener {
        fun onItemClicked(imageName: String)
    }
}