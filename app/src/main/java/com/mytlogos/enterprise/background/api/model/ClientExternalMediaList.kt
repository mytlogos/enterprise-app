package com.mytlogos.enterprise.background.api.model

import java.util.*

/**
 * API Model for ExternalList.
 */
class ClientExternalMediaList(val uuid: String, val id: Int, val name: String, val medium: Int, val url: String,
                              val items: IntArray) {
    override fun toString(): String {
        return "ClientExternalMediaList{" +
                "uuid='" + uuid + '\'' +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", medium=" + medium +
                ", url='" + url + '\'' +
                ", items=" + items.contentToString() +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as ClientExternalMediaList
        return id == that.id
    }

    override fun hashCode(): Int {
        return id
    }
}