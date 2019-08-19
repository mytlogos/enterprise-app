package com.mytlogos.enterprise.background.room.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = @ForeignKey(
        entity = RoomEpisode.class,
        parentColumns = "episodeId",
        childColumns = "episodeId",
        onDelete = ForeignKey.CASCADE
))
public class RoomFailedEpisode {
    @PrimaryKey
    private final int episodeId;
    private final int failCount;

    public RoomFailedEpisode(int episodeId, int failCount) {
        this.episodeId = episodeId;
        this.failCount = failCount;
    }

    public int getEpisodeId() {
        return episodeId;
    }

    public int getFailCount() {
        return failCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomFailedEpisode that = (RoomFailedEpisode) o;

        if (getEpisodeId() != that.getEpisodeId()) return false;
        return getFailCount() == that.getFailCount();
    }

    @Override
    public int hashCode() {
        int result = getEpisodeId();
        result = 31 * result + getFailCount();
        return result;
    }
}
