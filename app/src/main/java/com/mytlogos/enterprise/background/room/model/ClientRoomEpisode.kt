package com.mytlogos.enterprise.background.room.model

import com.mytlogos.enterprise.model.Indexable
import org.joda.time.DateTime

data class ClientRoomEpisode(
    val episodeId: Int,
    val progress: Float,
    val partId: Int,
    override val totalIndex: Int,
    override val partialIndex: Int,
    val combiIndex: Double,
    val readDate: DateTime?
) : Indexable {
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