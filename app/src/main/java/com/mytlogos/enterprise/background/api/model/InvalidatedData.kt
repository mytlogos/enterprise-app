package com.mytlogos.enterprise.background.api.model

import java.util.*

class InvalidatedData(val mediumId: Int, val partId: Int, val newsId: Int, val episodeId: Int, val isUserUuid: Boolean, val externalUuid: String, val externalListId: Int, val listId: Int, val uuid: String) {
    override fun toString(): String {
        return "InvalidatedData{" +
                "mediumId=" + mediumId +
                ", partId=" + partId +
                ", id=" + episodeId +
                ", userUuid=" + isUserUuid +
                ", externalUuid=" + externalUuid +
                ", externalListId=" + externalListId +
                ", listId=" + listId +
                ", newsId=" + newsId +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as InvalidatedData
        return mediumId == that.mediumId && partId == that.partId && newsId == that.newsId && episodeId == that.episodeId && isUserUuid == that.isUserUuid && externalListId == that.externalListId && listId == that.listId &&
                externalUuid == that.externalUuid &&
                uuid == that.uuid
    }

    override fun hashCode(): Int {
        return Objects.hash(mediumId, partId, newsId, episodeId, isUserUuid, externalUuid, externalListId, listId, uuid)
    }
}