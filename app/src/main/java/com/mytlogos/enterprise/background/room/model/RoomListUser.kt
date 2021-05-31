package com.mytlogos.enterprise.background.room.model

class RoomListUser(val uuid: String, val listId: Int) {
    override fun toString(): String {
        return "RoomListUser{" +
                "uuid='" + uuid + '\'' +
                ", listId=" + listId +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as RoomListUser
        return if (listId != that.listId) false else uuid == that.uuid
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + listId
        return result
    }
}