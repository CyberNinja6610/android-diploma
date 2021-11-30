package ru.netology.nmedia.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.dto.Token
import ru.netology.nmedia.dto.User

interface UserApi {

    @GET("users")
    suspend fun getAll(): Response<List<User>>

    @GET("users/{id}")
    suspend fun getById(@Path("id") id: Long): Response<User>

    @POST("users/registration")
    suspend fun register(@Query ("login") login: String, @Query ("pass") pass: String, @Query ("name") name: String): Response<Token>

    @Multipart
    @POST("users/registration")
    suspend fun register(@Part("login") login: RequestBody, @Part("pass") pass: RequestBody, @Part("name") name: RequestBody, @Part file: MultipartBody.Part): Response<Token>


    @POST("users/authentication")
    suspend fun sendAuth(@Query ("login") login: String, @Query ("pass") pass: String): Response<Token>

    @POST("users/push-tokens")
    suspend fun pushTokens(@Body pushToken: PushToken): Response<Unit>

}