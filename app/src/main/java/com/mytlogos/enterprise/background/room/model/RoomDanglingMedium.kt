package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(foreignKeys = [ForeignKey(entity = RoomMedium::class, parentColumns = arrayOf("mediumId"), childColumns = arrayOf("mediumId"), onDelete = ForeignKey.CASCADE)], indices = [Index(value = arrayOf("mediumId"))])
class RoomDanglingMedium(@field:PrimaryKey val mediumId: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as RoomDanglingMedium
        return mediumId == that.mediumId
    }

    override fun hashCode(): Int {
        return mediumId
    }
}