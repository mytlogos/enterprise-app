package com.mytlogos.enterprise.model;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;

public class NotificationItem {
    @NonNull
    private String title;
    @NonNull
    private String description;
    @NonNull
    private DateTime dateTime;

    public NotificationItem(@NonNull String title, @NonNull String description, @NonNull DateTime dateTime) {
        this.title = title;
        this.description = description;
        this.dateTime = dateTime;
    }

    public static NotificationItem createNow(String title, String description) {
        return new NotificationItem(title, description, DateTime.now());
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

        NotificationItem that = (NotificationItem) o;

        if (!getTitle().equals(that.getTitle())) return false;
        return getDateTime().equals(that.getDateTime());
    }

    @Override
    public int hashCode() {
        int result = getTitle().hashCode();
        result = 31 * result + getDateTime().hashCode();
        return result;
    }
}
