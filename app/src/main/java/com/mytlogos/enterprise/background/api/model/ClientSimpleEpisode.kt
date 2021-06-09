package com.mytlogos.enterprise.background.api.model

import java.util.*

/**
 * API Model for SimpleEpisode.
 */
class ClientSimpleEpisode(val id: Int, val partId: Int, val totalIndex: Int, val partialIndex: Int, val combiIndex: Double, val releases: Array<ClientEpisodeRelease>) {
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

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as ClientSimpleEpisode
        return id == that.id
    }

    override fun hashCode(): Int {
        return id
    }
}