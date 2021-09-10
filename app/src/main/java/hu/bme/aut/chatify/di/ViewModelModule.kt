package hu.bme.aut.chatify.di

import androidx.lifecycle.ViewModel
import co.zsmb.rainbowcake.dagger.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import hu.bme.aut.chatify.ui.login.LoginViewModel
import hu.bme.aut.chatify.ui.main.MainViewModel
import hu.bme.aut.chatify.ui.profile.ProfileViewModel
import hu.bme.aut.chatify.ui.pwreset.PasswordResetViewModel
import hu.bme.aut.chatify.ui.signup.SignUpViewModel

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(mainViewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    abstract fun bindLoginViewModel(loginViewModel: LoginViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SignUpViewModel::class)
    abstract fun bindSignUpViewModel(signUpViewModel: SignUpViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PasswordResetViewModel::class)
    abstract fun bindPasswordResetViewModel(passwordResetViewModel: PasswordResetViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel::class)
    abstract fun bindProfileViewModel(profileViewModel: ProfileViewModel): ViewModel
}
