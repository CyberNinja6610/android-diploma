package ru.netology.nmedia.auth

import android.content.Context
import android.widget.Toast
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.R
import ru.netology.nmedia.api.UserApi
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val tokenKey = "token"
    private val idKey = "id"
    private val _authStateFlow: MutableStateFlow<AuthState>


    init {
        val id = prefs.getLong(idKey, 0)
        val token = prefs.getString(tokenKey, null)

        if (id == 0L || token == null) {
            _authStateFlow = MutableStateFlow(AuthState())
            with(prefs.edit()) {
                clear()
                apply()
            }
        } else {
            _authStateFlow = MutableStateFlow(AuthState(id, token))
        }
        sendPushToken()
    }


    val authStateFlow: StateFlow<AuthState> = _authStateFlow.asStateFlow()

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface AppAuthEntryPoint {
        fun apiService(): UserApi
    }

    @Synchronized
    fun setAuth(id: Long, token: String?) {
        _authStateFlow.value = AuthState(id, token)
        with(prefs.edit()) {
            putLong(idKey, id)
            putString(tokenKey, token)
            apply()
        }
        sendPushToken()
    }

    @Synchronized
    fun authorize(login: String, pass: String) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val response = getApiService(context).sendAuth(login, pass)
                if (!response.isSuccessful) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(
                            context,
                            context.getString(R.string.error_sign_in),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    throw ApiError(response.code(), response.message())
                }
                val body = response.body() ?: throw ApiError(response.code(), response.message())
                setAuth(body.id, body.token)
                println(body.token)
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(
                        context,
                        context.getString(R.string.sign_in_success),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: IOException) {
                throw NetworkError
            } catch (e: Exception) {
                print(e.message)
            }
        }
    }



    @Synchronized
    fun setRegistration(login: String, pass: String, name: String, upload: MultipartBody.Part?) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                    val response = if (upload !=null) {
                        getApiService(context).register(
                            login.toRequestBody(),
                            pass.toRequestBody(),
                            name.toRequestBody(),
                            upload
                        )
                    } else {
                        getApiService(context).register(
                            login,
                            pass,
                            name,
                        )
                    }

                    if (!response.isSuccessful) {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                context,
                                context.getString(R.string.error_sign_up),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        throw ApiError(response.code(), response.message())
                    }
                    val body =
                        response.body() ?: throw ApiError(response.code(), response.message())
                    setAuth(body.id, body.token)
                    println(body.token)
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(
                            context,
                            context.getString(R.string.sign_up_success),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } catch (e: IOException) {
                throw NetworkError
            } catch (e: Exception) {
                print(e.message)
            }
        }
    }

    fun sendPushToken(token: String? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val pushToken = PushToken(token ?: Firebase.messaging.token.await())
                getApiService(context).pushTokens(pushToken)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    @Synchronized
    fun removeAuth() {
        _authStateFlow.value = AuthState()
        with(prefs.edit()) {
            clear()
            apply()
        }
        sendPushToken()
    }

    private fun getApiService(context: Context): UserApi {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            context,
            AppAuthEntryPoint::class.java
        )
        return hiltEntryPoint.apiService()
    }
}



data class AuthState(val id: Long = 0, val token: String? = null)