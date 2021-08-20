package com.mytlogos.enterprise.background.api.model

import com.mytlogos.enterprise.model.Indexable

/**
 * API Model for SimpleEpisode.
 */
class ClientSimpleEpisode(
    val id: Int,
    val partId: Int,
    override val totalIndex: Int,
    override val partialIndex: Int,
    val combiIndex: Double,
    val releases: Array<ClientEpisodeRelease>,
): Indexable {
    override fun toString(): String {
        return "ClientEpisode{" +
                "id=" + id +
                ", partId=" + partId +
                ", totalIndex=" + totalIndex +
                ", partialIndex=" + partialIndex +
                ", combiIndex" + combiIndex +
                ", releases=" + releases.contentToString() +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ClientSimpleEpisode
        return id == that.id
    }

    override fun hashCode(): Int {
        return id
    }
}