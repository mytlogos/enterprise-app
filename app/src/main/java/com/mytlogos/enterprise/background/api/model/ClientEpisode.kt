package com.mytlogos.enterprise.background.api.model

import org.joda.time.DateTime

/**
 * API Model for Episode.
 */
class ClientEpisode(
    val id: Int,
    val progress: Float,
    val partId: Int,
    val totalIndex: Int,
    val partialIndex: Int,
    val combiIndex: Double,
    val readDate: DateTime?,
    val releases: Array<ClientEpisodeRelease>
) {
    override fun toString(): String {
        return "ClientEpisode{" +
                "id=" + id +
                ", progress=" + progress +
                ", partId=" + partId +
                ", totalIndex=" + totalIndex +
                ", partialIndex=" + partialIndex +
                ", combiIndex" + combiIndex +
                ", readDate=" + readDate +
                ", releases=" + releases.contentToString() +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ClientEpisode
        return id == that.id
    }

    override fun hashCode(): Int {
        return id
    }
}