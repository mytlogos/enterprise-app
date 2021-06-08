package com.mytlogos.enterprise.model

class User(val uuid: String, val session: String, val name: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val user = other as User
        if (uuid != user.uuid) return false
        return session == user.session
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + session.hashCode()
        return result
    }
}