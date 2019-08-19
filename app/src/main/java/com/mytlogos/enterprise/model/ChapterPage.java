package com.mytlogos.enterprise.model;

import androidx.annotation.NonNull;

public class ChapterPage {
    private final int episodeId;
    private final int page;
    private final String path;

    public ChapterPage(int episodeId, int page, String path) {
        this.episodeId = episodeId;
        this.page = page;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public int getEpisodeId() {
        return episodeId;
    }

    public int getPage() {
        return page;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChapterPage that = (ChapterPage) o;

        if (episodeId != that.episodeId) return false;
        return page == that.page;
    }

    @Override
    public int hashCode() {
        int result = episodeId;
        result = 31 * result + page;
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "ChapterPage{" +
                "episodeId=" + episodeId +
                ", page=" + page +
                '}';
    }
}
