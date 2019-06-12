package com.mytlogos.enterprise;

import org.joda.time.DateTime;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Modified from <a href="https://memorynotfound.com/calculate-relative-time-time-ago-java/">Calculate Relative Time also known as “Time Ago” In Java</a>
 */
public class TimeAgo {
    private static final Map<String, Long> times = new LinkedHashMap<>();

    static {
        times.put("year", TimeUnit.DAYS.toMillis(365));
        times.put("month", TimeUnit.DAYS.toMillis(30));
        times.put("week", TimeUnit.DAYS.toMillis(7));
        times.put("day", TimeUnit.DAYS.toMillis(1));
        times.put("hour", TimeUnit.HOURS.toMillis(1));
        times.put("minute", TimeUnit.MINUTES.toMillis(1));
        times.put("second", TimeUnit.SECONDS.toMillis(1));
    }

    public static String toPastRelative(long duration) {
        StringBuilder res = new StringBuilder();
        for (Map.Entry<String, Long> time : times.entrySet()) {
            long timeDelta = duration / time.getValue();

            if (timeDelta > 0) {
                res.append(timeDelta)
                        .append(" ")
                        .append(time.getKey())
                        .append(timeDelta > 1 ? "s" : "")
                        .append(", ");

                break;
            }
        }
        if ("".equals(res.toString())) {
            return "Now";
        } else {
            res.setLength(res.length() - 2);
            res.append(" ago");
            return res.toString();
        }
    }

    public static String toFutureRelative(long duration) {
        StringBuilder res = new StringBuilder();

        for (Map.Entry<String, Long> time : times.entrySet()) {
            long timeDelta = duration / time.getValue();

            if (timeDelta > 0) {
                res.append(timeDelta)
                        .append(" ")
                        .append(time.getKey())
                        .append(timeDelta > 1 ? "s" : "");

                break;
            }
        }
        if ("".equals(res.toString())) {
            return "Now";
        } else {
            res.setLength(res.length() - 2);
            return "In " + res.toString();
        }
    }

    public static String toRelative(long duration) {
        if (duration == 0) {
            return "Now";
        } else if (duration > 0) {
            return toPastRelative(duration);
        } else {
            return toFutureRelative(duration);
        }
    }

    public static String toRelative(DateTime start, DateTime end) {
        return toRelative(end.getMillis() - start.getMillis());
    }
}
