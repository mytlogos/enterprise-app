package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(foreignKeys = [ForeignKey(parentColumns = arrayOf("mediumId"),
        childColumns = arrayOf("mediumId"),
        onDelete = ForeignKey.CASCADE,
        entity = RoomMedium::class)], indices = [Index(value = arrayOf("currentReadIndex")), Index(value = arrayOf("mediumId"))])
class RoomMediumProgress(@field:PrimaryKey val mediumId: Int, val currentReadIndex: Double) {
    override fun toString(): String {
        return "RoomMediumProgress{" +
                "mediumId=" + mediumId +
                ", currentReadIndex=" + currentReadIndex +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as RoomMediumProgress
        return if (mediumId != that.mediumId) false else that.currentReadIndex.compareTo(
            currentReadIndex) == 0
    }

    override fun hashCode(): Int {
        var result: Int = mediumId
        val temp: Long = java.lang.Double.doubleToLongBits(currentReadIndex)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        return result
    }
}