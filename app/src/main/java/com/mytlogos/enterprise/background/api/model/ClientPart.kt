package com.mytlogos.enterprise.background.api.model

/**
 * API Model of Part.
 */
data class ClientPart(
    val mediumId: Int,
    val id: Int,
    val title: String,
    val totalIndex: Int,
    val partialIndex: Int,
    val episodes: Array<ClientEpisode>?,
) {
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