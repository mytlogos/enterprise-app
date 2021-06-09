package com.mytlogos.enterprise.background.api.model

class Authentication(private val uuid: String, private val session: String) {
    fun getSession(): String {
        return session
    }

    fun getUuid(): String {
        return uuid
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as Authentication
        if (getUuid() != that.getUuid()) return false
        return getSession() == that.getSession()
    }

    override fun hashCode(): Int {
        var result = getUuid().hashCode()
        result = 31 * result + getSession().hashCode()
        return result
    }
}