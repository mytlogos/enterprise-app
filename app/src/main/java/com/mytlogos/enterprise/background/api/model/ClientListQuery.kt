package com.mytlogos.enterprise.background.api.model

import java.util.*

/**
 * API Model for ListMedia.
 */
class ClientListQuery(val list: ClientMediaList, val media: Array<ClientMedium>) {

    override fun toString(): String {
        return "ClientListQuery{" +
                "list=" + list +
                ", media=" + media.contentToString() +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as ClientListQuery
        return if (list != that.list) false else media.contentEquals(that.media)
    }

    override fun hashCode(): Int {
        var result = list.hashCode()
        result = 31 * result + media.contentHashCode()
        return result
    }
}