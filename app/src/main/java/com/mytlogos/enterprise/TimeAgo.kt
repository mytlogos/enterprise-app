package com.mytlogos.enterprise

import org.joda.time.DateTime
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Modified from [Calculate Relative Time also known as “Time Ago” In Java](https://memorynotfound.com/calculate-relative-time-time-ago-java/)
 */
object TimeAgo {
    private val times: MutableMap<String, Long> = LinkedHashMap()

    init {
        times["year"] = TimeUnit.DAYS.toMillis(365)
        times["month"] = TimeUnit.DAYS.toMillis(30)
        times["week"] = TimeUnit.DAYS.toMillis(7)
        times["day"] = TimeUnit.DAYS.toMillis(1)
        times["hour"] = TimeUnit.HOURS.toMillis(1)
        times["minute"] = TimeUnit.MINUTES.toMillis(1)
        times["second"] = TimeUnit.SECONDS.toMillis(1)
    }

    fun toPastRelative(duration: Long): String {
        val res = StringBuilder()
        for ((key, value) in times) {
            val timeDelta = duration / value

            if (timeDelta > 0) {
                res.append(timeDelta)
                    .append(" ")
                    .append(key)
                    .append(if (timeDelta > 1) "s" else "")
                    .append(", ")
                break
            }
        }
        return if ("" == res.toString()) {
            "Now"
        } else {
            res.setLength(res.length - 2)
            res.append(" ago")
            res.toString()
        }
    }

    fun toFutureRelative(duration: Long): String {
        val res = StringBuilder()
        for ((key, value) in times) {
            val timeDelta = duration / value

            if (timeDelta > 0) {
                res.append(timeDelta)
                    .append(" ")
                    .append(key)
                    .append(if (timeDelta > 1) "s" else "")
                break
            }
        }
        return if ("" == res.toString()) {
            "Now"
        } else {
            res.setLength(res.length - 2)
            "In $res"
        }
    }

    fun toRelative(duration: Long): String {
        return when {
            duration == 0L -> "Now"
            duration > 0 -> toPastRelative(duration)
            else -> toFutureRelative(duration)
        }
    }

    fun toRelative(start: DateTime?, end: DateTime? = DateTime.now()): String? {
        return if (start == null || end == null) {
            null
        } else toRelative(end.millis - start.millis)
    }
}