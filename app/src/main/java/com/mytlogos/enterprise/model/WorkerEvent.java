package com.mytlogos.enterprise.model;

import androidx.annotation.NonNull;
import androidx.room.Ignore;

import org.joda.time.DateTime;

public class WorkerEvent {
    @Event.WorkerEvent
    private final int event;
    private final String uuid;
    private final String workerName;
    private final String arguments;
    private final DateTime dateTime;

    public WorkerEvent(@Event.WorkerEvent int event, String uuid, String workerName, String arguments, DateTime dateTime) {
        this.event = event;
        this.uuid = uuid;
        this.workerName = workerName;
        this.arguments = arguments;
        this.dateTime = dateTime;
    }

    @Ignore
    public WorkerEvent(@Event.WorkerEvent int event, String uuid, String workerName, String arguments) {
        this(event, uuid, workerName, arguments, DateTime.now());
    }

    @Event.WorkerEvent
    public int getEvent() {
        return event;
    }

    public String getUuid() {
        return uuid;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WorkerEvent that = (WorkerEvent) o;

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
        return "WorkerEvent{" +
                "event=" + event +
                ", uuid='" + uuid + '\'' +
                ", workerName='" + workerName + '\'' +
                ", arguments='" + arguments + '\'' +
                ", dateTime=" + dateTime +
                '}';
    }
}
