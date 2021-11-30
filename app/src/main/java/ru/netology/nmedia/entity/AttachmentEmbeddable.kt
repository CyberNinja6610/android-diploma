package ru.netology.nmedia.entity

import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.enumiration.AttachmentType

data class AttachmentEmbeddable(
    val url: String,
    val type: AttachmentType,
    val isPlaying: Boolean?,
) {
    fun toDto() = Attachment(url, type)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(
                it.url,
                it.type,
                isPlaying = it.type == AttachmentType.VIDEO || it.type == AttachmentType.AUDIO
            )
        }
    }
}