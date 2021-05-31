package com.mytlogos.enterprise.background.api.model

import org.joda.time.DateTime

/**
 * API Model for PureDisplayRelease.
 */
class ClientRelease(val episodeId: Int, private val title: String, private val url: String, val isLocked: Boolean, private val releaseDate: DateTime) {
    fun getTitle(): String? {
        return title
    }

    fun getUrl(): String? {
        return url
    }

    fun getReleaseDate(): DateTime? {
        return releaseDate
    }

    override fun toString(): String {
        return "ClientRelease{" +
                "id=" + episodeId +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", locked='" + isLocked + '\'' +
                ", releaseDate=" + releaseDate +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as ClientRelease
        if (episodeId != that.episodeId) return false
        if (isLocked != that.isLocked) return false
        if (if (getTitle() != null) getTitle() != that.getTitle() else that.getTitle() != null) return false
        if (if (getUrl() != null) getUrl() != that.getUrl() else that.getUrl() != null) return false
        return if (getReleaseDate() != null) getReleaseDate() == that.getReleaseDate() else that.getReleaseDate() == null
    }

    override fun hashCode(): Int {
        var result = episodeId
        result = 31 * result + if (getTitle() != null) getTitle().hashCode() else 0
        result = 31 * result + if (getUrl() != null) getUrl().hashCode() else 0
        result = 31 * result + if (isLocked) 1 else 0
        result = 31 * result + if (getReleaseDate() != null) getReleaseDate().hashCode() else 0
        return result
    }
}