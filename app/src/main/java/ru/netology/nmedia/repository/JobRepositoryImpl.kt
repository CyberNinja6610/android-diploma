package ru.netology.nmedia.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import ru.netology.nmedia.api.JobApi
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dao.JobDao
import ru.netology.nmedia.dto.Job
import ru.netology.nmedia.entity.JobEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException
import javax.inject.Inject

class JobRepositoryImpl @Inject constructor(
    private val jobDao: JobDao,
    private val jobApiService: JobApi,
    private val appAuth: AppAuth,
) : JobRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val data: Flow<List<Job>> = appAuth.authStateFlow
        .flatMapLatest { (myId, _) ->
            jobDao.getAll(myId).map {
                it.map { job -> job.toDto() }
            }
        }


    override suspend fun save(job: Job) {
        try {
            val response = jobApiService.save(job)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            jobDao.insert(JobEntity.fromDto(body, appAuth.authStateFlow.value.id))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getAll() {
        try {
            val response = jobApiService.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            jobDao.insert(body.toEntity(appAuth.authStateFlow.value.id))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            val response = jobApiService.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            jobDao.removeById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}