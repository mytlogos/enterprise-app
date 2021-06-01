package com.mytlogos.enterprise.model;

public class FailedEpisode {
    private final int episodeId;
    private final int failCount;

    public FailedEpisode(int episodeId, int failCount) {
        this.episodeId = episodeId;
        this.failCount = failCount;
    }

    public int getEpisodeId() {
        return episodeId;
    }

    public int getFailCount() {
        return failCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FailedEpisode that = (FailedEpisode) o;

        return getEpisodeId() == that.getEpisodeId();
    }

    @Override
    public int hashCode() {
        return getEpisodeId();
    }
}
