package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity
import com.mytlogos.enterprise.background.EditEvent
import org.joda.time.DateTime

@Entity(primaryKeys = ["id", "objectType", "eventType", "dateTime"])
class RoomEditEvent(
    override val id: Int,
    override val objectType: Int,
    override val eventType: Int,
    override val dateTime: DateTime,
    override val firstValue: String,
    override val secondValue: String
) : EditEvent {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as RoomEditEvent
        if (id != that.id) return false
        if (objectType != that.objectType) return false
        if (eventType != that.eventType) return false
        if (dateTime != that.dateTime) return false
        if (firstValue != that.firstValue) return false
        return secondValue == that.secondValue
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + objectType
        result = 31 * result + eventType
        result = 31 * result + dateTime.hashCode()
        result = 31 * result + firstValue.hashCode()
        result = 31 * result + secondValue.hashCode()
        return result
    }

    override fun toString(): String {
        return "RoomEditEvent{" +
                "id=" + id +
                ", objectType=" + objectType +
                ", eventType=" + eventType +
                ", dateTime=" + dateTime +
                ", firstValue='" + firstValue + '\'' +
                ", secondValue='" + secondValue + '\'' +
                '}'
    }
}