package com.mytlogos.enterprise.background.api.model;

import java.util.Arrays;

/**
 * API Model for MinPart.
 */
public class ClientPartPure {
    private final int mediumId;
    private final int id;
    private final String title;
    private final int totalIndex;
    private final int partialIndex;

    public ClientPartPure(int mediumId, int id, String title, int totalIndex, int partialIndex) {
        this.mediumId = mediumId;
        this.id = id;
        this.title = title;
        this.totalIndex = totalIndex;
        this.partialIndex = partialIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientPartPure that = (ClientPartPure) o;

        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return getId();
    }

    @Override
    public String toString() {
        return "ClientPart{" +
                "mediumId=" + mediumId +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", totalIndex=" + totalIndex +
                ", partialIndex=" + partialIndex +
                '}';
    }

    public int getId() {
        return id;
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

    public int getMediumId() {
        return mediumId;
    }
}
