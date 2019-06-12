package com.mytlogos.enterprise.background.room.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.joda.time.DateTime;

@Entity(
        foreignKeys = @ForeignKey(
                entity = RoomPart.class,
                parentColumns = "partId",
                childColumns = "partId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index(value = "partId"),
                @Index(value = "episodeId"),
        }
)
public class RoomEpisode {
    @PrimaryKey
    private final int episodeId;
    private final float progress;
    private final DateTime readDate;
    private final int partId;
    private final int totalIndex;
    private final int partialIndex;
    private final boolean saved;

    public RoomEpisode(int episodeId, float progress, DateTime readDate, int partId, int totalIndex, int partialIndex, boolean saved) {
        this.episodeId = episodeId;
        this.progress = progress;
        this.readDate = readDate;
        this.partId = partId;
        this.totalIndex = totalIndex;
        this.partialIndex = partialIndex;
        this.saved = saved;
    }

    public int getEpisodeId() {
        return episodeId;
    }

    public float getProgress() {
        return progress;
    }

    public int getPartId() {
        return partId;
    }

    public int getTotalIndex() {
        return totalIndex;
    }

    public int getPartialIndex() {
        return partialIndex;
    }

    public DateTime getReadDate() {
        return readDate;
    }

    public boolean isSaved() {
        return saved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomEpisode that = (RoomEpisode) o;

        return episodeId == that.episodeId;
    }

    @Override
    public int hashCode() {
        return episodeId;
    }

    @NonNull
    @Override
    public String toString() {
        return "RoomEpisode{" +
                "id=" + episodeId +
                ", progress=" + progress +
                ", readDate=" + readDate +
                ", partId=" + partId +
                ", totalIndex=" + totalIndex +
                ", partialIndex=" + partialIndex +
                ", saved=" + saved +
                '}';
    }
}
