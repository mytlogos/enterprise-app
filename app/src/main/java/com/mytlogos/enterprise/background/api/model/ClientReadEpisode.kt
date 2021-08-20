package com.mytlogos.enterprise.background.api.model

import org.joda.time.DateTime

/**
 * API Model for ReadEpisode in [ClientUser].
 */
data class ClientReadEpisode(
    val episodeId: Int,
    val readDate: DateTime,
    val progress: Float,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ClientReadEpisode
        return episodeId == that.episodeId
    }

    override fun hashCode(): Int {
        return episodeId
    }
}