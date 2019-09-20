package com.mytlogos.enterprise.background.room.model;

import androidx.room.Relation;

import java.util.List;

public class RoomDisplayEpisode {
    private final int episodeId;
    private final int mediumId;
    private final String mediumTitle;
    private final int totalIndex;
    private final int partialIndex;
    private final boolean saved;
    private final boolean read;
    @Relation(parentColumn = "episodeId", entityColumn = "episodeId")
    private final List<RoomRelease> releases;

    public RoomDisplayEpisode(int episodeId, int mediumId, String mediumTitle, int totalIndex, int partialIndex, boolean saved, boolean read, List<RoomRelease> releases) {
        this.episodeId = episodeId;
        this.mediumId = mediumId;
        this.mediumTitle = mediumTitle;
        this.totalIndex = totalIndex;
        this.partialIndex = partialIndex;
        this.saved = saved;
        this.read = read;
        this.releases = releases;
    }

    public boolean isRead() {
        return read;
    }

    public String getMediumTitle() {
        return mediumTitle;
    }

    public int getMediumId() {
        return mediumId;
    }

    public int getEpisodeId() {
        return episodeId;
    }

    public int getTotalIndex() {
        return totalIndex;
    }

    public int getPartialIndex() {
        return partialIndex;
    }

    public boolean isSaved() {
        return saved;
    }

    public List<RoomRelease> getReleases() {
        return releases;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomDisplayEpisode that = (RoomDisplayEpisode) o;

        return getEpisodeId() == that.getEpisodeId();
    }

    @Override
    public int hashCode() {
        return getEpisodeId();
    }

    @Override
    public String toString() {
        return "RoomDisplayEpisode{" +
                "episodeId=" + episodeId +
                ", mediumId=" + mediumId +
                ", mediumTitle='" + mediumTitle + '\'' +
                ", totalIndex=" + totalIndex +
                ", partialIndex=" + partialIndex +
                ", saved=" + saved +
                ", read=" + read +
                ", releases=" + releases +
                '}';
    }
}
