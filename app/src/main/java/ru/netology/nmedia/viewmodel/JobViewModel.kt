package ru.netology.nmedia.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.dto.Job
import ru.netology.nmedia.model.JobFeedModel
import ru.netology.nmedia.model.JobFeedModelState
import ru.netology.nmedia.repository.JobRepository
import ru.netology.nmedia.utils.SingleLiveEvent
import javax.inject.Inject

private val default = Job(
    id = 0L,
    name = "",
    position = "",
    start = 0L
)

@HiltViewModel
@ExperimentalCoroutinesApi
class JobViewModel @Inject constructor(
    private val jobRepository: JobRepository
) : ViewModel() {

    val data: LiveData<JobFeedModel> = jobRepository.data
        .map { jobs ->
            JobFeedModel(
                jobs.map { it },
                jobs.isEmpty()
            )
        }
        .catch { e ->
            e.printStackTrace()
        }
        .asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData<JobFeedModelState>()
    val dataState: LiveData<JobFeedModelState>
        get() = _dataState

    private val edited = MutableLiveData(default)

    private val _jobCreated = SingleLiveEvent<Unit>()
    val jobCreated: LiveData<Unit>
        get() = _jobCreated

    init {
        load()
    }

    fun load() = viewModelScope.launch {
        try {
            _dataState.value = JobFeedModelState(loading = true)
            jobRepository.getAll()
            _dataState.value = JobFeedModelState()
        } catch (e: Exception) {
            _dataState.value = JobFeedModelState(error = true)
        }
    }

    fun refresh() = viewModelScope.launch {
        try {
            _dataState.value = JobFeedModelState(refreshing = true)
            jobRepository.getAll()
            _dataState.value = JobFeedModelState()
        } catch (e: Exception) {
            _dataState.value = JobFeedModelState(error = true)
        }
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            jobRepository.removeById(id)
            _dataState.value = JobFeedModelState()
        } catch (e: Exception) {
            _dataState.value = JobFeedModelState(error = true)
        }
    }

    fun save() = viewModelScope.launch {
        edited.value?.let { job ->
            _jobCreated.value = Unit
            viewModelScope.launch {
                try {
                    jobRepository.save(job)
                    _jobCreated.value = Unit
                } catch (e: Exception) {
                    _dataState.value = JobFeedModelState(error = true)
                }
            }
        }
        edited.value = default
    }

    fun edit(job: Job) {
        edited.value = job
    }

    fun changeContent(
        startDateTime: Long,
        name: String,
        position: String,
        finishDateTime: Long? = null,
        link: String? = null
    ) {
        edited.value = edited.value?.copy(
            start = startDateTime,
            finish = finishDateTime,
            name = name,
            position = position,
            link = link
        )
    }
}