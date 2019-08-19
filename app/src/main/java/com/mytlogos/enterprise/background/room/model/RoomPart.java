package com.mytlogos.enterprise.background.room.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.mytlogos.enterprise.model.Part;

import java.util.ArrayList;
import java.util.List;

@Entity(
        foreignKeys = {
                @ForeignKey(
                        entity = RoomMedium.class,
                        onDelete = ForeignKey.SET_NULL,
                        parentColumns = "mediumId",
                        childColumns = "mediumId"
                )
        },
        indices = {
                @Index(value = "mediumId"),
                @Index(value = "partId"),
        }
)
public class RoomPart implements Part {
    @PrimaryKey
    private final int partId;
    private final int mediumId;
    private final String title;
    private final int totalIndex;
    private final int partialIndex;
    private final double combiIndex;

    @Ignore
    private List<Integer> episodes;

    public RoomPart(int partId, int mediumId, String title, int totalIndex, int partialIndex, double combiIndex) {
        this.partId = partId;
        this.mediumId = mediumId;
        this.title = title;
        this.totalIndex = totalIndex;
        this.partialIndex = partialIndex;
        this.combiIndex = combiIndex;
        this.episodes = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomPart roomPart = (RoomPart) o;

        if (getPartId() != roomPart.getPartId()) return false;
        if (getMediumId() != roomPart.getMediumId()) return false;
        if (getTotalIndex() != roomPart.getTotalIndex()) return false;
        if (getPartialIndex() != roomPart.getPartialIndex()) return false;
        if (getTitle() != null ? !getTitle().equals(roomPart.getTitle()) : roomPart.getTitle() != null)
            return false;
        return getEpisodes() != null ? getEpisodes().equals(roomPart.getEpisodes()) : roomPart.getEpisodes() == null;
    }

    @Override
    public int hashCode() {
        int result = getPartId();
        result = 31 * result + getMediumId();
        result = 31 * result + (getTitle() != null ? getTitle().hashCode() : 0);
        result = 31 * result + getTotalIndex();
        result = 31 * result + getPartialIndex();
        result = 31 * result + (getEpisodes() != null ? getEpisodes().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RoomPart{" +
                "partId=" + partId +
                ", mediumId=" + mediumId +
                ", title='" + title + '\'' +
                ", totalIndex=" + totalIndex +
                ", partialIndex=" + partialIndex +
                ", episodes=" + episodes +
                '}';
    }

    public double getCombiIndex() {
        return combiIndex;
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
    public List<Integer> getEpisodes() {
        return episodes;
    }

    public int getMediumId() {
        return mediumId;
    }
}
