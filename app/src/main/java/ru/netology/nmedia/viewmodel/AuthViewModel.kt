package ru.netology.nmedia.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.Api
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.AuthResponseState
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.util.SingleLiveEvent

class AuthViewModel : ViewModel() {
    val data: LiveData<AuthState> = AppAuth.getInstance()
        .authStateFlow
        .asLiveData(Dispatchers.Default)
    val authenticated: Boolean
        get() = AppAuth.getInstance().authStateFlow.value.id != 0L

    private val _authRespState = MutableLiveData<AuthResponseState>()
    val authRespState: LiveData<AuthResponseState>
        get() = _authRespState

    private val _authDone = SingleLiveEvent<Unit>()
    val authDone: LiveData<Unit>
        get() = _authDone

    fun signIn(login: String, pass: String) {
        _authRespState.value = AuthResponseState(true)
        viewModelScope.launch {
            try {
                val response = Api.SERVICE.updateUser(login, pass)
                if (!response.isSuccessful) {
                    _authRespState.value = AuthResponseState(false, error = "Authentication failed")
                    return@launch
                }

                val authState = response.body()
                if (authState == null) {
                    _authRespState.value = AuthResponseState(false, error = "Empty response body")
                    return@launch
                }
                AppAuth.getInstance().setAuth(authState.id, authState.token?: "")

                _authRespState.value = AuthResponseState(false)
                _authDone.value = Unit
            } catch (e: Exception) {
                _authRespState.value = AuthResponseState(false, error = e.message)
                _authDone.value = Unit
            }
        }
    }
}