package com.mytlogos.enterprise.background.api.model

/**
 * API Model for ListMedia.
 */
data class ClientMultiListQuery(
    val list: Array<ClientMediaList>,
    val media: Array<ClientMedium>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClientMultiListQuery

        if (!list.contentEquals(other.list)) return false
        if (!media.contentEquals(other.media)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = list.contentHashCode()
        result = 31 * result + media.contentHashCode()
        return result
    }
}