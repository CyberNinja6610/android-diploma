package ru.netology.nmedia.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Event
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload

interface EventRepository {
    val data: Flow<PagingData<Event>>
    suspend fun likeById(id: Long)
    suspend fun dislikeById(id: Long)
    suspend fun save(event: Event, mediaUpload: MediaUpload?)
    suspend fun removeById(id: Long)
    suspend fun getLatest()
    suspend fun participateById(id: Long)
    suspend fun rejectParticipateById(id: Long)
    suspend fun upload(upload: MediaUpload): Media
}