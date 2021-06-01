package com.mytlogos.enterprise

import org.joda.time.DateTime
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat

object Formatter {
    fun formatDateTime(date: DateTime?): String {
        return DateTimeFormat.forPattern("dd.MM.YYYY HH:mm").print(date)
    }

    fun parseDateTime(isoDate: String?): DateTime {
        return Instant.parse(isoDate).toDateTime()
    }
}