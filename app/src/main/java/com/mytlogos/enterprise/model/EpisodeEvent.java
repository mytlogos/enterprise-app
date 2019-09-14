package com.mytlogos.enterprise.model;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;

public class EpisodeEvent {
    @Event.EpisodeEvent
    private final int type;
    private final int episodeId;
    private final int mediumId;
    private final DateTime dateTime;

    public EpisodeEvent(@Event.EpisodeEvent int type, int episodeId, int mediumId, DateTime dateTime) {
        this.type = type;
        this.episodeId = episodeId;
        this.mediumId = mediumId;
        this.dateTime = dateTime;
    }

    public EpisodeEvent(@Event.EpisodeEvent int type, int episodeId, int mediumId) {
        this(type, episodeId, mediumId, DateTime.now());
    }

    @Event.EpisodeEvent
    public int getType() {
        return type;
    }

    public int getEpisodeId() {
        return episodeId;
    }

    public int getMediumId() {
        return mediumId;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EpisodeEvent that = (EpisodeEvent) o;

        if (getType() != that.getType()) return false;
        if (getEpisodeId() != that.getEpisodeId()) return false;
        if (getMediumId() != that.getMediumId()) return false;
        return getDateTime() != null ? getDateTime().equals(that.getDateTime()) : that.getDateTime() == null;
    }

    @Override
    public int hashCode() {
        int result = getType();
        result = 31 * result + getEpisodeId();
        result = 31 * result + getMediumId();
        result = 31 * result + (getDateTime() != null ? getDateTime().hashCode() : 0);
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "EpisodeEvent{" +
                "type=" + type +
                ", episodeId=" + episodeId +
                ", mediumId=" + mediumId +
                ", dateTime=" + dateTime +
                '}';
    }
}
