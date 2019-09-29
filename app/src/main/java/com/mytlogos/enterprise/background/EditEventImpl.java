package com.mytlogos.enterprise.background;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;

public class EditEventImpl implements EditEvent {
    private final int id;
    private final int objectType;
    private final int eventType;
    @NonNull
    private final DateTime dateTime;
    private final String firstValue;
    private final String secondValue;

    EditEventImpl(int id, @EditService.EditObject int objectType, @EditService.Event int eventType, @NonNull DateTime dateTime, String firstValue, String secondValue) {
        this.id = id;
        this.objectType = objectType;
        this.eventType = eventType;
        this.dateTime = dateTime;
        this.firstValue = firstValue;
        this.secondValue = secondValue;
    }

    EditEventImpl(int id, @EditService.EditObject int objectType, @EditService.Event int eventType, @NonNull DateTime dateTime, Object firstValue, Object secondValue) {
        this.id = id;
        this.objectType = objectType;
        this.eventType = eventType;
        this.dateTime = dateTime;
        this.firstValue = String.valueOf(firstValue);
        this.secondValue = String.valueOf(secondValue);
    }

    public EditEventImpl(int id, int objectType, int eventType, Object firstValue, Object secondValue) {
        this(id, objectType, eventType, DateTime.now(), firstValue, secondValue);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getObjectType() {
        return objectType;
    }

    @Override
    public int getEventType() {
        return eventType;
    }

    @Override
    @NonNull
    public DateTime getDateTime() {
        return dateTime;
    }

    @Override
    public String getFirstValue() {
        return firstValue;
    }

    @Override
    public String getSecondValue() {
        return secondValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EditEvent editEvent = (EditEvent) o;

        if (getId() != editEvent.getId()) return false;
        if (getObjectType() != editEvent.getObjectType()) return false;
        if (getEventType() != editEvent.getEventType()) return false;
        if (!getDateTime().equals(editEvent.getDateTime())) return false;
        if (getFirstValue() != null ? !getFirstValue().equals(editEvent.getFirstValue()) : editEvent.getFirstValue() != null)
            return false;
        return getSecondValue() != null ? getSecondValue().equals(editEvent.getSecondValue()) : editEvent.getSecondValue() == null;
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
        return "EditEventImpl{" +
                "id=" + id +
                ", objectType=" + objectType +
                ", eventType=" + eventType +
                ", dateTime=" + dateTime +
                ", firstValue='" + firstValue + '\'' +
                ", secondValue='" + secondValue + '\'' +
                '}';
    }
}
