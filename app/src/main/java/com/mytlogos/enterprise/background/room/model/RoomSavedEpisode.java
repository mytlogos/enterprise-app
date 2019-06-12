package com.mytlogos.enterprise.background.room.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import java.util.Objects;

@Entity(
        foreignKeys = {
                @ForeignKey(
                        entity = RoomEpisode.class,
                        onDelete = ForeignKey.CASCADE,
                        parentColumns = "id",
                        childColumns = "id"
                )
        },
        primaryKeys = {
                "path", "id"
        },
        indices = {
                @Index(value = "id"),
                @Index(value = "path"),
        }
)
public class RoomSavedEpisode {
    private final String path;
    private final int episodeId;

    public RoomSavedEpisode(@NonNull String path, int episodeId) {
        this.path = path;
        this.episodeId = episodeId;
    }

    public String getPath() {
        return path;
    }

    public int getEpisodeId() {
        return episodeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomSavedEpisode that = (RoomSavedEpisode) o;
        return episodeId == that.episodeId &&
                path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, episodeId);
    }
}
