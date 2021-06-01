package com.mytlogos.enterprise.model;

import org.joda.time.DateTime;

import java.util.List;

public class TocEpisode {
    private final int episodeId;
    private final float progress;
    private final DateTime readDate;
    private final int partId;
    private final int totalIndex;
    private final int partialIndex;
    private final boolean saved;
    private final List<Release> releases;

    public TocEpisode(int episodeId, float progress, int partId, int partialIndex, int totalIndex, DateTime readDate, boolean saved, List<Release> releases) {
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

    public List<Release> getReleases() {
        return releases;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TocEpisode that = (TocEpisode) o;

        return episodeId == that.episodeId;
    }

    @Override
    public int hashCode() {
        return episodeId;
    }
}
