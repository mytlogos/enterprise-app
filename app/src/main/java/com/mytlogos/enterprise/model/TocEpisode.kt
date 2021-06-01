package com.mytlogos.enterprise.model

import org.joda.time.DateTime

class TocEpisode(
    val episodeId: Int,
    val progress: Float,
    val partId: Int,
    val partialIndex: Int,
    val totalIndex: Int,
    val readDate: DateTime?,
    val isSaved: Boolean,
    val releases: List<Release>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as TocEpisode
        return episodeId == that.episodeId
    }

    override fun hashCode(): Int {
        return episodeId
    }
}