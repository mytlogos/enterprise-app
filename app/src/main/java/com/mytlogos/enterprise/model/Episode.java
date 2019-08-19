package com.mytlogos.enterprise.model;

import org.joda.time.DateTime;

public class Episode  {
    private final int episodeId;
    private final float progress;
    private final int partId;
    private final int partialIndex;
    private final int totalIndex;
    private final DateTime readDate;
    private final boolean saved;

    public Episode(int episodeId, float progress, int partId, int partialIndex, int totalIndex, DateTime readDate, boolean saved) {
        this.episodeId = episodeId;
        this.progress = progress;
        this.partId = partId;
        this.partialIndex = partialIndex;
        this.totalIndex = totalIndex;
        this.readDate = readDate;
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
}
