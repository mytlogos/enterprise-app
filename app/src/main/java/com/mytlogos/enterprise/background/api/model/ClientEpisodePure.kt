package com.mytlogos.enterprise.background.api.model

import com.mytlogos.enterprise.model.Indexable
import org.joda.time.DateTime

/**
 * API Model for PureEpisode.
 */
class ClientEpisodePure(
    val id: Int,
    val progress: Float,
    val partId: Int,
    override val totalIndex: Int,
    override val partialIndex: Int,
    val combiIndex: Double,
    val readDate: DateTime?,
    releases: Array<ClientRelease?>?,
): Indexable {
    override fun toString(): String {
        return "ClientEpisodePure{" +
                "id=" + id +
                ", progress=" + progress +
                ", partId=" + partId +
                ", totalIndex=" + totalIndex +
                ", partialIndex=" + partialIndex +
                ", combiIndex" + combiIndex +
                ", readDate=" + readDate +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ClientEpisodePure
        return id == that.id
    }

    override fun hashCode(): Int {
        return id
    }
}