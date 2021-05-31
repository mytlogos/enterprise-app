package com.mytlogos.enterprise.background.api.model

class ClientSimpleUser(private val uuid: String, private val session: String, private val name: String) {
    fun getUuid(): String {
        return uuid
    }

    fun getSession(): String {
        return session
    }

    fun getName(): String {
        return name
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as ClientSimpleUser
        if (getUuid() != that.getUuid()) return false
        if (getSession() != that.getSession()) return false
        return getName() == that.getName()
    }

    override fun hashCode(): Int {
        var result = getUuid().hashCode()
        result = 31 * result + getSession().hashCode()
        result = 31 * result + getName().hashCode()
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