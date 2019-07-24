package com.mytlogos.enterprise.model;

public class MediumInWait {
    private final String title;
    private final int medium;
    private final String link;

    public MediumInWait(String title, int medium, String link) {
        this.title = title;
        this.medium = medium;
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public int getMedium() {
        return medium;
    }

    public String getLink() {
        return link;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MediumInWait that = (MediumInWait) o;

        if (getMedium() != that.getMedium()) return false;
        if (!getTitle().equals(that.getTitle())) return false;
        return getLink().equals(that.getLink());
    }

    @Override
    public int hashCode() {
        int result = getTitle().hashCode();
        result = 31 * result + getMedium();
        result = 31 * result + getLink().hashCode();
        return result;
    }
}
