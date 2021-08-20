package com.mytlogos.enterprise.background.api.model

import org.joda.time.DateTime

/**
 * API Model for PureNews.
 */
data class ClientNews(
    val title: String,
    val link: String,
    val date: DateTime,
    val id: Int,
    val isRead: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ClientNews
        return id == that.id
    }

    override fun hashCode(): Int {
        return id
    }
}