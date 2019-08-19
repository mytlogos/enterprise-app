package com.mytlogos.enterprise.background.room.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;

import org.joda.time.DateTime;

@Entity(primaryKeys = {"title", "dateTime"})
public class RoomNotification {
    @NonNull
    private String title;
    @NonNull
    private String description;
    @NonNull
    private DateTime dateTime;

    public RoomNotification(@NonNull String title, @NonNull String description, @NonNull DateTime dateTime) {
        this.title = title;
        this.description = description;
        this.dateTime = dateTime;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    @NonNull
    public DateTime getDateTime() {
        return dateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomNotification that = (RoomNotification) o;

        if (!getTitle().equals(that.getTitle())) return false;
        if (!getDescription().equals(that.getDescription())) return false;
        return getDateTime().equals(that.getDateTime());
    }

    @Override
    public int hashCode() {
        int result = getTitle().hashCode();
        result = 31 * result + getDescription().hashCode();
        result = 31 * result + getDateTime().hashCode();
        return result;
    }
}
