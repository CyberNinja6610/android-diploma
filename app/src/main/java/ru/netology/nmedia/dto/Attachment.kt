package ru.netology.nmedia.dto

import ru.netology.nmedia.enumiration.AttachmentType


data class Attachment(
    val url: String,
    val type: AttachmentType,
)