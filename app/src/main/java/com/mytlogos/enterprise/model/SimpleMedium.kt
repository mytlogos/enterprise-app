package com.mytlogos.enterprise.model

data class SimpleMedium(
    val mediumId: Int,
    val title: String,
    val medium: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as SimpleMedium
        return mediumId != that.mediumId
    }

    override fun hashCode(): Int {
        return mediumId
    }
}