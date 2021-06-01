package com.mytlogos.enterprise.model;

import java.util.List;

public class DisplayEpisode {
    private final int episodeId;
    private final int mediumId;
    private final String mediumTitle;
    private final int totalIndex;
    private final int partialIndex;
    private final boolean saved;
    private final boolean read;
    private final List<Release> releases;

    public DisplayEpisode(int episodeId, int mediumId, String mediumTitle, int totalIndex, int partialIndex, boolean saved, boolean read, List<Release> releases) {
        this.episodeId = episodeId;
        this.mediumId = mediumId;
        this.mediumTitle = mediumTitle;
        this.totalIndex = totalIndex;
        this.partialIndex = partialIndex;
        this.saved = saved;
        this.read = read;
        this.releases = releases;
    }

    public List<Release> getReleases() {
        return releases;
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

    public int getTotalIndex() {
        return totalIndex;
    }

    public int getPartialIndex() {
        return partialIndex;
    }

    public boolean isSaved() {
        return saved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DisplayEpisode that = (DisplayEpisode) o;

        return getEpisodeId() == that.getEpisodeId();
    }

    @Override
    public int hashCode() {
        return getEpisodeId();
    }
}
