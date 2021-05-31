package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(foreignKeys = [ForeignKey(entity = RoomEpisode::class, parentColumns = arrayOf("episodeId"), childColumns = arrayOf("episodeId"), onDelete = ForeignKey.CASCADE)])
class RoomFailedEpisode(@field:PrimaryKey val episodeId: Int, val failCount: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as RoomFailedEpisode
        return if (episodeId != that.episodeId) false else failCount == that.failCount
    }

    override fun hashCode(): Int {
        var result = episodeId
        result = 31 * result + failCount
        return result
    }
}