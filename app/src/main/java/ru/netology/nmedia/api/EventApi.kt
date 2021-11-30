package ru.netology.nmedia.api

import retrofit2.Response
import retrofit2.http.*
import ru.netology.nmedia.dto.*


interface EventApi {

    @GET("events/latest")
    suspend fun getLatest(@Query("count") count: Int): Response<List<Event>>

    @POST("events")
    suspend fun save(@Body event: Event): Response<Event>

    @POST("events/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response<Event>

    @DELETE("events/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Long): Response<Event>

    @DELETE("events/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Unit>

    @GET("events/{id}/before")
    suspend fun getBefore(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Event>>

    @POST("events/{id}/participants")
    suspend fun participateById(@Path("id") id: Long): Response<Event>

    @DELETE("events/{id}/participants")
    suspend fun rejectParticipateById(@Path("id") id: Long): Response<Event>

}