package com.mytlogos.enterprise.background.api.model

import com.mytlogos.enterprise.model.Indexable
import org.joda.time.DateTime

/**
 * API Model for Episode.
 */
data class ClientEpisode(
    val id: Int,
    val progress: Float,
    val partId: Int,
    override val totalIndex: Int,
    override val partialIndex: Int,
    val combiIndex: Double,
    val readDate: DateTime?,
    val releases: Array<ClientEpisodeRelease>
): Indexable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ClientEpisode
        return id == that.id
    }

    override fun hashCode(): Int {
        return id
    }
}