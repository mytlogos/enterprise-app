package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(foreignKeys = [ForeignKey(parentColumns = arrayOf("partId"),
        childColumns = arrayOf("partId"),
        onDelete = ForeignKey.CASCADE,
        entity = RoomPart::class), ForeignKey(parentColumns = arrayOf("episodeId"),
        childColumns = arrayOf("episodeId"),
        onDelete = ForeignKey.CASCADE,
        entity = RoomEpisode::class)],
        indices = [Index(value = arrayOf("partId")), Index(value = arrayOf("episodeId"))],
        primaryKeys = ["episodeId", "partId"])
class RoomPartEpisode(val partId: Int, val episodeId: Int) {
    override fun toString(): String {
        return "RoomMediumPart{" +
                "episodeId=" + episodeId +
                ", partId=" + partId +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as RoomPartEpisode
        return if (episodeId != that.episodeId) false else partId == that.partId
    }

    override fun hashCode(): Int {
        var result = episodeId
        result = 31 * result + partId
        return result
    }
}