package com.mytlogos.enterprise.background.room.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import com.mytlogos.enterprise.model.Release;

import org.joda.time.DateTime;

@Entity(
        primaryKeys = {"episodeId", "url"},
        foreignKeys = {
                @ForeignKey(
                        entity = RoomEpisode.class,
                        onDelete = ForeignKey.CASCADE,
                        childColumns = "episodeId",
                        parentColumns = "episodeId"
                )
        },
        indices = {
                @Index("episodeId")
        })
public class RoomRelease implements Release {
    private final int episodeId;
    @NonNull
    private final String title;
    @NonNull
    private final String url;
    @NonNull
    private final DateTime releaseDate;

    private final boolean locked;

    public RoomRelease(int episodeId, @NonNull String title, @NonNull String url, @NonNull DateTime releaseDate, boolean locked) {
        this.episodeId = episodeId;
        this.title = title;
        this.url = url;
        this.releaseDate = releaseDate;
        this.locked = locked;
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    @NonNull
    @Override
    public DateTime getReleaseDate() {
        return releaseDate;
    }

    @NonNull
    @Override
    public String getTitle() {
        return title;
    }

    @NonNull
    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public int getEpisodeId() {
        return episodeId;
    }
}
