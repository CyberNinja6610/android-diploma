package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Job

interface JobRepository {
    val data: Flow<List<Job>>


    suspend fun save(job: Job)
    suspend fun getAll()
    suspend fun removeById(id: Long)
}