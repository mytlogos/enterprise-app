package com.mytlogos.enterprise.background.room.model;

public class RoomProgressComparison {
    private final int mediumId;
    private final double currentReadIndex;
    private final double currentMaxReadIndex;

    public RoomProgressComparison(int mediumId, double currentReadIndex, double currentMaxReadIndex) {
        this.mediumId = mediumId;
        this.currentReadIndex = currentReadIndex;
        this.currentMaxReadIndex = currentMaxReadIndex;
    }

    public int getMediumId() {
        return mediumId;
    }

    public double getCurrentReadIndex() {
        return currentReadIndex;
    }

    public double getCurrentMaxReadIndex() {
        return currentMaxReadIndex;
    }

    @Override
    public String toString() {
        return "RoomProgressComparison{" +
                "mediumId=" + mediumId +
                ", currentReadIndex=" + currentReadIndex +
                ", currentMaxReadIndex=" + currentMaxReadIndex +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomProgressComparison that = (RoomProgressComparison) o;

        if (getMediumId() != that.getMediumId()) return false;
        if (Double.compare(that.getCurrentReadIndex(), getCurrentReadIndex()) != 0)
            return false;
        return Double.compare(that.getCurrentMaxReadIndex(), getCurrentMaxReadIndex()) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = getMediumId();
        temp = Double.doubleToLongBits(getCurrentReadIndex());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getCurrentMaxReadIndex());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
