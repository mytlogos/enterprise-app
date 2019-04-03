package com.mytlogos.enterprise.background.api.model;

import org.joda.time.DateTime;

public class ClientReadEpisode {
    private int episodeId;
    private DateTime readDate;
    private float progress;

    public ClientReadEpisode(int episodeId, DateTime readDate, float progress) {
        this.episodeId = episodeId;
        this.readDate = readDate;
        this.progress = progress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientReadEpisode that = (ClientReadEpisode) o;

        if (getEpisodeId() != that.getEpisodeId()) return false;
        if (Float.compare(that.getProgress(), getProgress()) != 0) return false;
        return getReadDate() != null ? getReadDate().equals(that.getReadDate()) : that.getReadDate() == null;
    }

    @Override
    public int hashCode() {
        int result = getEpisodeId();
        result = 31 * result + (getReadDate() != null ? getReadDate().hashCode() : 0);
        result = 31 * result + (getProgress() != +0.0f ? Float.floatToIntBits(getProgress()) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientReadEpisode{" +
                "episodeId=" + episodeId +
                ", readDate=" + readDate +
                ", progress=" + progress +
                '}';
    }

    public int getEpisodeId() {
        return episodeId;
    }

    public DateTime getReadDate() {
        return readDate;
    }

    public float getProgress() {
        return progress;
    }
}
