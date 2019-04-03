package com.mytlogos.enterprise.background.room.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.mytlogos.enterprise.model.Episode;

import org.joda.time.DateTime;

@Entity(
        foreignKeys = @ForeignKey(
                entity = RoomPart.class,
                parentColumns = "partId",
                childColumns = "partId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index(value = "partId"),
                @Index(value = "episodeId"),
        }
)
public class RoomEpisode implements Episode {
    @PrimaryKey
    private int episodeId;
    private float progress;
    private DateTime readDate;
    private int partId;
    private String title;
    private int totalIndex;
    private int partialIndex;
    private String url;
    private DateTime releaseDate;

    public RoomEpisode(int episodeId, float progress, DateTime readDate, int partId, String title, int totalIndex, int partialIndex, String url, DateTime releaseDate) {
        this.episodeId = episodeId;
        this.progress = progress;
        this.readDate = readDate;
        this.partId = partId;
        this.title = title;
        this.totalIndex = totalIndex;
        this.partialIndex = partialIndex;
        this.url = url;
        this.releaseDate = releaseDate;
    }


    @Override
    public int getEpisodeId() {
        return episodeId;
    }

    @Override
    public float getProgress() {
        return progress;
    }

    @Override
    public int getPartId() {
        return partId;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getTotalIndex() {
        return totalIndex;
    }

    @Override
    public int getPartialIndex() {
        return partialIndex;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public DateTime getReleaseDate() {
        return releaseDate;
    }

    @Override
    public DateTime getReadDate() {
        return readDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomEpisode that = (RoomEpisode) o;

        if (getEpisodeId() != that.getEpisodeId()) return false;
        if (Float.compare(that.getProgress(), getProgress()) != 0) return false;
        if (getPartId() != that.getPartId()) return false;
        if (getTotalIndex() != that.getTotalIndex()) return false;
        if (getPartialIndex() != that.getPartialIndex()) return false;
        if (getReadDate() != null ? !getReadDate().equals(that.getReadDate()) : that.getReadDate() != null)
            return false;
        if (getTitle() != null ? !getTitle().equals(that.getTitle()) : that.getTitle() != null)
            return false;
        if (getUrl() != null ? !getUrl().equals(that.getUrl()) : that.getUrl() != null)
            return false;
        return getReleaseDate() != null ? getReleaseDate().equals(that.getReleaseDate()) : that.getReleaseDate() == null;
    }

    @Override
    public int hashCode() {
        int result = getEpisodeId();
        result = 31 * result + (getProgress() != +0.0f ? Float.floatToIntBits(getProgress()) : 0);
        result = 31 * result + (getReadDate() != null ? getReadDate().hashCode() : 0);
        result = 31 * result + getPartId();
        result = 31 * result + (getTitle() != null ? getTitle().hashCode() : 0);
        result = 31 * result + getTotalIndex();
        result = 31 * result + getPartialIndex();
        result = 31 * result + (getUrl() != null ? getUrl().hashCode() : 0);
        result = 31 * result + (getReleaseDate() != null ? getReleaseDate().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RoomEpisode{" +
                "episodeId=" + episodeId +
                ", progress=" + progress +
                ", readDate=" + readDate +
                ", partId=" + partId +
                ", title='" + title + '\'' +
                ", totalIndex=" + totalIndex +
                ", partialIndex=" + partialIndex +
                ", url='" + url + '\'' +
                ", releaseDate=" + releaseDate +
                '}';
    }
}
