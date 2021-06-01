package com.mytlogos.enterprise.model

import org.joda.time.DateTime

class TocEpisode(
    val episodeId: Int,
    val progress: Float,
    val partId: Int,
    val partialIndex: Int,
    val totalIndex: Int,
    val readDate: DateTime,
    val isSaved: Boolean,
    val releases: List<Release>
) {
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as TocEpisode
        return episodeId == that.episodeId
    }

    override fun hashCode(): Int {
        return episodeId
    }
}