package com.mytlogos.enterprise.model

import java.io.Serializable

data class MediumInWait(
    val title: String,
    val medium: Int,
    val link: String,
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as MediumInWait
        if (medium != that.medium) return false
        return if (title != that.title) false else link == that.link
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + medium
        result = 31 * result + link.hashCode()
        return result
    }
}