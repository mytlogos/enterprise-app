package com.mytlogos.enterprise.background.room.model

import androidx.room.*
import com.mytlogos.enterprise.model.Part
import java.util.*

@Entity(foreignKeys = [ForeignKey(entity = RoomMedium::class,
        onDelete = ForeignKey.SET_NULL,
        parentColumns = arrayOf("mediumId"),
        childColumns = arrayOf("mediumId"))],
        indices = [Index(value = arrayOf("mediumId")), Index(value = arrayOf("partId"))])
class RoomPart(
    @field:PrimaryKey override val partId: Int,
    val mediumId: Int,
    override val title: String,
    override val totalIndex: Int,
    override val partialIndex: Int,
    val combiIndex: Double,
) : Part {

    @Ignore
    override val episodes: List<Int>

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val roomPart = other as RoomPart
        if (partId != roomPart.partId) return false
        if (mediumId != roomPart.mediumId) return false
        if (totalIndex != roomPart.totalIndex) return false
        if (partialIndex != roomPart.partialIndex) return false
        if (title != roomPart.title) return false
        return episodes == roomPart.episodes
    }

    override fun hashCode(): Int {
        var result = partId
        result = 31 * result + mediumId
        result = 31 * result + title.hashCode()
        result = 31 * result + totalIndex
        result = 31 * result + partialIndex
        result = 31 * result + episodes.hashCode()
        return result
    }

    override fun toString(): String {
        return "RoomPart{" +
                "partId=" + partId +
                ", mediumId=" + mediumId +
                ", title='" + title + '\'' +
                ", totalIndex=" + totalIndex +
                ", partialIndex=" + partialIndex +
                ", episodes=" + episodes +
                '}'
    }

    init {
        episodes = ArrayList()
    }
}