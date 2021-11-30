package ru.netology.nmedia.model

import ru.netology.nmedia.dto.Job

data class JobFeedModel(
    val jobs: List<Job> = emptyList(),
    val empty: Boolean = false,
)

