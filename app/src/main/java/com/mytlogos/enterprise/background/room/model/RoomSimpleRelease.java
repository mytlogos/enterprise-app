package com.mytlogos.enterprise.background.room.model;

public class RoomSimpleRelease {
    public final int partId;
    public final int episodeId;
    public final String url;

    public RoomSimpleRelease(int partId, int episodeId, String url) {
        this.partId = partId;
        this.episodeId = episodeId;
        this.url = url;
    }

    @Override
    public String toString() {
        return "RoomSimpleRelease{" +
                "partId=" + partId +
                ", episodeId=" + episodeId +
                ", url='" + url + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomSimpleRelease that = (RoomSimpleRelease) o;

        if (partId != that.partId) return false;
        if (episodeId != that.episodeId) return false;
        return url.equals(that.url);
    }

    @Override
    public int hashCode() {
        int result = partId;
        result = 31 * result + episodeId;
        result = 31 * result + url.hashCode();
        return result;
    }
}
