package com.mytlogos.enterprise.background.api.model

/**
 * API Model for DownloadContent.
 */
class ClientDownloadedEpisode(private val content: Array<String>, private val title: String, val episodeId: Int) {
    fun getContent(): Array<String> {
        return content
    }

    fun getTitle(): String {
        return title
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val episode = o as ClientDownloadedEpisode
        if (episodeId != episode.episodeId) return false
        if (!getContent().contentEquals(episode.getContent())) return false
        return getTitle() == episode.getTitle()
    }

    override fun hashCode(): Int {
        var result = getContent().contentHashCode()
        result = 31 * result + getTitle().hashCode()
        result = 31 * result + episodeId
        return result
    }
}