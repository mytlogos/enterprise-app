package com.mytlogos.enterprise.background.api.model

/**
 * API Model for DisplayExternalUser.
 */
open class ClientExternalUser(val localUuid: String, private val uuid: String, val identifier: String, val type: Int, val lists: Array<ClientExternalMediaList>) {
    fun getUuid(): String {
        return uuid
    }

    override fun toString(): String {
        return "ClientExternalUser{" +
                "localUuid='" + localUuid + '\'' +
                ", uuid='" + uuid + '\'' +
                ", identifier='" + identifier + '\'' +
                ", type=" + type +
                ", lists=" + lists.contentToString() +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as ClientExternalUser
        return getUuid() == that.getUuid()
    }

    override fun hashCode(): Int {
        return getUuid().hashCode()
    }
}