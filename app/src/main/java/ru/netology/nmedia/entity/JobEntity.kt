package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Job

@Entity
class JobEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val position: String,
    val start: Long,
    val finish: Long? = null,
    val link: String? = null,
    /** Для того, чтобы хранить информацию о работе разных пользователей */
    val userId: Long? = null,
) {
    fun toDto() =
        Job(
            id = id,
            name = name,
            position = position,
            start = start,
            finish = finish,
            link = link,
        )

    companion object {
        fun fromDto(dto: Job, userId: Long) =
            JobEntity(
                id = dto.id,
                name = dto.name,
                position = dto.position,
                start = dto.start,
                finish = dto.finish,
                link = dto.link,
                userId = userId,
            )
    }
}

fun List<Job>.toEntity(userId: Long): List<JobEntity> = map { JobEntity.fromDto(it, userId) }
fun List<JobEntity>.toDto(): List<Job> = map { it.toDto() }