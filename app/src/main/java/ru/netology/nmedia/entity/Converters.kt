package ru.netology.nmedia.entity

import androidx.room.TypeConverter

/** Конвертеры для работы с Set<Long> (лайки, учатники события) */
class Converters {

    @TypeConverter
    fun fromSet(value: Set<Long>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toSet(data: String): Set<Long> {
        return if (data.isBlank()) emptySet() else data.split(",").map(String::toLong).toSet()
    }

}