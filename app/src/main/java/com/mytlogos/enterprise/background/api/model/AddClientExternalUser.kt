package com.mytlogos.enterprise.background.api.model

/**
 * TODO: check Parameter for POST externalUser.
 */
class AddClientExternalUser(uuid: String, identifier: String, type: Int, lists: Array<ClientExternalMediaList>, private val pwd: String) : ClientExternalUser("", uuid, identifier, type, lists) {
    fun getPwd(): String? {
        return pwd
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        if (!super.equals(o)) return false
        val that = o as AddClientExternalUser
        return if (getPwd() != null) getPwd() == that.getPwd() else that.getPwd() == null
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + if (getPwd() != null) getPwd().hashCode() else 0
        return result
    }
}