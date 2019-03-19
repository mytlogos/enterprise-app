package com.mytlogos.enterprise.background.room.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.mytlogos.enterprise.model.Episode;

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
public class RoomEpisode implements Episode {
    @PrimaryKey
    private int episodeId;
    private float progress;
    private DateTime readDate;
    private int partId;
    private String title;
    private int totalIndex;
    private int partialIndex;
    private String url;
    private DateTime releaseDate;

    public RoomEpisode(int episodeId, float progress, DateTime readDate, int partId, String title, int totalIndex, int partialIndex, String url, DateTime releaseDate) {
        this.episodeId = episodeId;
        this.progress = progress;
        this.readDate = readDate;
        this.partId = partId;
        this.title = title;
        this.totalIndex = totalIndex;
        this.partialIndex = partialIndex;
        this.url = url;
        this.releaseDate = releaseDate;
    }


    @Override
    public int getEpisodeId() {
        return episodeId;
    }

    @Override
    public float getProgress() {
        return progress;
    }

    @Override
    public int getPartId() {
        return partId;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getTotalIndex() {
        return totalIndex;
    }

    @Override
    public int getPartialIndex() {
        return partialIndex;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public DateTime getReleaseDate() {
        return releaseDate;
    }

    @Override
    public DateTime getReadDate() {
        return readDate;
    }
}
