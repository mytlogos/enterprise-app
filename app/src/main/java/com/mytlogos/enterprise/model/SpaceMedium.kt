package com.mytlogos.enterprise.model;

public class SpaceMedium {
    private final int mediumId;
    private final String title;
    private final int savedEpisodes;

    public SpaceMedium(int mediumId, String title, int savedEpisodes) {
        this.mediumId = mediumId;
        this.title = title;
        this.savedEpisodes = savedEpisodes;
    }

    public String getTitle() {
        return title;
    }

    public int getSavedEpisodes() {
        return savedEpisodes;
    }

    public int getMediumId() {
        return mediumId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpaceMedium that = (SpaceMedium) o;

        return getMediumId() == that.getMediumId();
    }

    @Override
    public int hashCode() {
        return getMediumId();
    }
}
