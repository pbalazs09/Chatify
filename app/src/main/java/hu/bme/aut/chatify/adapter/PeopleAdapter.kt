package hu.bme.aut.chatify.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import hu.bme.aut.chatify.R
import hu.bme.aut.chatify.model.User

class PeopleAdapter(options: FirestoreRecyclerOptions<User>, private val listener: ItemClickListener) : FirestoreRecyclerAdapter<User, PeopleAdapter.PeopleViewHolder>(options) {

    inner class PeopleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var user: User? = null
        var partner: TextView = itemView.findViewById(R.id.tvPartner)
        var civ: CircleImageView = itemView.findViewById(R.id.circleImageView)
        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listener.onItemClicked(user!!.id)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleViewHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.people_list_item, parent, false)
        return PeopleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PeopleViewHolder, position: Int, model: User) {
        holder.user = model
        holder.partner.text = model.name
        if(model.photoUrl.isNotEmpty()){
            Picasso.get().load(model.photoUrl).into(holder.civ)
        }
        /*for(userId in model.participants){
            if(userId != Firebase.auth.currentUser?.uid.toString()){
                Firebase.firestore.collection("Users").document(userId).get().addOnSuccessListener {
                    if(!it.data.isNullOrEmpty()){
                        holder.partner.text = it.data!!["name"] as String
                    }
                }
            }
        }*/
    }

    interface ItemClickListener {
        fun onItemClicked(userId: String)
    }
}