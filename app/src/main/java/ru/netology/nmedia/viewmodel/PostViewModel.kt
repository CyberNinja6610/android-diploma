package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.model.*
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.utils.SingleLiveEvent
import java.lang.Exception
import javax.inject.Inject

private val default = Post(
    id = 0,
    content = "",
    authorId = 0,
    author = "",
    authorAvatar = "",
    /** Почему-то ловил ошибку с бэкенда, если оставлял поле пустым, поэтому сделал 0*/
    published = "0"
);

private val noPhoto = PhotoModel()

@ExperimentalCoroutinesApi
@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    auth: AppAuth,
) : ViewModel() {

    private val cached = postRepository.data.cachedIn(viewModelScope)

    val data: Flow<PagingData<Post>> = auth.authStateFlow
        .flatMapLatest { (myId, _) ->
            cached.map { pagingData ->
                pagingData.map { post ->
                    post.copy(
                        ownedByMe = post.authorId == myId,
                        likedByMe = post.likeOwnerIds.contains(myId)
                    )
                }
            }
        }

    private val _dataState = MutableLiveData<PostsFeedModelState>()
    val dataState: LiveData<PostsFeedModelState>
        get() = _dataState

    private val edited = MutableLiveData(default)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo


    init {
        load()
    }

    fun load() = viewModelScope.launch {
        try {
            _dataState.value = PostsFeedModelState(loading = true)
            postRepository.getLatest()
            _dataState.value = PostsFeedModelState()
        } catch (e: Exception) {
            _dataState.value = PostsFeedModelState(error = true)
        }
    }

    fun refresh() = viewModelScope.launch {
        try {
            _dataState.value = PostsFeedModelState(refreshing = true)
            postRepository.getLatest()
            _dataState.value = PostsFeedModelState()
        } catch (e: Exception) {
            _dataState.value = PostsFeedModelState(error = true)
        }
    }

    fun likeById(id: Long) = viewModelScope.launch {
        try {
            postRepository.likeById(id)
            _dataState.value = PostsFeedModelState()
        } catch (e: Exception) {
            _dataState.value = PostsFeedModelState(error = true)
        }
    }

    fun dislikeById(id: Long) = viewModelScope.launch {
        try {
            postRepository.dislikeById(id)
            _dataState.value = PostsFeedModelState()
        } catch (e: Exception) {
            _dataState.value = PostsFeedModelState(error = true)
        }
    }

    fun removeById(id: Long) = viewModelScope.launch {
        viewModelScope.launch {
            try {
                postRepository.removeById(id)
                _dataState.value = PostsFeedModelState()
            } catch (e: Exception) {
                _dataState.value = PostsFeedModelState(error = true)
            }
        }
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (text == edited.value?.content) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }


    fun save() {
        edited.value?.let {
            viewModelScope.launch {
                try {
                    postRepository.save(
                        it, _photo.value?.uri?.let { MediaUpload(it.toFile()) }
                    )

                    _postCreated.value = Unit
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        edited.value = default
        _photo.value = noPhoto
    }


    fun edit(post: Post) {
        edited.value = post
    }

    fun changePhoto(uri: Uri?) {
        _photo.value = PhotoModel(uri)
    }
}