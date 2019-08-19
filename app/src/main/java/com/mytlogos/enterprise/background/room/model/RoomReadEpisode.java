package com.mytlogos.enterprise.background.room.model;

import androidx.room.Relation;

import java.util.List;

public class RoomReadEpisode {
    private final int episodeId;
    private final int mediumId;
    private final String mediumTitle;
    private final int totalIndex;
    private final int partialIndex;

    @Relation(parentColumn = "episodeId", entityColumn = "episodeId")
    private final List<RoomRelease> releases;

    public RoomReadEpisode(int episodeId, int mediumId, String mediumTitle, int totalIndex, int partialIndex, List<RoomRelease> releases) {
        this.episodeId = episodeId;
        this.mediumId = mediumId;
        this.mediumTitle = mediumTitle;
        this.totalIndex = totalIndex;
        this.partialIndex = partialIndex;
        this.releases = releases;
    }

    public int getMediumId() {
        return mediumId;
    }

    public String getMediumTitle() {
        return mediumTitle;
    }

    public int getTotalIndex() {
        return totalIndex;
    }

    public int getPartialIndex() {
        return partialIndex;
    }

    public List<RoomRelease> getReleases() {
        return releases;
    }

    public int getEpisodeId() {
        return episodeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomReadEpisode that = (RoomReadEpisode) o;

        return getEpisodeId() == that.getEpisodeId();
    }

    @Override
    public int hashCode() {
        return getEpisodeId();
    }
}
