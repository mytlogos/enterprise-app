package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity
import org.joda.time.DateTime

@Entity(primaryKeys = ["title", "dateTime"])
class RoomNotification(val title: String, val description: String, val dateTime: DateTime) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as RoomNotification
        if (title != that.title) return false
        return if (description != that.description) false else dateTime == that.dateTime
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + dateTime.hashCode()
        return result
    }
}