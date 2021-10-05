package hu.bme.aut.chatify.ui.people

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.dagger.getViewModelFromFactory
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.navigation.navigator
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import hu.bme.aut.chatify.R
import hu.bme.aut.chatify.adapter.PeopleAdapter
import hu.bme.aut.chatify.databinding.FragmentPeopleBinding
import hu.bme.aut.chatify.model.User
import hu.bme.aut.chatify.ui.chat.ChatFragment
import hu.bme.aut.chatify.ui.profile.ProfileFragment
import kotlinx.android.synthetic.main.fragment_people.*

class PeopleFragment : RainbowCakeFragment<PeopleViewState, PeopleViewModel>(), PeopleAdapter.ItemClickListener {

    private lateinit var peopleRecyclerView: RecyclerView
    private lateinit var peopleAdapter: PeopleAdapter
    private lateinit var binding: FragmentPeopleBinding
    val people = mutableListOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPeopleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        binding.civProfilePicture.setOnClickListener {
            navigator?.add(
                ProfileFragment(),
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
    }

    private fun initRecyclerView() {
        var options = FirestoreRecyclerOptions.Builder<User>()
            .setQuery(Firebase.firestore
                .collection("Users")
                .whereNotEqualTo("id", Firebase.auth.currentUser?.uid.toString()), User::class.java)
            .build()
        peopleRecyclerView = peopleList
        peopleAdapter = PeopleAdapter(options, this)
        peopleRecyclerView.adapter = peopleAdapter
        peopleRecyclerView.layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
        )
        peopleAdapter.startListening()
    }

    override fun onDestroy() {
        if(this::peopleAdapter.isInitialized){
            peopleAdapter.stopListening()
        }
        super.onDestroy()
    }

    override fun getViewResource(): Int = R.layout.fragment_people

    override fun provideViewModel(): PeopleViewModel = getViewModelFromFactory()

    override fun render(viewState: PeopleViewState) {
        when (viewState) {
            Initialize -> {

            }

            Loading -> {

            }
            is NetworkError -> TODO()
            is PeopleReady -> TODO()
        }.exhaustive
    }

    override fun onItemClicked(userId: String) {
        navigator?.add(ChatFragment(userId))
    }
}