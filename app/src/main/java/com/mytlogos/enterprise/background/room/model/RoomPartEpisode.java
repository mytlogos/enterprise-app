package com.mytlogos.enterprise.background.room.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        foreignKeys = {
                @ForeignKey(
                        parentColumns = "partId",
                        childColumns = "partId",
                        onDelete = ForeignKey.CASCADE,
                        entity = RoomPart.class
                ),
                @ForeignKey(
                        parentColumns = "episodeId",
                        childColumns = "episodeId",
                        onDelete = ForeignKey.CASCADE,
                        entity = RoomEpisode.class
                )
        },
        indices = {
                @Index(value = "partId"),
                @Index(value = "episodeId"),
        },
        primaryKeys = {"episodeId", "partId"}
)
public class RoomPartEpisode {
    private final int episodeId;
    private final int partId;

    public RoomPartEpisode(int partId, int episodeId) {
        this.partId = partId;
        this.episodeId = episodeId;
    }

    public int getEpisodeId() {
        return episodeId;
    }

    public int getPartId() {
        return partId;
    }

    @Override
    public String toString() {
        return "RoomMediumPart{" +
                "episodeId=" + episodeId +
                ", partId=" + partId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomPartEpisode that = (RoomPartEpisode) o;

        if (getEpisodeId() != that.getEpisodeId()) return false;
        return getPartId() == that.getPartId();
    }

    @Override
    public int hashCode() {
        int result = getEpisodeId();
        result = 31 * result + getPartId();
        return result;
    }
}
