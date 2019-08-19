package com.mytlogos.enterprise.model;

public class SimpleEpisode  {
    private final int episodeId;
    private final int totalIndex;
    private final int partialIndex;
    private final float progress;

    public SimpleEpisode(int episodeId, int totalIndex, int partialIndex, float progress) {
        this.episodeId = episodeId;
        this.totalIndex = totalIndex;
        this.partialIndex = partialIndex;
        this.progress = progress;
    }

    public String getFormattedTitle() {
        if (this.getPartialIndex() > 0) {
            return String.format("#%s.%s", this.getTotalIndex(), this.getPartialIndex());
        } else {
            return String.format("#%s", this.getTotalIndex());
        }
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

    public float getProgress() {
        return progress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleEpisode that = (SimpleEpisode) o;

        return getEpisodeId() == that.getEpisodeId();
    }

    @Override
    public int hashCode() {
        return getEpisodeId();
    }
}
