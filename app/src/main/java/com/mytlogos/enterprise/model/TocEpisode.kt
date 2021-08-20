package com.mytlogos.enterprise.model

import com.mytlogos.enterprise.tools.Selectable
import org.joda.time.DateTime

data class TocEpisode(
    val episodeId: Int,
    val progress: Float,
    val partId: Int,
    override val partialIndex: Int,
    override val totalIndex: Int,
    val readDate: DateTime?,
    val isSaved: Boolean,
    val releases: List<Release>
) : Selectable, Indexable {
    override fun getSelectionKey(): Long = episodeId.toLong()

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