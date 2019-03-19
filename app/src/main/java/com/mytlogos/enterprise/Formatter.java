package com.mytlogos.enterprise;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;

public class Formatter {
    public static String formatDateTime(DateTime date) {
        return DateTimeFormat.forPattern("dd.MM.YYYY HH:mm").print(date);
    }

    public static DateTime parseDateTime(String isoDate) {
        return Instant.parse(isoDate).toDateTime();
    }
}
