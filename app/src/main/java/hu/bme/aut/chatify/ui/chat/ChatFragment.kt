package hu.bme.aut.chatify.ui.chat

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import hu.bme.aut.chatify.R
import hu.bme.aut.chatify.adapter.MessageAdapter
import hu.bme.aut.chatify.adapter.PeopleAdapter
import hu.bme.aut.chatify.databinding.FragmentChatBinding
import hu.bme.aut.chatify.model.Message
import hu.bme.aut.chatify.model.User
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_people.*
import java.util.*


class ChatFragment(val user: User) : RainbowCakeFragment<ChatViewState, ChatViewModel>() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var chatRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        binding.tvDisplayName.text = user.name
        if(user.photoUrl.isNotEmpty()){
            Picasso.get().load(user.photoUrl).into(binding.civProfilePicture)
        }
        binding.btnSend.setOnClickListener {
            if(binding.etTypeMessage.text.isNotEmpty()){
                val message = binding.etTypeMessage.text.toString()
                val sender = Firebase.auth.currentUser?.uid.toString()
                val receiver = user.id
                val date = Date().time
                Firebase.firestore.collection("Messages").add(
                        hashMapOf(
                                "message" to message,
                                "sender" to sender,
                                "receiver" to receiver,
                                "date" to date
                        )
                )
                //Firebase.firestore.collection("Conversations").document("conversationId").collection("Messages").add()

            }
        }
    }

    private fun initRecyclerView() {
        var options = FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(Firebase.firestore.collection("Messages").orderBy("date", Query.Direction.DESCENDING), Message::class.java)
                .build()
        messageAdapter = MessageAdapter(options)
        chatRecyclerView = chatList
        chatRecyclerView.adapter = messageAdapter
        chatRecyclerView.layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                true
        )
    }

    override fun onStart() {
        super.onStart()
        messageAdapter.startListening()
    }

    override fun onStop() {
        messageAdapter.stopListening()
        super.onStop()
    }

    override fun getViewResource(): Int = R.layout.fragment_chat

    override fun provideViewModel(): ChatViewModel = getViewModelFromFactory()

    override fun render(viewState: ChatViewState) {
        when (viewState) {
            Initialize -> {

            }

            Loading -> {

            }
        }.exhaustive
    }
}