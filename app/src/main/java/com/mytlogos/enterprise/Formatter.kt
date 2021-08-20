package com.mytlogos.enterprise

import org.joda.time.DateTime
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat

fun DateTime.formatDateTime(): String {
    return DateTimeFormat.forPattern("dd.MM.YYYY HH:mm").print(this)
}

fun DateTime.formatDateTimeIso(): String {
    return this.toInstant().toString()
}

fun String.parseDateTime(): DateTime {
    return Instant.parse(this).toDateTime()
}