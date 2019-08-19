package com.mytlogos.enterprise.model;

import java.util.List;

public class ReadEpisode {
    private final int episodeId;
    private final int mediumId;
    private final String mediumTitle;
    private final int totalIndex;
    private final int partialIndex;
    private final List<Release> releases;

    public ReadEpisode(int episodeId, int mediumId, String mediumTitle, int totalIndex, int partialIndex, List<Release> releases) {
        this.episodeId = episodeId;
        this.mediumId = mediumId;
        this.mediumTitle = mediumTitle;
        this.totalIndex = totalIndex;
        this.partialIndex = partialIndex;
        this.releases = releases;
    }

    public List<Release> getReleases() {
        return releases;
    }

    public int getEpisodeId() {
        return episodeId;
    }

    public int getMediumId() {
        return mediumId;
    }

    public String getMediumTitle() {
        return mediumTitle;
    }

    public int getTotalIndex() {
        return totalIndex;
    }

    public int getPartialIndex() {
        return partialIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReadEpisode that = (ReadEpisode) o;

        return getEpisodeId() == that.getEpisodeId();
    }

    @Override
    public int hashCode() {
        return getEpisodeId();
    }
}
