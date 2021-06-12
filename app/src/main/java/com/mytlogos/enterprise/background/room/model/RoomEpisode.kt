package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mytlogos.enterprise.model.Indexable
import org.joda.time.DateTime

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = RoomPart::class,
            parentColumns = ["partId"],
            childColumns = ["partId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["partId"]),
        Index(value = ["episodeId"]),
    ]
)
data class RoomEpisode(
    @field:PrimaryKey val episodeId: Int,
    val progress: Float,
    val readDate: DateTime?,
    val partId: Int,
    override val totalIndex: Int,
    override val partialIndex: Int,
    val combiIndex: Double,
    val saved: Boolean,
) : Indexable {

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