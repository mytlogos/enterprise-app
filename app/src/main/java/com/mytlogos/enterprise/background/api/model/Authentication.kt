package com.mytlogos.enterprise.background.api.model

class Authentication(private val uuid: String, private val session: String) {
    fun getSession(): String? {
        return session
    }

    fun getUuid(): String? {
        return uuid
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as Authentication
        if (if (getUuid() != null) getUuid() != that.getUuid() else that.getUuid() != null) return false
        return if (getSession() != null) getSession() == that.getSession() else that.getSession() == null
    }

    override fun hashCode(): Int {
        var result = if (getUuid() != null) getUuid().hashCode() else 0
        result = 31 * result + if (getSession() != null) getSession().hashCode() else 0
        return result
    }
}