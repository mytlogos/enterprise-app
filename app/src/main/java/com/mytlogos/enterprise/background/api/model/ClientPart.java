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

        if (getMediumId() != that.getMediumId()) return false;
        if (getId() != that.getId()) return false;
        if (getTotalIndex() != that.getTotalIndex()) return false;
        if (getPartialIndex() != that.getPartialIndex()) return false;
        if (getTitle() != null ? !getTitle().equals(that.getTitle()) : that.getTitle() != null)
            return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(getEpisodes(), that.getEpisodes());
    }

    @Override
    public int hashCode() {
        int result = getMediumId();
        result = 31 * result + getId();
        result = 31 * result + (getTitle() != null ? getTitle().hashCode() : 0);
        result = 31 * result + getTotalIndex();
        result = 31 * result + getPartialIndex();
        result = 31 * result + Arrays.hashCode(getEpisodes());
        return result;
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
