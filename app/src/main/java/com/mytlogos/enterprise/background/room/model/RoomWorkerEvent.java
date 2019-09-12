package com.mytlogos.enterprise.background.room.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;

import org.joda.time.DateTime;

@Entity(
        primaryKeys = {"event", "uuid", "dateTime"},
        indices = {
                @Index(value = "event"),
                @Index(value = "uuid"),
                @Index(value = "dateTime"),
        }
)
public class RoomWorkerEvent {
    private final int event;
    private final String uuid;
    private final String workerName;
    private final String arguments;
    private final DateTime dateTime;

    public RoomWorkerEvent(int event, String uuid, String workerName, String arguments, DateTime dateTime) {
        this.event = event;
        this.uuid = uuid;
        this.workerName = workerName;
        this.arguments = arguments;
        this.dateTime = dateTime;
    }

    public int getEvent() {
        return event;
    }

    public String getWorkerName() {
        return workerName;
    }

    public String getArguments() {
        return arguments;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomWorkerEvent that = (RoomWorkerEvent) o;

        if (getEvent() != that.getEvent()) return false;
        if (getUuid() != null ? !getUuid().equals(that.getUuid()) : that.getUuid() != null)
            return false;
        if (getWorkerName() != null ? !getWorkerName().equals(that.getWorkerName()) : that.getWorkerName() != null)
            return false;
        if (getArguments() != null ? !getArguments().equals(that.getArguments()) : that.getArguments() != null)
            return false;
        return getDateTime() != null ? getDateTime().equals(that.getDateTime()) : that.getDateTime() == null;
    }

    @Override
    public int hashCode() {
        int result = getEvent();
        result = 31 * result + (getUuid() != null ? getUuid().hashCode() : 0);
        result = 31 * result + (getWorkerName() != null ? getWorkerName().hashCode() : 0);
        result = 31 * result + (getArguments() != null ? getArguments().hashCode() : 0);
        result = 31 * result + (getDateTime() != null ? getDateTime().hashCode() : 0);
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "RoomWorkerEvent{" +
                "event=" + event +
                ", uuid='" + uuid + '\'' +
                ", workerName='" + workerName + '\'' +
                ", arguments='" + arguments + '\'' +
                ", dateTime=" + dateTime +
                '}';
    }
}
