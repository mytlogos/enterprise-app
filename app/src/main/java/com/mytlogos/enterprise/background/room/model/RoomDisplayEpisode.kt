package com.mytlogos.enterprise.background.room.model

import androidx.room.Relation
import com.mytlogos.enterprise.model.Indexable

data class RoomDisplayEpisode(
    val episodeId: Int,
    val mediumId: Int,
    val mediumTitle: String,
    override val totalIndex: Int,
    override val partialIndex: Int,
    val saved: Boolean,
    val read: Boolean,
    @field:Relation(parentColumn = "episodeId", entityColumn = "episodeId")
    val releases: List<RoomRelease>,
) : Indexable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as RoomDisplayEpisode
        return episodeId == that.episodeId
    }

    override fun hashCode(): Int {
        return episodeId
    }
}