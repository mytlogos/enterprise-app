package com.mytlogos.enterprise.background.api.model

import org.joda.time.DateTime

/**
 * API Model for EpisodeRelease.
 */
class ClientEpisodeRelease(
    val episodeId: Int,
    val tocId: Int,
    val title: String,
    val url: String,
    val locked: Boolean,
    val releaseDate: DateTime,
) {

    override fun toString(): String {
        return "ClientRelease{" +
                "id=" + episodeId +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", locked='" + locked + '\'' +
                ", releaseDate=" + releaseDate +
                ", tocId=" + tocId +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as ClientEpisodeRelease
        if (episodeId != that.episodeId) return false
        if (locked != that.locked) return false
        if (title != that.title) return false
        if (url != that.url) return false
        return releaseDate == that.releaseDate
    }

    override fun hashCode(): Int {
        var result = episodeId
        result = 31 * result + title.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + if (locked) 1 else 0
        result = 31 * result + releaseDate.hashCode()
        return result
    }
}