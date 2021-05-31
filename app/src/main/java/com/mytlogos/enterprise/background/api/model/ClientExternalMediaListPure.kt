package com.mytlogos.enterprise.background.api.model

/**
 * API Model for PureExternalList.
 */
class ClientExternalMediaListPure(val uuid: String, val id: Int, val name: String, val medium: Int, val url: String) {
    override fun toString(): String {
        return "ClientExternalMediaList{" +
                "uuid='" + uuid + '\'' +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", medium=" + medium +
                ", url='" + url + '\'' +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as ClientExternalMediaListPure
        return id == that.id
    }

    override fun hashCode(): Int {
        return id
    }
}