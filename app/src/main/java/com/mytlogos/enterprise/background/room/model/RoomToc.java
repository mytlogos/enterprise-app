package com.mytlogos.enterprise.background.room.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        primaryKeys = {"mediumId", "link"},
        foreignKeys = {
                @ForeignKey(
                        entity = RoomMedium.class,
                        onDelete = ForeignKey.CASCADE,
                        childColumns = "mediumId",
                        parentColumns = "mediumId"
                )
        },
        indices = {
                @Index("mediumId"),
                @Index("link")
        })
public class RoomToc {
    private final int mediumId;
    @NonNull
    private final String link;

    public RoomToc(int mediumId, @NonNull String link) {
        this.mediumId = mediumId;
        this.link = link;
    }

    public int getMediumId() {
        return mediumId;
    }

    @NonNull
    public String getLink() {
        return link;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomToc roomToc = (RoomToc) o;

        if (getMediumId() != roomToc.getMediumId()) return false;
        return getLink().equals(roomToc.getLink());
    }

    @Override
    public int hashCode() {
        int result = getMediumId();
        result = 31 * result + getLink().hashCode();
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "RoomToc{" +
                "mediumId=" + mediumId +
                ", link='" + link + '\'' +
                '}';
    }
}
