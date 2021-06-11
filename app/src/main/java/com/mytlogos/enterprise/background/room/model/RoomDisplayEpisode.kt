package com.mytlogos.enterprise.background.room.model

import androidx.room.Relation

data class RoomDisplayEpisode(
    val episodeId: Int,
    val mediumId: Int,
    val mediumTitle: String,
    val totalIndex: Int,
    val partialIndex: Int,
    val saved: Boolean,
    val read: Boolean,
    @field:Relation(parentColumn = "episodeId", entityColumn = "episodeId")
    val releases: List<RoomRelease>,
) {

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