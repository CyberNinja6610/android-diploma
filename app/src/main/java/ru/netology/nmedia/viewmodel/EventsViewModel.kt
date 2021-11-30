package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Event
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.entity.EventTypeEmbeddable
import ru.netology.nmedia.enumiration.EventType
import ru.netology.nmedia.model.EventFeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.EventRepository
import ru.netology.nmedia.utils.SingleLiveEvent
import javax.inject.Inject

private val default = Event(
    id = 0L,
    authorId = 0L,
    author = "",
    authorAvatar = null,
    content = "",
    datetime = "",
    published = "",
    type = EventType.ONLINE,
    link = ""
)

private val noPhoto = PhotoModel()

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class EventsViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    auth: AppAuth
) : ViewModel() {

    private val cached = eventRepository.data.cachedIn(viewModelScope)

    val data: Flow<PagingData<Event>> = auth.authStateFlow
        .flatMapLatest { (myId, _) ->
            cached.map { pagingData ->
                pagingData.map { event ->
                    event.copy(
                        /** Установка полей, зависящих от текущего юзера*/
                        ownedByMe = event.authorId == myId,
                        likedByMe = event.likeOwnerIds.contains(myId),
                        participatedByMe = event.participantsIds.contains(myId),
                    )
                }
            }
        }

    private val _dataState = MutableLiveData<EventFeedModelState>()
    val dataState: LiveData<EventFeedModelState>
        get() = _dataState

    private val edited = MutableLiveData(default)

    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: LiveData<Unit>
        get() = _eventCreated

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo


    init {
        load ()
    }

    fun load() = viewModelScope.launch {
        try {
            _dataState.value = EventFeedModelState(loading = true)
            eventRepository.getLatest()
            _dataState.value = EventFeedModelState()
        } catch (e: Exception) {
            _dataState.value = EventFeedModelState(error = true)
        }
    }

    fun refresh() = viewModelScope.launch {
        try {
            _dataState.value = EventFeedModelState(refreshing = true)
            eventRepository.getLatest()
            _dataState.value = EventFeedModelState()
        } catch (e: Exception) {
            _dataState.value = EventFeedModelState(error = true)
        }
    }

    fun likeById(id: Long) = viewModelScope.launch {
        try {
            eventRepository.likeById(id)
            _dataState.value = EventFeedModelState()
        } catch (e: Exception) {
            _dataState.value = EventFeedModelState(error = true)
        }
    }

    fun dislikeById(id: Long) = viewModelScope.launch {
        try {
            eventRepository.dislikeById(id)
            _dataState.value = EventFeedModelState()
        } catch (e: Exception) {
            _dataState.value = EventFeedModelState(error = true)
        }
    }

    fun participateById(id: Long) = viewModelScope.launch {
        try {
            eventRepository.participateById(id)
            _dataState.value = EventFeedModelState()
        } catch (e: Exception) {
            _dataState.value = EventFeedModelState(error = true)
        }
    }

    fun rejectParticipateById(id: Long) = viewModelScope.launch {
        try {
            eventRepository.rejectParticipateById(id)
            _dataState.value = EventFeedModelState()
        } catch (e: Exception) {
            _dataState.value = EventFeedModelState(error = true)
        }
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            eventRepository.removeById(id)
            _dataState.value = EventFeedModelState()
        } catch (e: Exception) {
            _dataState.value = EventFeedModelState(error = true)
        }
    }

    fun changeContent(content: String, datetime: String, type: String) {
        val typeDto = EventTypeEmbeddable(type).toDto()
        print(edited.value?.content)
        if (content == edited.value?.content && datetime == edited.value?.datetime && typeDto == edited.value?.type) {
            return
        }
        edited.value = edited.value?.copy(content = content, datetime = datetime, type = typeDto)
    }


    fun save() {
        edited.value?.let {
            viewModelScope.launch {
                try {
                    eventRepository.save(
                        it, _photo.value?.uri?.let { MediaUpload(it.toFile()) }
                    )
                    _eventCreated.value = Unit
                } catch (e: Exception) {
                    e.printStackTrace()
                    _dataState.value = EventFeedModelState(error = true)
                }
            }
        }
        edited.value = default
        _photo.value = noPhoto
    }

    fun edit(event: Event) {
        edited.value = event
    }

    fun changePhoto(uri: Uri?) {
        _photo.value = PhotoModel(uri)
    }

}