package com.mytlogos.enterprise.background.api.model

import org.joda.time.DateTime

/**
 * API Model for ReadEpisode in [ClientUser].
 */
class ClientReadEpisode(val episodeId: Int, val readDate: DateTime, val progress: Float) {
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as ClientReadEpisode
        return episodeId == that.episodeId
    }

    override fun hashCode(): Int {
        return episodeId
    }

    override fun toString(): String {
        return "ClientReadEpisode{" +
                "id=" + episodeId +
                ", readDate=" + readDate +
                ", progress=" + progress +
                '}'
    }
}