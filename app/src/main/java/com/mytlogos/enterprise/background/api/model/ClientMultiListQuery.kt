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
        return if (!Arrays.equals(list, that.list)) false else Arrays.equals(media, that.media)
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
    }

    override fun hashCode(): Int {
        var result = Arrays.hashCode(list)
        result = 31 * result + Arrays.hashCode(media)
        return result
    }

    override fun toString(): String {
        return "ClientMultiListQuery{" +
                "list=" + Arrays.toString(list) +
                ", media=" + Arrays.toString(media) +
                '}'
    }
}