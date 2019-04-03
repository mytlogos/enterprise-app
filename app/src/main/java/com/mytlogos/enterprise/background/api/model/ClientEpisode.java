package com.mytlogos.enterprise.background.api.model;

import org.joda.time.DateTime;

public class ClientEpisode {
    private int id;
    private float progress;
    private DateTime readDate;
    private int partId;
    private String title;
    private int totalIndex;
    private int partialIndex;
    private String url;
    private DateTime releaseDate;

    public ClientEpisode(int id, float progress, DateTime readDate, int partId, String title, int totalIndex, int partialIndex,
                         String url, DateTime releaseDate) {
        this.id = id;
        this.progress = progress;
        this.readDate = readDate;
        this.partId = partId;
        this.title = title;
        this.totalIndex = totalIndex;
        this.partialIndex = partialIndex;
        this.url = url;
        this.releaseDate = releaseDate;
    }

    public int getId() {
        return id;
    }

    public int getPartId() {
        return partId;
    }

    public String getTitle() {
        return title;
    }

    public int getTotalIndex() {
        return totalIndex;
    }

    public int getPartialIndex() {
        return partialIndex;
    }

    public String getUrl() {
        return url;
    }

    public DateTime getReleaseDate() {
        return releaseDate;
    }

    public float getProgress() {
        return progress;
    }

    public DateTime getReadDate() {
        return readDate;
    }

    @Override
    public String toString() {
        return "ClientEpisode{" +
                "id=" + id +
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientEpisode that = (ClientEpisode) o;

        if (getId() != that.getId()) return false;
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
        int result = getId();
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
}
