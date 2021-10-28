package hu.bme.aut.chatify.ui.chat

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.navigation.navigator
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import hu.bme.aut.chatify.R
import hu.bme.aut.chatify.adapter.MessageAdapter
import hu.bme.aut.chatify.databinding.FragmentChatBinding
import hu.bme.aut.chatify.model.Message
import hu.bme.aut.chatify.model.User
import hu.bme.aut.chatify.ui.imageview.ImageViewFragment
import kotlinx.android.synthetic.main.fragment_chat.*


@Suppress("IMPLICIT_CAST_TO_ANY")
class ChatFragment(private val userId: String) : RainbowCakeFragment<ChatViewState, ChatViewModel>(), MessageAdapter.ItemClickListener {

    private lateinit var binding: FragmentChatBinding
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var sender: String
    private lateinit var receiver: String
    private lateinit var message: String
    private lateinit var user: User
    private val PICK_IMAGE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sender = Firebase.auth.currentUser?.uid.toString()
        receiver = userId
    }

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
        Firebase.firestore.collection("Users").document(userId).get().addOnSuccessListener {
            user = User(
                it.data?.get("id").toString(), it.data?.get("name").toString(), it.data?.get(
                    "photoUrl"
                ).toString()
            )
            binding.tvDisplayName.text = user.name
            if(user.photoUrl.isNotEmpty()){
                Picasso.get().load(user.photoUrl).into(binding.civProfilePicture)
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
        }
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.Colors,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerColors.adapter = adapter
        Firebase.firestore.collection("Conversations")
                .whereEqualTo("participants.$sender", true)
                .whereEqualTo("participants.$receiver", true).get().addOnSuccessListener {
                if(it.documents.isNotEmpty()) {
                    var chatColor = it.documents[0].data?.get("chatColor")?.toString()
                    when (chatColor) {
                        "Red" -> {
                            binding.spinnerColors.setSelection(0)
                            binding.etTypeMessage.setBackgroundColor(Color.RED)
                            binding.btnSend.backgroundTintList = ColorStateList.valueOf(Color.RED)
                            binding.btnImage.imageTintList = ColorStateList.valueOf(Color.RED)
                            binding.btnBack.imageTintList = ColorStateList.valueOf(Color.RED)
                        }
                        "Blue" -> {
                            binding.spinnerColors.setSelection(1)
                            binding.etTypeMessage.setBackgroundColor(Color.BLUE)
                            binding.btnSend.backgroundTintList = ColorStateList.valueOf(Color.BLUE)
                            binding.btnImage.imageTintList = ColorStateList.valueOf(Color.BLUE)
                            binding.btnBack.imageTintList = ColorStateList.valueOf(Color.BLUE)
                        }
                        "Green" -> {
                            binding.spinnerColors.setSelection(2)
                            binding.etTypeMessage.setBackgroundColor(resources.getColor(R.color.app_color))
                            binding.btnSend.backgroundTintList =
                                ColorStateList.valueOf(resources.getColor(R.color.app_color))
                            binding.btnImage.imageTintList =
                                ColorStateList.valueOf(resources.getColor(R.color.app_color))
                            binding.btnBack.imageTintList =
                                ColorStateList.valueOf(resources.getColor(R.color.app_color))
                        }
                    }
                }
                else{
                    binding.spinnerColors.setSelection(2)
                    binding.etTypeMessage.setBackgroundColor(resources.getColor(R.color.app_color))
                    binding.btnSend.backgroundTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.app_color))
                    binding.btnImage.imageTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.app_color))
                    binding.btnBack.imageTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.app_color))
                }
                    binding.spinnerColors.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: View,
                            position: Int,
                            id: Long
                        ) {
                            when(parent.getItemAtPosition(position) as String) {
                                "Red" -> {
                                    viewModel.changeColor("Red", sender, receiver)
                                    binding.etTypeMessage.setBackgroundColor(Color.RED)
                                    binding.btnSend.backgroundTintList = ColorStateList.valueOf(Color.RED)
                                    binding.btnImage.imageTintList = ColorStateList.valueOf(Color.RED)
                                    binding.btnBack.imageTintList = ColorStateList.valueOf(Color.RED)
                                }
                                "Blue" -> {
                                    viewModel.changeColor("Blue", sender, receiver)
                                    binding.etTypeMessage.setBackgroundColor(Color.BLUE)
                                    binding.btnSend.backgroundTintList = ColorStateList.valueOf(Color.BLUE)
                                    binding.btnImage.imageTintList = ColorStateList.valueOf(Color.BLUE)
                                    binding.btnBack.imageTintList = ColorStateList.valueOf(Color.BLUE)
                                }
                                "Green" -> {
                                    viewModel.changeColor("Green", sender, receiver)
                                    binding.etTypeMessage.setBackgroundColor(resources.getColor(R.color.app_color))
                                    binding.btnSend.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.app_color))
                                    binding.btnImage.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.app_color))
                                    binding.btnBack.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.app_color))
                                }
                            }
                        }
                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
                }
        binding.btnBack.setOnClickListener {
            navigator?.pop()
        }
        binding.btnSend.setOnClickListener {
            if(binding.etTypeMessage.text.isNotEmpty()){
                message = binding.etTypeMessage.text.toString()
                binding.etTypeMessage.text.clear()
                sender = Firebase.auth.currentUser?.uid.toString()
                receiver = userId
                viewModel.sendMessage(sender, receiver, message, requireContext())
            }
        }
        binding.btnImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Choose a photo"), PICK_IMAGE)
        }
    }

    private fun initRecyclerView() {
        val query = Firebase.firestore.collection("Conversations")
                .whereEqualTo("participants.$sender", true)
                .whereEqualTo("participants.$receiver", true)
        query.get().addOnSuccessListener {
            if(!it.isEmpty){
                var options = FirestoreRecyclerOptions.Builder<Message>()
                        .setQuery(
                            Firebase.firestore
                                .collection("Conversations")
                                .document(it.documents[0].data?.get("id") as String)
                                .collection("Messages")
                                .orderBy("date", Query.Direction.DESCENDING), Message::class.java
                        )
                        .build()
                messageAdapter = MessageAdapter(options, this, requireContext())
                chatRecyclerView = chatList
                chatRecyclerView.adapter = messageAdapter
                chatRecyclerView.layoutManager = LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                    true
                )
                if(this::messageAdapter.isInitialized){
                    messageAdapter.startListening()
                }
                binding.chatList.scrollToPosition(0)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1){
            val uri = data?.data
            if (uri != null) {
                viewModel.sendImage(uri, sender, receiver, requireContext())
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if(this::messageAdapter.isInitialized){
            messageAdapter.startListening()
        }
        viewModel.getConversation(userId, sender, receiver)
    }

    override fun onDestroy() {
        if(this::messageAdapter.isInitialized){
            messageAdapter.stopListening()
        }
        super.onDestroy()
    }

    override fun getViewResource(): Int = R.layout.fragment_chat

    override fun provideViewModel(): ChatViewModel = getViewModelFromFactory()

    override fun render(viewState: ChatViewState) {
        when (viewState) {
            Initialize -> {
            }

            Loading -> {
            }

            is ThemeApplied -> {
                initRecyclerView()
                //navigator?.replace(ChatFragment(userId))
                viewModel.init()
            }

            is MessageSent -> {
                Firebase.firestore.collection("Conversations")
                    .whereEqualTo("participants.$sender", true)
                    .whereEqualTo("participants.$receiver", true).get().addOnSuccessListener {
                        if (!it.isEmpty) {
                            viewModel.getReceiverTokens(
                                receiver,
                                it.documents[0].data?.get("id").toString(),
                                message,
                                requireContext()
                            )
                        }
                    }
            }

            is ImageSent -> {
                Firebase.firestore.collection("Conversations")
                    .whereEqualTo("participants.$sender", true)
                    .whereEqualTo("participants.$receiver", true).get().addOnSuccessListener {
                        if (!it.isEmpty) {
                            viewModel.getReceiverTokens(
                                receiver,
                                it.documents[0].data?.get("id").toString(),
                                "Sent a photo",
                                requireContext()
                            )
                        }
                    }
            }

            is ChatReady -> {
                binding.chatList.scrollToPosition(0)
                viewModel.init()
            }
            is InitChat -> {
                initRecyclerView()
                binding.chatList.scrollToPosition(0)
                viewModel.init()
            }
            is NetworkError -> {
                Toast.makeText(requireContext(), viewState.response, Toast.LENGTH_SHORT).show()
            }
        }.exhaustive
    }

    override fun onItemClicked(imageName: String) {
        navigator?.add(ImageViewFragment(imageName))
    }
}