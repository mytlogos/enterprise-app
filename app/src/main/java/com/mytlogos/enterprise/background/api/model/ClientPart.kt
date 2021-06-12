package com.mytlogos.enterprise.background.api.model

import com.mytlogos.enterprise.model.Indexable

/**
 * API Model of Part.
 */
data class ClientPart(
    val mediumId: Int,
    val id: Int,
    val title: String,
    override val totalIndex: Int,
    override val partialIndex: Int,
    val episodes: Array<ClientEpisode>?,
): Indexable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ClientPart
        return id == that.id
    }

    override fun hashCode(): Int {
        return id
    }
}