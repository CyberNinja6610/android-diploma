package ru.netology.nmedia.adapter

import ru.netology.nmedia.dto.Event

interface OnEventInteractionListener {
    fun onEdit(event: Event)
    fun onRemove(event: Event)
    fun onLike(event: Event)
    fun onViewAttachment(event: Event)
    fun onParticipate(event: Event)
    fun onShare(event: Event)
}