package com.mytlogos.enterprise.background.api.model

/**
 * API Model for User.
 */
class ClientUser(val uuid: String, val session: String, val name: String, val externalUser: Array<ClientExternalUser>, val lists: Array<ClientMediaList>, val unreadNews: Array<ClientNews>, val unreadChapter: IntArray, val readToday: Array<ClientReadEpisode>) {
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as ClientUser
        return uuid == that.uuid
    }

    override fun hashCode(): Int {
        return uuid.hashCode()
    }
}