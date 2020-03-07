package com.mytlogos.enterprise.background.room.model;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;

public class ClientRoomEpisode {
    private final int episodeId;
    private final float progress;
    private final int partId;
    private final int totalIndex;
    private final int partialIndex;
    private final double combiIndex;
    private final DateTime readDate;

    public ClientRoomEpisode(int episodeId, float progress, int partId, int totalIndex, int partialIndex, double combiIndex, DateTime readDate) {
        this.episodeId = episodeId;
        this.progress = progress;
        this.partId = partId;
        this.totalIndex = totalIndex;
        this.partialIndex = partialIndex;
        this.combiIndex = combiIndex;
        this.readDate = readDate;
    }

    public DateTime getReadDate() {
        return readDate;
    }

    public int getEpisodeId() {
        return episodeId;
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

    public float getProgress() {
        return progress;
    }

    public double getCombiIndex() {
        return combiIndex;
    }

    @NonNull
    @Override
    public String toString() {
        return "ClientRoomEpisode{" +
                "episodeId=" + episodeId +
                ", progress=" + progress +
                ", partId=" + partId +
                ", totalIndex=" + totalIndex +
                ", partialIndex=" + partialIndex +
                ", combiIndex=" + combiIndex +
                ", readDate=" + readDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientRoomEpisode that = (ClientRoomEpisode) o;

        return getEpisodeId() == that.getEpisodeId();
    }

    @Override
    public int hashCode() {
        return getEpisodeId();
    }
}
