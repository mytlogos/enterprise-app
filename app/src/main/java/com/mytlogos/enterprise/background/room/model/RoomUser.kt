package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class RoomUser(val name: String, @field:PrimaryKey val uuid: String, val session: String) {

    override fun toString(): String {
        return super.toString() +
                "{" +
                "uuid: " + uuid +
                ", name: " + name +
                "}"
    }

    override fun hashCode(): Int {
        return uuid.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other == null) return false
        if (other !is RoomUser) return false
        return uuid == other.uuid
    }
}