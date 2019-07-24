package com.mytlogos.enterprise.model;

import org.joda.time.DateTime;

public class DisplayUnreadEpisode {
    private final int episodeId;
    private final int mediumId;
    private final String mediumTitle;
    private final String title;
    private final int totalIndex;
    private final int partialIndex;
    private final String url;
    private final DateTime releaseDate;
    private final boolean saved;
    private final boolean read;

    public DisplayUnreadEpisode(int episodeId, int mediumId, String mediumTitle, String title, int totalIndex, int partialIndex, String url, DateTime releaseDate, boolean saved, boolean read) {
        this.episodeId = episodeId;
        this.mediumId = mediumId;
        this.mediumTitle = mediumTitle;
        this.title = title;
        this.totalIndex = totalIndex;
        this.partialIndex = partialIndex;
        this.url = url;
        this.releaseDate = releaseDate;
        this.saved = saved;
        this.read = read;
    }

    public boolean isRead() {
        return read;
    }

    public String getMediumTitle() {
        return mediumTitle;
    }

    public int getMediumId() {
        return mediumId;
    }

    public int getEpisodeId() {
        return episodeId;
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

    public boolean isSaved() {
        return saved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DisplayUnreadEpisode that = (DisplayUnreadEpisode) o;

        return getEpisodeId() == that.getEpisodeId();
    }

    @Override
    public int hashCode() {
        return getEpisodeId();
    }
}
