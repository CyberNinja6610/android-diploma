package ru.netology.nmedia.api

import retrofit2.Response
import retrofit2.http.*
import ru.netology.nmedia.dto.Job

interface JobApi {

    @POST("my/jobs")
    suspend fun save(@Body job: Job): Response<Job>

    @GET("my/jobs")
    suspend fun getAll(): Response<List<Job>>

    @DELETE("my/jobs/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Unit>
}