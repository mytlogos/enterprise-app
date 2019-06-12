package com.mytlogos.enterprise.background.api.model;

import androidx.annotation.NonNull;

import java.util.Objects;

public class InvalidatedData {
    private int mediaId;
    private int partId;
    private int newsId;
    private int episodeId;
    private boolean userUuid;
    private String externalUuid;
    private int externalListId;
    private int listId;
    private String uuid;

    public InvalidatedData(int mediaId, int partId, int newsId, int episodeId, boolean userUuid, String externalUuid, int externalListId, int listId, String uuid) {
        this.mediaId = mediaId;
        this.partId = partId;
        this.newsId = newsId;
        this.episodeId = episodeId;
        this.userUuid = userUuid;
        this.externalUuid = externalUuid;
        this.externalListId = externalListId;
        this.listId = listId;
        this.uuid = uuid;
    }

    @NonNull
    @Override
    public String toString() {
        return "InvalidatedData{" +
                "mediaId=" + mediaId +
                ", partId=" + partId +
                ", id=" + episodeId +
                ", userUuid=" + userUuid +
                ", externalUuid=" + externalUuid +
                ", externalListId=" + externalListId +
                ", listId=" + listId +
                ", newsId=" + newsId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvalidatedData that = (InvalidatedData) o;
        return mediaId == that.mediaId &&
                partId == that.partId &&
                newsId == that.newsId &&
                episodeId == that.episodeId &&
                userUuid == that.userUuid &&
                externalListId == that.externalListId &&
                listId == that.listId &&
                Objects.equals(externalUuid, that.externalUuid) &&
                Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mediaId, partId, newsId, episodeId, userUuid, externalUuid, externalListId, listId, uuid);
    }

    public int getNewsId() {
        return newsId;
    }

    public String getUuid() {
        return uuid;
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

    public String getExternalUuid() {
        return externalUuid;
    }

    public int getExternalListId() {
        return externalListId;
    }

    public int getListId() {
        return listId;
    }
}
