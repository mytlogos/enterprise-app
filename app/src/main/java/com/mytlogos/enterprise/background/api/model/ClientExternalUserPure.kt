package com.mytlogos.enterprise.background.api.model

/**
 * API Model for DisplayExternalUser.
 */
class ClientExternalUserPure(val localUuid: String, private val uuid: String, val identifier: String, val type: Int) {
    fun getUuid(): String {
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ClientExternalUserPure
        return getUuid() == that.getUuid()
    }

    override fun hashCode(): Int {
        return getUuid().hashCode()
    }
}