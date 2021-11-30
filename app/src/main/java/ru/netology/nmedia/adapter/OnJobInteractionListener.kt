package ru.netology.nmedia.adapter

import ru.netology.nmedia.dto.Job

interface OnJobInteractionListener {
    fun onEdit(job: Job)
    fun onRemove(job: Job)
}