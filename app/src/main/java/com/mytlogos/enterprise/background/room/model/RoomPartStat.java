package com.mytlogos.enterprise.background.room.model;

public class RoomPartStat {
    public final int partId;
    public final long episodeCount;
    public final long episodeSum;
    public final long releaseCount;

    public RoomPartStat(int partId, long episodeCount, long episodeSum, long releaseCount) {
        this.partId = partId;
        this.episodeCount = episodeCount;
        this.episodeSum = episodeSum;
        this.releaseCount = releaseCount;
    }

    @Override
    public String toString() {
        return "RoomPartStat{" +
                "partId=" + partId +
                ", episodeCount=" + episodeCount +
                ", episodeSum=" + episodeSum +
                ", releaseCount=" + releaseCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomPartStat that = (RoomPartStat) o;

        if (partId != that.partId) return false;
        if (episodeCount != that.episodeCount) return false;
        if (episodeSum != that.episodeSum) return false;
        return releaseCount == that.releaseCount;
    }

    @Override
    public int hashCode() {
        int result = partId;
        result = 31 * result + (int) (episodeCount ^ (episodeCount >>> 32));
        result = 31 * result + (int) (episodeSum ^ (episodeSum >>> 32));
        result = 31 * result + (int) (releaseCount ^ (releaseCount >>> 32));
        return result;
    }
}
