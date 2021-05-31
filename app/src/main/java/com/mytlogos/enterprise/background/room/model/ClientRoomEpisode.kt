package com.mytlogos.enterprise.background.room.model

import org.joda.time.DateTime

class ClientRoomEpisode(
    val episodeId: Int,
    val progress: Float,
    val partId: Int,
    val totalIndex: Int,
    val partialIndex: Int,
    val combiIndex: Double,
    val readDate: DateTime?
) {
    override fun toString(): String {
        return "ClientRoomEpisode{" +
                "episodeId=" + episodeId +
                ", progress=" + progress +
                ", partId=" + partId +
                ", totalIndex=" + totalIndex +
                ", partialIndex=" + partialIndex +
                ", combiIndex=" + combiIndex +
                ", readDate=" + readDate +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ClientRoomEpisode
        return episodeId == that.episodeId
    }

    override fun hashCode(): Int {
        return episodeId
    }
}