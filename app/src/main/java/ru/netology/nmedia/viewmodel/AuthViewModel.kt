package ru.netology.nmedia.viewmodel
import android.net.Uri
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.UserApi
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.model.PhotoModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: AppAuth,
    private val userApi: UserApi,
) : ViewModel() {
    val data: LiveData<AuthState> = auth
        .authStateFlow
        .asLiveData(Dispatchers.Default)

    val authenticated: Boolean
        get() = auth.authStateFlow.value.id != 0L

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?>
        get() = _user

    private val _photo = MutableLiveData<PhotoModel>(null)
    val photo: LiveData<PhotoModel>
        get() = _photo

    fun changePhoto(uri: Uri?) {
        _photo.value = uri?.let {
            PhotoModel(uri)
        }
    }

    fun loadUser(id: Long) = viewModelScope.launch {
        if (id == 0L) {
            _user.value = null
        } else {
            try {
                val response = userApi.getById(id)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                _user.value = response.body() ?: throw ApiError(response.code(), response.message())
            } catch (e: Exception) {
                _user.value = null
                print(e.message)
            }
        }
    }
}