package com.mytlogos.enterprise.background.api.model

import org.joda.time.DateTime

/**
 * API Model for PureDisplayRelease.
 */
class ClientRelease(
    val episodeId: Int,
    private val title: String,
    private val url: String,
    val isLocked: Boolean,
    private val releaseDate: DateTime
) {
    fun getTitle(): String {
        return title
    }

    fun getUrl(): String {
        return url
    }

    fun getReleaseDate(): DateTime {
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
        if (getTitle() != that.getTitle()) return false
        if (getUrl() != that.getUrl()) return false
        return getReleaseDate() == that.getReleaseDate()
    }

    override fun hashCode(): Int {
        var result = episodeId
        result = 31 * result + getTitle().hashCode()
        result = 31 * result + getUrl().hashCode()
        result = 31 * result + if (isLocked) 1 else 0
        result = 31 * result + getReleaseDate().hashCode()
        return result
    }
}