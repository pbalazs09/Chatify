package hu.bme.aut.chatify.ui.people

import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import javax.inject.Inject

class PeopleViewModel @Inject constructor(

) : RainbowCakeViewModel<PeopleViewState>(Initialize) {

}