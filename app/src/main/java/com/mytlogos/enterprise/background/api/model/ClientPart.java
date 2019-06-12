package com.mytlogos.enterprise.background.api.model;

import java.util.Arrays;

public class ClientPart {
    private int mediumId;
    private int id;
    private String title;
    private int totalIndex;
    private int partialIndex;
    private ClientEpisode[] episodes;

    public ClientPart(int mediumId, int id, String title, int totalIndex, int partialIndex, ClientEpisode[] episodes) {
        this.mediumId = mediumId;
        this.id = id;
        this.title = title;
        this.totalIndex = totalIndex;
        this.partialIndex = partialIndex;
        this.episodes = episodes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientPart that = (ClientPart) o;

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
                ", episodes=" + Arrays.toString(episodes) +
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

    public ClientEpisode[] getEpisodes() {
        return episodes;
    }

    public int getMediumId() {
        return mediumId;
    }
}
