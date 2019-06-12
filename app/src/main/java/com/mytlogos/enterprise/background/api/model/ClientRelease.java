package com.mytlogos.enterprise.background.api.model;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;

public class ClientRelease {
    private final int episodeId;
    private final String title;
    private final String url;
    private final DateTime releaseDate;

    public ClientRelease(int episodeId, String title, String url, DateTime releaseDate) {
        this.episodeId = episodeId;
        this.title = title;
        this.url = url;
        this.releaseDate = releaseDate;
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

    @NonNull
    @Override
    public String toString() {
        return "ClientRelease{" +
                "id=" + episodeId +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", releaseDate=" + releaseDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientRelease that = (ClientRelease) o;

        if (episodeId != that.episodeId) return false;
        if (!title.equals(that.title)) return false;
        if (!url.equals(that.url)) return false;
        return releaseDate.equals(that.releaseDate);
    }

    @Override
    public int hashCode() {
        int result = episodeId;
        result = 31 * result + title.hashCode();
        result = 31 * result + url.hashCode();
        result = 31 * result + releaseDate.hashCode();
        return result;
    }
}
