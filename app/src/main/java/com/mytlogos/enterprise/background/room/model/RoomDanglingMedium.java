package com.mytlogos.enterprise.background.room.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        foreignKeys = @ForeignKey(
                entity = RoomMedium.class,
                parentColumns = "mediumId",
                childColumns = "mediumId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index(value = "mediumId"),
        }
)
public class RoomDanglingMedium {
    @PrimaryKey
    private final int mediumId;

    public RoomDanglingMedium(int mediumId) {
        this.mediumId = mediumId;
    }

    public int getMediumId() {
        return mediumId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomDanglingMedium that = (RoomDanglingMedium) o;

        return getMediumId() == that.getMediumId();
    }

    @Override
    public int hashCode() {
        return getMediumId();
    }
}
