package com.mytlogos.enterprise.background.api.model

import java.util.*

/**
 * API Model of Part.
 */
class ClientPart(val mediumId: Int, val id: Int, val title: String, val totalIndex: Int, val partialIndex: Int, val episodes: Array<ClientEpisode>) {
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as ClientPart
        return id == that.id
    }

    override fun hashCode(): Int {
        return id
    }

    override fun toString(): String {
        return "ClientPart{" +
                "mediumId=" + mediumId +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", totalIndex=" + totalIndex +
                ", partialIndex=" + partialIndex +
                ", episodes=" + Arrays.toString(episodes) +
                '}'
    }
}