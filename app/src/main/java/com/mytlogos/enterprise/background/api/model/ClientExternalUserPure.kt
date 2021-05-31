package com.mytlogos.enterprise.background.api.model

/**
 * API Model for DisplayExternalUser.
 */
class ClientExternalUserPure(val localUuid: String, private val uuid: String, val identifier: String, val type: Int) {
    fun getUuid(): String? {
        return uuid
    }

    override fun toString(): String {
        return "ClientExternalUser{" +
                "localUuid='" + localUuid + '\'' +
                ", uuid='" + uuid + '\'' +
                ", identifier='" + identifier + '\'' +
                ", type=" + type +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as ClientExternalUserPure
        return if (getUuid() != null) getUuid() == that.getUuid() else that.getUuid() == null
    }

    override fun hashCode(): Int {
        return if (getUuid() != null) getUuid().hashCode() else 0
    }
}