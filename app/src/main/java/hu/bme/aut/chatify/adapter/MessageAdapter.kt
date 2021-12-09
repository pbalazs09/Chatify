package hu.bme.aut.chatify.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.common.ChangeEventType
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import hu.bme.aut.chatify.R
import hu.bme.aut.chatify.model.Message


class MessageAdapter(
        options: FirestoreRecyclerOptions<Message>,
        private val listener: ItemClickListener,
        private val context: Context
) : FirestoreRecyclerAdapter<Message, MessageAdapter.MessageViewHolder>(options) {

    companion object {
        private const val MSG_TYPE_LEFT = 0
        private const val MSG_TYPE_RIGHT = 1
        private const val MSG_TYPE_DELETED_RIGHT = 2
        private const val MSG_TYPE_DELETED_LEFT = 3
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
            if (message!!.image) {
                listener.onItemClicked(message!!.imageName)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val itemView: View
        when (viewType) {
            MSG_TYPE_RIGHT -> {
                itemView = LayoutInflater.from(parent.context).inflate(
                        R.layout.message_right,
                        parent,
                        false
                )
            }
            MSG_TYPE_DELETED_RIGHT -> {
                itemView = LayoutInflater.from(parent.context).inflate(
                        R.layout.message_right_deleted,
                        parent,
                        false
                )
            }
            MSG_TYPE_DELETED_LEFT -> {
                itemView = LayoutInflater.from(parent.context).inflate(
                        R.layout.message_left_deleted,
                        parent,
                        false
                )
            }
            else -> {
                itemView = LayoutInflater.from(parent.context).inflate(
                        R.layout.message_left,
                        parent,
                        false
                )
            }
        }
        return MessageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int, model: Message) {
        if (model.sender != Firebase.auth.currentUser?.uid.toString()) {
            Firebase.firestore.collection("Users").document(model.sender).get().addOnSuccessListener {
                if (!it.data.isNullOrEmpty()) {
                    val photoUrl = it.data!!["photoUrl"].toString()
                    if (photoUrl.isNotEmpty()) {
                        Picasso.get().load(photoUrl).into(holder.civ)
                    }
                }
            }
        }

        Firebase.firestore.collection("Conversations").document(model.conversationId).get().addOnSuccessListener {
            if (!it.data.isNullOrEmpty()) {
                if (holder.text.text != "Deleted message") {
                    if (holder.message?.sender == Firebase.auth.currentUser?.uid.toString()) {
                        when (it.data!!["chatColor"].toString()) {
                            "Red" -> {
                                val shapeAppearanceModel = ShapeAppearanceModel()
                                        .toBuilder()
                                        .setAllCorners(CornerFamily.ROUNDED, 70f)
                                        .build()
                                val shapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
                                shapeDrawable.fillColor =
                                        ContextCompat.getColorStateList(context, R.color.red)
                                holder.text.background = shapeDrawable
                            }
                            "Blue" -> {
                                val shapeAppearanceModel = ShapeAppearanceModel()
                                        .toBuilder()
                                        .setAllCorners(CornerFamily.ROUNDED, 70f)
                                        .build()
                                val shapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
                                shapeDrawable.fillColor =
                                        ContextCompat.getColorStateList(context, R.color.blue)
                                holder.text.background = shapeDrawable
                            }
                            "Green" -> {
                                val shapeAppearanceModel = ShapeAppearanceModel()
                                        .toBuilder()
                                        .setAllCorners(CornerFamily.ROUNDED, 70f)
                                        .build()
                                val shapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
                                shapeDrawable.fillColor =
                                        ContextCompat.getColorStateList(context, R.color.app_color)
                                holder.text.background = shapeDrawable
                            }
                        }
                    }
                } else {
                    val shapeAppearanceModel = ShapeAppearanceModel()
                            .toBuilder()
                            .setAllCorners(CornerFamily.ROUNDED, 70f)
                            .build()
                    val shapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
                    shapeDrawable.fillColor =
                            ContextCompat.getColorStateList(context, R.color.gray)
                    holder.text.background = shapeDrawable
                    holder.text.setTextColor(Color.GRAY)
                }
            }
        }

        holder.itemView.setOnLongClickListener {
            if (model.message != "Deleted message" && model.sender == Firebase.auth.currentUser?.uid.toString()) {
                AlertDialog.Builder(context)
                        .setMessage("Would you like to delete this message?")
                        .setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                            val message = Firebase.firestore
                                    .collection("Conversations")
                                    .document(model.conversationId)
                                    .collection("Messages")
                                    .document(model.id)
                            message.update("image", false).addOnSuccessListener {
                                message.update("message", "Deleted message").addOnSuccessListener {
                                    message.update("imageName", "").addOnSuccessListener {
                                        val conversation = Firebase.firestore
                                                .collection("Conversations")
                                                .document(model.conversationId)
                                        conversation.update("lastMessage", "Deleted message").addOnSuccessListener {
                                            holder.text.setTypeface(null, Typeface.ITALIC)
                                            holder.text.visibility = View.VISIBLE
                                            holder.ivImage.visibility = View.GONE
                                            holder.cvImage.visibility = View.GONE
                                        }
                                    }
                                }
                            }
                        }
                        .setNegativeButton("No", null)
                        .show()
            }
            true
        }
        holder.message = model
        if (model.image) {
            Firebase.storage.getReference("Images").child(model.imageName).downloadUrl.addOnSuccessListener {
                if (model.message.isNotEmpty()) {
                    Picasso.get().load(it).into(holder.ivImage)
                    holder.ivImage.visibility = View.VISIBLE
                    holder.cvImage.visibility = View.VISIBLE
                    holder.text.visibility = View.GONE
                }
            }.addOnFailureListener {
                if (model.message.isNotEmpty()) {
                    Picasso.get().load(Uri.parse(model.message)).into(holder.ivImage)
                    holder.ivImage.visibility = View.VISIBLE
                    holder.cvImage.visibility = View.VISIBLE
                    holder.text.visibility = View.GONE
                }
            }
        } else {
            holder.text.visibility = View.VISIBLE
            holder.ivImage.visibility = View.GONE
            holder.cvImage.visibility = View.GONE
            holder.text.text = model.message
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            snapshots[position].message == "Deleted message" && snapshots[position].sender == Firebase.auth.currentUser?.uid.toString() -> {
                MSG_TYPE_DELETED_RIGHT
            }
            snapshots[position].message == "Deleted message" && snapshots[position].sender != Firebase.auth.currentUser?.uid.toString() -> {
                MSG_TYPE_DELETED_LEFT
            }
            snapshots[position].sender == Firebase.auth.currentUser?.uid.toString() -> {
                MSG_TYPE_RIGHT
            }
            else -> {
                MSG_TYPE_LEFT
            }
        }
    }

    override fun onChildChanged(
            type: ChangeEventType,
            snapshot: DocumentSnapshot,
            newIndex: Int,
            oldIndex: Int
    ) {
        super.onChildChanged(type, snapshot, newIndex, oldIndex)
        if (type != ChangeEventType.CHANGED) {
            listener.onMessageReceived()
        }
    }

    interface ItemClickListener {
        fun onItemClicked(imageName: String)
        fun onMessageReceived()
    }
}