package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity
import com.mytlogos.enterprise.background.EditEvent
import org.joda.time.DateTime

@Entity(primaryKeys = ["id", "objectType", "eventType", "dateTime"])
class RoomEditEvent(private val id: Int, private val objectType: Int, private val eventType: Int, private val dateTime: DateTime, private val firstValue: String, private val secondValue: String) : EditEvent {
    override fun getId(): Int {
        return id
    }

    override fun getObjectType(): Int {
        return objectType
    }

    override fun getEventType(): Int {
        return eventType
    }

    override fun getDateTime(): DateTime {
        return dateTime
    }

    override fun getFirstValue(): String {
        return firstValue
    }

    override fun getSecondValue(): String {
        return secondValue
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as RoomEditEvent
        if (getId() != that.getId()) return false
        if (getObjectType() != that.getObjectType()) return false
        if (getEventType() != that.getEventType()) return false
        if (getDateTime() != that.getDateTime()) return false
        if (if (getFirstValue() != null) getFirstValue() != that.getFirstValue() else that.getFirstValue() != null) return false
        return if (getSecondValue() != null) getSecondValue() == that.getSecondValue() else that.getSecondValue() == null
    }

    override fun hashCode(): Int {
        var result = getId()
        result = 31 * result + getObjectType()
        result = 31 * result + getEventType()
        result = 31 * result + getDateTime().hashCode()
        result = 31 * result + if (getFirstValue() != null) getFirstValue().hashCode() else 0
        result = 31 * result + if (getSecondValue() != null) getSecondValue().hashCode() else 0
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