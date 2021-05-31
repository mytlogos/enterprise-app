package com.mytlogos.enterprise.background.api.model

import java.util.*

/**
 * API Model for DownloadContent.
 */
class ClientDownloadedEpisode(private val content: Array<String>, private val title: String, val episodeId: Int) {
    fun getContent(): Array<String>? {
        return content
    }

    fun getTitle(): String? {
        return title
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val episode = o as ClientDownloadedEpisode
        if (episodeId != episode.episodeId) return false
        if (if (getContent() != null) !Arrays.equals(getContent(), episode.getContent()) else episode.getContent() != null) return false
        return if (getTitle() != null) getTitle() == episode.getTitle() else episode.getTitle() == null
    }

    override fun hashCode(): Int {
        var result = if (getContent() != null) Arrays.hashCode(getContent()) else 0
        result = 31 * result + if (getTitle() != null) getTitle().hashCode() else 0
        result = 31 * result + episodeId
        return result
    }
}