package com.mytlogos.enterprise.model

import org.joda.time.DateTime

data class DisplayRelease(
    val episodeId: Int,
    val mediumId: Int,
    val mediumTitle: String,
    override val totalIndex: Int,
    override val partialIndex: Int,
    val saved: Boolean,
    val read: Boolean,
    val title: String,
    val url: String,
    val releaseDate: DateTime,
    val locked: Boolean
): Indexable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as DisplayRelease
        if (episodeId != that.episodeId) return false
        if (locked != that.locked) return false
        return if (url != that.url) false else releaseDate == that.releaseDate
    }

    override fun hashCode(): Int {
        var result = episodeId
        result = 31 * result + url.hashCode()
        result = 31 * result + releaseDate.hashCode()
        result = 31 * result + if (locked) 1 else 0
        return result
    }
}