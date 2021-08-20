package com.mytlogos.enterprise.model

data class DisplayEpisode(
    val episodeId: Int,
    val mediumId: Int,
    val mediumTitle: String,
    override val totalIndex: Int,
    override val partialIndex: Int,
    val isSaved: Boolean,
    val isRead: Boolean,
    val releases: List<Release>
) : Indexable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as DisplayEpisode
        return episodeId == that.episodeId
    }

    override fun hashCode(): Int {
        return episodeId
    }
}