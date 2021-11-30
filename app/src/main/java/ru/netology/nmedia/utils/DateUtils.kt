package ru.netology.nmedia.utils

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

object DateUtils {
    fun Long.convertToDate(): String {
        val date = this
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return format.format(date)
    }

    fun String.convertToTimestamp(): Long? {
        val date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(this)
        return date?.time
    }

    fun String.convertToInstant(): String {
        val date = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).parse(this);
        val formatter = DateTimeFormatter.ISO_INSTANT.withZone(
            TimeZone.getDefault().toZoneId() ?: ZoneId.of("Europe/Moscow")
        )
        return formatter.format(date?.toInstant())
    }

    fun String.convertToDateTime(): String? {
        return DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm ")
            .withZone(TimeZone.getDefault().toZoneId() ?: ZoneId.of("Europe/Moscow"))
            .format(Instant.parse(this))
    }
}