package com.mytlogos.enterprise.background.api.model;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;

import java.util.Arrays;

/**
 * API Model for SimpleEpisode.
 */
public class ClientSimpleEpisode {
    private final int id;
    private final int partId;
    private final int totalIndex;
    private final int partialIndex;
    private final double combiIndex;
    private final ClientEpisodeRelease[] releases;

    public ClientSimpleEpisode(int id, int partId, int totalIndex, int partialIndex, double combiIndex, ClientEpisodeRelease[] releases) {
        this.id = id;
        this.partId = partId;
        this.totalIndex = totalIndex;
        this.partialIndex = partialIndex;
        this.combiIndex = combiIndex;
        this.releases = releases;
    }

    public ClientEpisodeRelease[] getReleases() {
        return releases;
    }

    public int getId() {
        return id;
    }

    public int getPartId() {
        return partId;
    }

    public int getTotalIndex() {
        return totalIndex;
    }

    public int getPartialIndex() {
        return partialIndex;
    }

    public double getCombiIndex() {
        return combiIndex;
    }

    @NonNull
    @Override
    public String toString() {
        return "ClientEpisode{" +
                "id=" + id +
                ", partId=" + partId +
                ", totalIndex=" + totalIndex +
                ", partialIndex=" + partialIndex +
                ", combiIndex" + combiIndex +
                ", releases=" + Arrays.toString(releases) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientSimpleEpisode that = (ClientSimpleEpisode) o;
        return id == that.id;
    }


    @Override
    public int hashCode() {
        return id;
    }
}
