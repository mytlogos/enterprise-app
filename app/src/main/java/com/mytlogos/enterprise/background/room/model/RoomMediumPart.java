package com.mytlogos.enterprise.background.room.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        foreignKeys = {
                @ForeignKey(
                        parentColumns = "partId",
                        childColumns = "partId",
                        onDelete = ForeignKey.CASCADE,
                        entity = RoomPart.class
                ),
                @ForeignKey(
                        parentColumns = "mediumId",
                        childColumns = "mediumId",
                        onDelete = ForeignKey.CASCADE,
                        entity = RoomMedium.class
                )
        },
        indices = {
                @Index(value = "partId"),
                @Index(value = "mediumId"),
        },
        primaryKeys = {"mediumId", "partId"}
)
public class RoomMediumPart {
    private final int mediumId;
    private final int partId;

    public RoomMediumPart(int mediumId, int partId) {
        this.mediumId = mediumId;
        this.partId = partId;
    }

    public int getMediumId() {
        return mediumId;
    }

    public int getPartId() {
        return partId;
    }

    @Override
    public String toString() {
        return "RoomMediumPart{" +
                "mediumId=" + mediumId +
                ", partId=" + partId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomMediumPart that = (RoomMediumPart) o;

        if (getMediumId() != that.getMediumId()) return false;
        return getPartId() == that.getPartId();
    }

    @Override
    public int hashCode() {
        int result = getMediumId();
        result = 31 * result + getPartId();
        return result;
    }
}
