package com.mytlogos.enterprise.background.room.model;

import androidx.room.Relation;

import org.joda.time.DateTime;

import java.util.List;

public class RoomTocEpisode {
    private final int episodeId;
    private final float progress;
    private final int partId;
    private final int partialIndex;
    private final int totalIndex;
    private final DateTime readDate;
    private final boolean saved;
    @Relation(parentColumn = "episodeId", entityColumn = "episodeId")
    private final List<RoomRelease> releases;

    public RoomTocEpisode(int episodeId, float progress, int partId, int partialIndex, int totalIndex, DateTime readDate, boolean saved, List<RoomRelease> releases) {
        this.episodeId = episodeId;
        this.progress = progress;
        this.partId = partId;
        this.partialIndex = partialIndex;
        this.totalIndex = totalIndex;
        this.readDate = readDate;
        this.saved = saved;
        this.releases = releases;
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

    public int getPartialIndex() {
        return partialIndex;
    }

    public int getTotalIndex() {
        return totalIndex;
    }

    public DateTime getReadDate() {
        return readDate;
    }

    public boolean isSaved() {
        return saved;
    }

    public List<RoomRelease> getReleases() {
        return releases;
    }
}
