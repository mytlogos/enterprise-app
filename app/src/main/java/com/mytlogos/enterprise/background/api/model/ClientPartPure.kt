package com.mytlogos.enterprise.background.api.model

/**
 * API Model for MinPart.
 */
class ClientPartPure(val mediumId: Int, val id: Int, val title: String, val totalIndex: Int, val partialIndex: Int) {
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as ClientPartPure
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