package com.mytlogos.enterprise.model

import org.joda.time.DateTime

class NotificationItem(val title: String, val description: String, val dateTime: DateTime) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as NotificationItem
        return if (title != that.title) false else dateTime == that.dateTime
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + dateTime.hashCode()
        return result
    }

    companion object {
        fun createNow(title: String, description: String): NotificationItem {
            return NotificationItem(title, description, DateTime.now())
        }
    }
}