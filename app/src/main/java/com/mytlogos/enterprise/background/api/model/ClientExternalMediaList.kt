package com.mytlogos.enterprise.background.api.model

/**
 * API Model for ExternalList.
 */
data class ClientExternalMediaList(
    val uuid: String,
    val id: Int,
    val name: String,
    val medium: Int,
    val url: String,
    val items: IntArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ClientExternalMediaList
        return id == that.id
    }

    override fun hashCode(): Int {
        return id
    }
}