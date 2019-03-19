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
