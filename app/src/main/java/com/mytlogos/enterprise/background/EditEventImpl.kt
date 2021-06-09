package com.mytlogos.enterprise.background

import org.joda.time.DateTime

class EditEventImpl : EditEvent {
    override val id: Int
    override val objectType: Int
    override val eventType: Int
    override val dateTime: DateTime
    override val firstValue: String
    override val secondValue: String

    internal constructor(
        id: Int,
        @EditService.EditObject objectType: Int,
        @EditService.Event eventType: Int,
        dateTime: DateTime,
        firstValue: String,
        secondValue: String
    ) {
        this.id = id
        this.objectType = objectType
        this.eventType = eventType
        this.dateTime = dateTime
        this.firstValue = firstValue
        this.secondValue = secondValue
    }

    internal constructor(
        id: Int,
        @EditService.EditObject objectType: Int,
        @EditService.Event eventType: Int,
        dateTime: DateTime,
        firstValue: Any?,
        secondValue: Any?
    ) {
        this.id = id
        this.objectType = objectType
        this.eventType = eventType
        this.dateTime = dateTime
        this.firstValue = firstValue.toString()
        this.secondValue = secondValue.toString()
    }

    constructor(
        id: Int,
        objectType: Int,
        eventType: Int,
        firstValue: Any?,
        secondValue: Any?
    ) : this(id, objectType, eventType, DateTime.now(), firstValue, secondValue)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val editEvent = other as EditEvent
        if (id != editEvent.id) return false
        if (objectType != editEvent.objectType) return false
        if (eventType != editEvent.eventType) return false
        if (dateTime != editEvent.dateTime) return false
        if (firstValue != editEvent.firstValue) return false
        return secondValue == editEvent.secondValue
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
        return "EditEventImpl{" +
                "id=" + id +
                ", objectType=" + objectType +
                ", eventType=" + eventType +
                ", dateTime=" + dateTime +
                ", firstValue='" + firstValue + '\'' +
                ", secondValue='" + secondValue + '\'' +
                '}'
    }
}