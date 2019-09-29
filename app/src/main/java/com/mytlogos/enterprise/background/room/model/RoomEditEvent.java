package com.mytlogos.enterprise.background.room.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;

import com.mytlogos.enterprise.background.EditEvent;

import org.joda.time.DateTime;

@Entity(
        primaryKeys = {"id", "objectType", "eventType", "dateTime"}
)
public class RoomEditEvent implements EditEvent {
    private final int id;
    private final int objectType;
    private final int eventType;
    @NonNull
    private final DateTime dateTime;
    private final String firstValue;
    private final String secondValue;

    public RoomEditEvent(int id, int objectType, int eventType, @NonNull DateTime dateTime, String firstValue, String secondValue) {
        this.id = id;
        this.objectType = objectType;
        this.eventType = eventType;
        this.dateTime = dateTime;
        this.firstValue = firstValue;
        this.secondValue = secondValue;
    }

    public int getId() {
        return id;
    }

    public int getObjectType() {
        return objectType;
    }

    public int getEventType() {
        return eventType;
    }

    @NonNull
    public DateTime getDateTime() {
        return dateTime;
    }

    public String getFirstValue() {
        return firstValue;
    }

    public String getSecondValue() {
        return secondValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomEditEvent that = (RoomEditEvent) o;

        if (getId() != that.getId()) return false;
        if (getObjectType() != that.getObjectType()) return false;
        if (getEventType() != that.getEventType()) return false;
        if (!getDateTime().equals(that.getDateTime()))
            return false;
        if (getFirstValue() != null ? !getFirstValue().equals(that.getFirstValue()) : that.getFirstValue() != null)
            return false;
        return getSecondValue() != null ? getSecondValue().equals(that.getSecondValue()) : that.getSecondValue() == null;
    }

    @Override
    public int hashCode() {
        int result = getId();
        result = 31 * result + getObjectType();
        result = 31 * result + getEventType();
        result = 31 * result + getDateTime().hashCode();
        result = 31 * result + (getFirstValue() != null ? getFirstValue().hashCode() : 0);
        result = 31 * result + (getSecondValue() != null ? getSecondValue().hashCode() : 0);
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "RoomEditEvent{" +
                "id=" + id +
                ", objectType=" + objectType +
                ", eventType=" + eventType +
                ", dateTime=" + dateTime +
                ", firstValue='" + firstValue + '\'' +
                ", secondValue='" + secondValue + '\'' +
                '}';
    }
}
