package hu.bme.aut.chatify.ui.main

import android.os.Bundle
import android.util.proto.ProtoOutputStream.makeToken
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.navigation.navigator
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.squareup.picasso.Picasso
import hu.bme.aut.chatify.R
import hu.bme.aut.chatify.adapter.ConversationsAdapter
import hu.bme.aut.chatify.adapter.MessageAdapter
import hu.bme.aut.chatify.adapter.PeopleAdapter
import hu.bme.aut.chatify.databinding.FragmentMainBinding
import hu.bme.aut.chatify.model.ClientToken
import hu.bme.aut.chatify.model.Conversation
import hu.bme.aut.chatify.model.Message
import hu.bme.aut.chatify.model.User
import hu.bme.aut.chatify.ui.chat.ChatFragment
import hu.bme.aut.chatify.ui.login.LoginFragment
import hu.bme.aut.chatify.ui.people.PeopleFragment
import hu.bme.aut.chatify.ui.profile.ProfileFragment
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : RainbowCakeFragment<MainViewState, MainViewModel>(), ConversationsAdapter.ItemClickListener {

    private lateinit var binding: FragmentMainBinding
    private lateinit var conversationsAdapter: ConversationsAdapter
    private lateinit var conversationRecyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        binding.civProfilePicture.setOnClickListener {
            navigator?.add(ProfileFragment(),
                R.anim.slide_right_new,
                R.anim.slide_right_old,
                R.anim.slide_right_new,
                R.anim.slide_right_old
            )
        }
        val currentUser = Firebase.auth.currentUser
        binding.tvDisplayName.text = currentUser?.displayName
        if(currentUser?.photoUrl != null){
            Picasso.get().load(currentUser.photoUrl).into(binding.civProfilePicture)
        }
        makeToken(currentUser?.uid.toString())
    }

    private fun makeToken(uid: String) {
        Firebase.messaging.token.addOnSuccessListener { token ->
            if(token != null){
                Firebase.firestore.collection("ClientTokens").document(uid).get().addOnSuccessListener {
                    if(it.exists()){
                        val clientToken = ClientToken(it.data?.get("tokens") as HashMap<String, Boolean>)
                        if(!clientToken.tokens.keys.contains(token)){
                            Firebase.firestore.collection("ClientTokens").document(uid).update("tokens.$token", true)
                        }
                    }
                    else{
                        Firebase.firestore.collection("ClientTokens").document(uid).set(
                            ClientToken(
                                hashMapOf(token to true)
                            )
                        )
                    }
                }
            }
        }
    }

    private fun initRecyclerView() {
        var options = FirestoreRecyclerOptions.Builder<Conversation>()
            .setQuery(Firebase.firestore
                .collection("Conversations")
                .whereEqualTo("participants.${Firebase.auth.currentUser?.uid.toString()}", true), Conversation::class.java)
            .build()
        conversationsAdapter = ConversationsAdapter(options, this, requireContext())
        conversationRecyclerView = conversationList
        conversationRecyclerView.adapter = conversationsAdapter
        conversationRecyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        conversationsAdapter.startListening()
    }

    override fun onDestroy() {
        if(this::conversationsAdapter.isInitialized){
            conversationsAdapter.stopListening()
        }
        super.onDestroy()
    }

    override fun getViewResource(): Int = R.layout.fragment_main

    override fun provideViewModel(): MainViewModel = getViewModelFromFactory()

    override fun render(viewState: MainViewState) {
        when (viewState) {
            Initialize -> {
            }

            Loading -> {

            }
            else -> {}
        }.exhaustive
    }

    override fun onItemClicked(userId: String) {
        navigator?.add(ChatFragment(userId))
    }
}