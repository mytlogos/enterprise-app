package com.mytlogos.enterprise.background.room.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;

import com.mytlogos.enterprise.model.Event;

import org.joda.time.DateTime;

@Entity(
        primaryKeys = {"event", "uuid", "dateTime"},
        indices = {
                @Index(value = "event"),
                @Index(value = "uuid"),
                @Index(value = "dateTime"),
                @Index(value = {"event", "uuid"}, unique = true),
        }
)
public class RoomWorkerEvent {
    @Event.WorkerEvent
    private final int event;
    @NonNull
    private final String uuid;
    @NonNull
    private final String workerName;
    private final String arguments;
    @NonNull
    private final DateTime dateTime;

    public RoomWorkerEvent(@Event.WorkerEvent int event, @NonNull String uuid, @NonNull String workerName, String arguments, @NonNull DateTime dateTime) {
        this.event = event;
        this.uuid = uuid;
        this.workerName = workerName;
        this.arguments = arguments;
        this.dateTime = dateTime;
    }

    @Event.WorkerEvent
    public int getEvent() {
        return event;
    }

    @NonNull
    public String getWorkerName() {
        return workerName;
    }

    public String getArguments() {
        return arguments;
    }

    @NonNull
    public DateTime getDateTime() {
        return dateTime;
    }

    @NonNull
    public String getUuid() {
        return uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomWorkerEvent that = (RoomWorkerEvent) o;

        if (getEvent() != that.getEvent()) return false;
        if (!getUuid().equals(that.getUuid()))
            return false;
        if (getWorkerName() != null ? !getWorkerName().equals(that.getWorkerName()) : that.getWorkerName() != null)
            return false;
        if (getArguments() != null ? !getArguments().equals(that.getArguments()) : that.getArguments() != null)
            return false;
        return getDateTime().equals(that.getDateTime());
    }

    @Override
    public int hashCode() {
        int result = getEvent();
        result = 31 * result + getUuid().hashCode();
        result = 31 * result + (getWorkerName() != null ? getWorkerName().hashCode() : 0);
        result = 31 * result + (getArguments() != null ? getArguments().hashCode() : 0);
        result = 31 * result + getDateTime().hashCode();
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
