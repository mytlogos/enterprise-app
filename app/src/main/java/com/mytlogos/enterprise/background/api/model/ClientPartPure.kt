package com.mytlogos.enterprise.background.api.model

import com.mytlogos.enterprise.model.Indexable

/**
 * API Model for MinPart.
 */
class ClientPartPure(
    val mediumId: Int,
    val id: Int,
    val title: String,
    override val totalIndex: Int,
    override val partialIndex: Int,
) : Indexable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ClientPartPure
        return id == that.id
    }

    override fun hashCode(): Int {
        return id
    }

    override fun toString(): String {
        return "ClientPart{" +
                "mediumId=" + mediumId +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", totalIndex=" + totalIndex +
                ", partialIndex=" + partialIndex +
                '}'
    }
}