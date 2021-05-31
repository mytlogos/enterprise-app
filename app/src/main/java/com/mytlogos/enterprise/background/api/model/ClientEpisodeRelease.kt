package com.mytlogos.enterprise.background.api.model;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;

/**
 * API Model for EpisodeRelease.
 */
public class ClientEpisodeRelease {
    private final int episodeId;
    private final int tocId;
    private final String title;
    private final String url;
    private final boolean locked;
    private final DateTime releaseDate;

    public ClientEpisodeRelease(int episodeId, int tocId, String title, String url, boolean locked, DateTime releaseDate) {
        this.episodeId = episodeId;
        this.tocId = tocId;
        this.title = title;
        this.url = url;
        this.locked = locked;
        this.releaseDate = releaseDate;
    }

    public boolean isLocked() {
        return locked;
    }

    public int getEpisodeId() {
        return episodeId;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public DateTime getReleaseDate() {
        return releaseDate;
    }

    public int getTocId() {
        return tocId;
    }

    @NonNull
    @Override
    public String toString() {
        return "ClientRelease{" +
                "id=" + episodeId +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", locked='" + locked + '\'' +
                ", releaseDate=" + releaseDate +
                ", tocId=" + tocId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientEpisodeRelease that = (ClientEpisodeRelease) o;

        if (getEpisodeId() != that.getEpisodeId()) return false;
        if (isLocked() != that.isLocked()) return false;
        if (getTitle() != null ? !getTitle().equals(that.getTitle()) : that.getTitle() != null)
            return false;
        if (getUrl() != null ? !getUrl().equals(that.getUrl()) : that.getUrl() != null)
            return false;
        return getReleaseDate() != null ? getReleaseDate().equals(that.getReleaseDate()) : that.getReleaseDate() == null;
    }

    @Override
    public int hashCode() {
        int result = getEpisodeId();
        result = 31 * result + (getTitle() != null ? getTitle().hashCode() : 0);
        result = 31 * result + (getUrl() != null ? getUrl().hashCode() : 0);
        result = 31 * result + (isLocked() ? 1 : 0);
        result = 31 * result + (getReleaseDate() != null ? getReleaseDate().hashCode() : 0);
        return result;
    }
}
