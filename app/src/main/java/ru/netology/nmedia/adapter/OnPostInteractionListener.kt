package ru.netology.nmedia.adapter

import ru.netology.nmedia.dto.Post

interface OnPostInteractionListener {
    fun onEdit(post: Post)
    fun onRemove(post: Post)
    fun onShare(post: Post)
    fun onLike(post: Post)
    fun onViewAttachment(post: Post)
}
