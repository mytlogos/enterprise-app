package com.mytlogos.enterprise.background.room

import androidx.room.TypeConverter
import com.mytlogos.enterprise.formatDateTimeIso
import com.mytlogos.enterprise.parseDateTime
import org.joda.time.DateTime

/**
 * Converter Class for [AbstractDatabase].
 */
object Converters {
    @TypeConverter
    fun fromTimeStamp(s: String?): DateTime? {
        return s?.parseDateTime()
    }

    @TypeConverter
    fun fromDateTime(dateTime: DateTime?): String? {
        return dateTime?.formatDateTimeIso()
    }
}