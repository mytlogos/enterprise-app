package com.mytlogos.enterprise.background.room.model;

import androidx.annotation.NonNull;

public class RoomTocStat {
    private final int mediumId;
    private final int tocCount;

    public RoomTocStat(int mediumId, int tocCount) {
        this.mediumId = mediumId;
        this.tocCount = tocCount;
    }

    public int getMediumId() {
        return mediumId;
    }

    public int getTocCount() {
        return tocCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomTocStat that = (RoomTocStat) o;

        if (getMediumId() != that.getMediumId()) return false;
        return getTocCount() == that.getTocCount();
    }

    @Override
    public int hashCode() {
        int result = getMediumId();
        result = 31 * result + getTocCount();
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "RoomTocStat{" +
                "mediumId=" + mediumId +
                ", tocCount=" + tocCount +
                '}';
    }
}
