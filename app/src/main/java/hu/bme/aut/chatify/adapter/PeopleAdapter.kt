package hu.bme.aut.chatify.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import hu.bme.aut.chatify.R
import hu.bme.aut.chatify.model.User
import hu.bme.aut.chatify.ui.people.PeopleFragment

class PeopleAdapter(private val listener: ItemClickListener) :
    RecyclerView.Adapter<PeopleAdapter.PeopleViewHolder>() {
    var people = mutableListOf<User>()

    inner class PeopleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var person: User? = null
        var name: TextView
        var civ: CircleImageView

        init {
            itemView.setOnClickListener(this)
            name = itemView.findViewById(R.id.tvPartner)
            civ = itemView.findViewById(R.id.circleImageView)
        }

        override fun onClick(v: View?) {
            listener.onItemClicked(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleViewHolder {
        val itemView: View = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.people_list_item, parent, false)
        return PeopleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PeopleViewHolder, position: Int) {
        val person = people[position]
        holder.person = person
        holder.name.text = person.name
        Picasso.get().load(person.photoUrl).into(holder.civ)
    }

    interface ItemClickListener {
        fun onItemClicked(position: Int)
    }

    fun update(peopleList: MutableList<User>) {
        people.clear()
        people.addAll(peopleList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = people.size
}