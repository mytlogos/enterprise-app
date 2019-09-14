package com.mytlogos.enterprise.background.room.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import com.mytlogos.enterprise.model.Event;

import org.joda.time.DateTime;

@Entity(
        foreignKeys = {
                @ForeignKey(
                        entity = RoomEpisode.class,
                        childColumns = "episodeId",
                        parentColumns = "episodeId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = RoomMedium.class,
                        childColumns = "mediumId",
                        parentColumns = "mediumId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        primaryKeys = {"event", "episodeId", "dateTime"},
        indices = {
                @Index(value = "event"),
                @Index(value = "episodeId"),
                @Index(value = "mediumId"),
                @Index(value = "dateTime"),
        }
)
public class RoomEpisodeEvent {
    @Event.EpisodeEvent
    private final int event;
    private final int episodeId;
    private final int mediumId;
    @NonNull
    private final DateTime dateTime;

    public RoomEpisodeEvent(@Event.EpisodeEvent int event, int episodeId, int mediumId, @NonNull DateTime dateTime) {
        this.event = event;
        this.episodeId = episodeId;
        this.mediumId = mediumId;
        this.dateTime = dateTime;
    }

    @Event.EpisodeEvent
    public int getEvent() {
        return event;
    }

    public int getEpisodeId() {
        return episodeId;
    }

    public int getMediumId() {
        return mediumId;
    }

    @NonNull
    public DateTime getDateTime() {
        return dateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomEpisodeEvent that = (RoomEpisodeEvent) o;

        if (getEvent() != that.getEvent()) return false;
        if (getEpisodeId() != that.getEpisodeId()) return false;
        if (getMediumId() != that.getMediumId()) return false;
        return getDateTime().equals(that.getDateTime());
    }

    @Override
    public int hashCode() {
        int result = getEvent();
        result = 31 * result + getEpisodeId();
        result = 31 * result + getMediumId();
        result = 31 * result + getDateTime().hashCode();
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "RoomEpisodeEvent{" +
                "event=" + event +
                ", episodeId=" + episodeId +
                ", mediumId=" + mediumId +
                ", dateTime=" + dateTime +
                '}';
    }
}
