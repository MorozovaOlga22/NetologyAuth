package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.api.Api
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.AuthResponseState
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.util.SingleLiveEvent

private val noPhoto = PhotoModel()

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

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo


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
                AppAuth.getInstance().setAuth(authState.id, authState.token ?: "")

                _authRespState.value = AuthResponseState(false)
                _authDone.value = Unit
            } catch (e: Exception) {
                _authRespState.value = AuthResponseState(false, error = e.message)
                _authDone.value = Unit
            }
        }
    }

    fun signUp(login: String, pass: String, name: String) {
        _authRespState.value = AuthResponseState(true)
        viewModelScope.launch {
            try {
                val file = _photo.value?.uri?.toFile()
                val response = if (file != null) {
                    val media = MultipartBody.Part.createFormData(
                        "file", file.name, file.asRequestBody()
                    )
                    Api.SERVICE.registerUser(
                        login = login.toRequestBody("text/plain".toMediaType()),
                        pass = pass.toRequestBody("text/plain".toMediaType()),
                        name = name.toRequestBody("text/plain".toMediaType()),
                        media = media
                    )
                } else {
                    Api.SERVICE.registerUser(login = login, pass = pass, name = name)
                }

                if (!response.isSuccessful) {
                    _authRespState.value = AuthResponseState(false, error = "Registration failed")
                    return@launch
                }

                val authState = response.body()
                if (authState == null) {
                    _authRespState.value = AuthResponseState(false, error = "Empty response body")
                    return@launch
                }
                AppAuth.getInstance().setAuth(authState.id, authState.token ?: "")

                _authRespState.value = AuthResponseState(false)
                _authDone.value = Unit
            } catch (e: Exception) {
                _authRespState.value = AuthResponseState(false, error = e.message)
                _authDone.value = Unit
            }
        }
    }

    fun changePhoto(uri: Uri?) {
        _photo.value = PhotoModel(uri)
    }
}