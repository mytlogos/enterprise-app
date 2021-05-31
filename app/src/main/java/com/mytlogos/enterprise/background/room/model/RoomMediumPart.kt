package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(foreignKeys = [ForeignKey(parentColumns = arrayOf("partId"),
        childColumns = arrayOf("partId"),
        onDelete = ForeignKey.CASCADE,
        entity = RoomPart::class), ForeignKey(parentColumns = arrayOf("mediumId"),
        childColumns = arrayOf("mediumId"),
        onDelete = ForeignKey.CASCADE,
        entity = RoomMedium::class)], indices = [Index(value = arrayOf("partId")), Index(value = arrayOf("mediumId"))], primaryKeys = ["mediumId", "partId"])
class RoomMediumPart(val mediumId: Int, val partId: Int) {
    override fun toString(): String {
        return "RoomMediumPart{" +
                "mediumId=" + mediumId +
                ", partId=" + partId +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as RoomMediumPart
        return if (mediumId != that.mediumId) false else partId == that.partId
    }

    override fun hashCode(): Int {
        var result = mediumId
        result = 31 * result + partId
        return result
    }
}