package com.mytlogos.enterprise.model;

import java.util.List;

public class TocPart {

    private final int partialIndex;
    private final int totalIndex;
    private final String title;
    private final int partId;
    private final List<DisplayUnreadEpisode> episodes;

    public TocPart(int partialIndex, int totalIndex, String title, int partId, List<DisplayUnreadEpisode> episodes) {
        this.partialIndex = partialIndex;
        this.totalIndex = totalIndex;
        this.title = title;
        this.partId = partId;
        this.episodes = episodes;
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

    public List<DisplayUnreadEpisode> getEpisodes() {
        return episodes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TocPart tocPart = (TocPart) o;

        return getPartId() == tocPart.getPartId();
    }

    @Override
    public int hashCode() {
        return getPartId();
    }
}
