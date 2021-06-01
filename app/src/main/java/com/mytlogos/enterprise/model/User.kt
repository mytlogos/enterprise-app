package com.mytlogos.enterprise.model

class User(val uuid: String, val session: String, val name: String) {
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val user = o as User
        if (uuid != user.uuid) return false
        return session == user.session
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + session.hashCode()
        return result
    }
}