package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.joda.time.DateTime

@Entity(
        foreignKeys = [
            ForeignKey(entity = RoomPart::class, parentColumns = arrayOf("partId"), childColumns = arrayOf("partId"), onDelete = ForeignKey.CASCADE)
                      ],
        indices = [
            Index(value = arrayOf("partId")), Index(value = arrayOf("episodeId"))
        ]
)
data class RoomEpisode(
        @field:PrimaryKey val episodeId: Int,
        val progress: Float,
        val readDate: DateTime?,
        val partId: Int,
        val totalIndex: Int,
        val partialIndex: Int,
        val combiIndex: Double,
        val saved: Boolean
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as RoomEpisode
        return episodeId == that.episodeId
    }

    override fun hashCode(): Int {
        return episodeId
    }
}