package com.mytlogos.enterprise.background.room.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        foreignKeys = @ForeignKey(
                parentColumns = "mediumId",
                childColumns = "mediumId",
                onDelete = ForeignKey.CASCADE,
                entity = RoomMedium.class
        ),
        indices = {
                @Index(value = "currentReadIndex"),
                @Index(value = "mediumId"),
        }
)
public class RoomMediumProgress {
    @PrimaryKey
    private final int mediumId;
    private final double currentReadIndex;


    public RoomMediumProgress(int mediumId, double currentReadIndex) {
        this.mediumId = mediumId;
        this.currentReadIndex = currentReadIndex;
    }

    public int getMediumId() {
        return mediumId;
    }

    public double getCurrentReadIndex() {
        return currentReadIndex;
    }

    @Override
    public String toString() {
        return "RoomMediumProgress{" +
                "mediumId=" + mediumId +
                ", currentReadIndex=" + currentReadIndex +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomMediumProgress that = (RoomMediumProgress) o;

        if (getMediumId() != that.getMediumId()) return false;
        return Double.compare(that.getCurrentReadIndex(), getCurrentReadIndex()) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = getMediumId();
        temp = Double.doubleToLongBits(getCurrentReadIndex());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
