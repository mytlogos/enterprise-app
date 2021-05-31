package com.mytlogos.enterprise.background.room

import androidx.room.TypeConverter
import com.mytlogos.enterprise.Formatter
import org.joda.time.DateTime

object Converters {
    @TypeConverter
    fun fromTimeStamp(s: String?): DateTime? {
        return if (s == null) null else Formatter.parseDateTime(s)
    }

    @TypeConverter
    fun fromDateTime(dateTime: DateTime?): String? {
        return dateTime?.toInstant()?.toString()
    }
}