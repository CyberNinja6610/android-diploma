package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Event

@Entity
class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val content: String,
    val datetime: String,
    val published: String,
    @Embedded
    val coords: CoordinatesEmbeddable?,
    @Embedded
    val type: EventTypeEmbeddable,
    val likedByMe: Boolean,
    val likeOwnerIds: Set<Long> = emptySet(),
    val link: String?,
    val participatedByMe: Boolean = false,
    @Embedded
    val attachment: AttachmentEmbeddable?,
    val participantsIds: Set<Long> = emptySet(),
) {
    fun toDto() =
        Event(
            id = id,
            authorId = authorId,
            author = author,
            authorAvatar = authorAvatar,
            content = content,
            datetime = datetime,
            published = published,
            coords = coords?.toDto(),
            type = type.toDto(),
            likedByMe = likedByMe,
            participatedByMe = participatedByMe,
            attachment = attachment?.toDto(),
            participantsIds = participantsIds,
            link = link,
            likeOwnerIds = likeOwnerIds,
        )

    companion object {
        fun fromDto(dto: Event) =
            EventEntity(
                dto.id,
                dto.authorId,
                dto.author,
                dto.authorAvatar,
                dto.content,
                dto.datetime,
                dto.published,
                CoordinatesEmbeddable.fromDto(dto.coords),
                EventTypeEmbeddable.fromDto(dto.type),
                dto.likedByMe,
                dto.likeOwnerIds,
                dto.link,
                dto.participatedByMe,
                AttachmentEmbeddable.fromDto(dto.attachment),
                dto.participantsIds,
            )
    }
}

fun List<Event>.toEntity(): List<EventEntity> = map(EventEntity.Companion::fromDto)
