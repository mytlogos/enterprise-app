package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(foreignKeys = [ForeignKey(entity = RoomUser::class, childColumns = arrayOf("userUuid"), parentColumns = arrayOf("uuid"), onDelete = ForeignKey.CASCADE)], indices = [Index(value = arrayOf("uuid")), Index(value = arrayOf("userUuid"))])
class RoomExternalUser(@field:PrimaryKey val uuid: String, val userUuid: String, val identifier: String, val type: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as RoomExternalUser
        if (type != that.type) return false
        if (uuid != that.uuid) return false
        return if (userUuid != that.userUuid) false else identifier == that.identifier
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + userUuid.hashCode()
        result = 31 * result + identifier.hashCode()
        result = 31 * result + type
        return result
    }

    override fun toString(): String {
        return "RoomExternalUser{" +
                "uuid='" + uuid + '\'' +
                ", userUuid='" + userUuid + '\'' +
                ", identifier='" + identifier + '\'' +
                ", type=" + type +
                '}'
    }
}