package com.mytlogos.enterprise.background.api.model

import java.util.*

/**
 * API Model for ListMedia.
 */
class ClientMultiListQuery(val list: Array<ClientMediaList>, val media: Array<ClientMedium>) {
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as ClientMultiListQuery

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return if (!list.contentEquals(that.list)) false else media.contentEquals(that.media)
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
    }

    override fun hashCode(): Int {
        var result = list.contentHashCode()
        result = 31 * result + media.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "ClientMultiListQuery{" +
                "list=" + list.contentToString() +
                ", media=" + media.contentToString() +
                '}'
    }
}