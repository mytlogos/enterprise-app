package com.mytlogos.enterprise.model;

public class SimpleMedium {
    private final int mediumId;
    private final String title;
    private final int medium;

    public SimpleMedium(int mediumId, String title, int medium) {
        this.mediumId = mediumId;
        this.title = title;
        this.medium = medium;
    }

    public int getMedium() {
        return medium;
    }

    public int getMediumId() {
        return mediumId;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleMedium that = (SimpleMedium) o;

        return getMediumId() != that.getMediumId();
    }

    @Override
    public int hashCode() {
        return getMediumId();
    }
}
