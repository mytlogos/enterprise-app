package com.mytlogos.enterprise.background.api.model

class ClientSimpleUser(private val uuid: String, private val session: String, private val name: String) {
    fun getUuid(): String? {
        return uuid
    }

    fun getSession(): String? {
        return session
    }

    fun getName(): String? {
        return name
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as ClientSimpleUser
        if (if (getUuid() != null) getUuid() != that.getUuid() else that.getUuid() != null) return false
        if (if (getSession() != null) getSession() != that.getSession() else that.getSession() != null) return false
        return if (getName() != null) getName() == that.getName() else that.getName() == null
    }

    override fun hashCode(): Int {
        var result = if (getUuid() != null) getUuid().hashCode() else 0
        result = 31 * result + if (getSession() != null) getSession().hashCode() else 0
        result = 31 * result + if (getName() != null) getName().hashCode() else 0
        return result
    }

    override fun toString(): String {
        return "ClientSimpleUser{" +
                "uuid='" + uuid + '\'' +
                ", session='" + session + '\'' +
                ", name='" + name + '\'' +
                '}'
    }
}