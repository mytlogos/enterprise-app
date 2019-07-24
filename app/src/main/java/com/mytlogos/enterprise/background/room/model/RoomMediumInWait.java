package com.mytlogos.enterprise.background.room.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(primaryKeys = {"title", "medium", "link"})
public class RoomMediumInWait {
    @NonNull
    private final String title;
    private final int medium;
    @NonNull
    private final String link;

    public RoomMediumInWait(@NonNull String title, int medium, @NonNull String link) {
        this.title = title;
        this.medium = medium;
        this.link = link;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public int getMedium() {
        return medium;
    }

    @NonNull
    public String getLink() {
        return link;
    }
}
