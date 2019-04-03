package com.mytlogos.enterprise.background.api.model;

public class InvalidatedData {
    private int mediaId;
    private int partId;
    private int episodeId;
    private boolean userUuid;
    private int externalUuid;
    private int externalListId;
    private int listId;

    public InvalidatedData(int mediaId, int partId, int episodeId, boolean userUuid, int externalUuid, int externalListId, int listId) {
        this.mediaId = mediaId;
        this.partId = partId;
        this.episodeId = episodeId;
        this.userUuid = userUuid;
        this.externalUuid = externalUuid;
        this.externalListId = externalListId;
        this.listId = listId;
    }

    @Override
    public String toString() {
        return "InvalidatedData{" +
                "mediaId=" + mediaId +
                ", partId=" + partId +
                ", episodeId=" + episodeId +
                ", userUuid=" + userUuid +
                ", externalUuid=" + externalUuid +
                ", externalListId=" + externalListId +
                ", listId=" + listId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InvalidatedData that = (InvalidatedData) o;

        if (getMediaId() != that.getMediaId()) return false;
        if (getPartId() != that.getPartId()) return false;
        if (getEpisodeId() != that.getEpisodeId()) return false;
        if (isUserUuid() != that.isUserUuid()) return false;
        if (getExternalUuid() != that.getExternalUuid()) return false;
        if (getExternalListId() != that.getExternalListId()) return false;
        return getListId() == that.getListId();
    }

    @Override
    public int hashCode() {
        int result = getMediaId();
        result = 31 * result + getPartId();
        result = 31 * result + getEpisodeId();
        result = 31 * result + (isUserUuid() ? 1 : 0);
        result = 31 * result + getExternalUuid();
        result = 31 * result + getExternalListId();
        result = 31 * result + getListId();
        return result;
    }

    public int getMediaId() {
        return mediaId;
    }

    public int getPartId() {
        return partId;
    }

    public int getEpisodeId() {
        return episodeId;
    }

    public boolean isUserUuid() {
        return userUuid;
    }

    public int getExternalUuid() {
        return externalUuid;
    }

    public int getExternalListId() {
        return externalListId;
    }

    public int getListId() {
        return listId;
    }
}
