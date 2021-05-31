package com.mytlogos.enterprise.background.api.model

class ClientUpdateUser(val uuid: String, val name: String, val password: String, val newPassword: String) {
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as ClientUpdateUser
        if (uuid != that.uuid) return false
        if (name != that.name) return false
        if (password != that.password) return false
        return newPassword == that.newPassword
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + newPassword.hashCode()
        return result
    }
}