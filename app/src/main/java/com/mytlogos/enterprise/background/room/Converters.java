package com.mytlogos.enterprise.background.room;

import androidx.room.TypeConverter;

import com.mytlogos.enterprise.Formatter;

import org.joda.time.DateTime;

public class Converters {

    @TypeConverter
    public static DateTime fromTimeStamp(String s) {
        return s == null ? null : Formatter.parseDateTime(s);
    }

    @TypeConverter
    public static String fromDateTime(DateTime dateTime) {
        return dateTime == null ? null : dateTime.toInstant().toString();
    }
}
