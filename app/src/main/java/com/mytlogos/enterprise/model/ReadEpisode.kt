package com.mytlogos.enterprise.model

class ReadEpisode(
    val episodeId: Int,
    val mediumId: Int,
    val mediumTitle: String,
    val totalIndex: Int,
    val partialIndex: Int,
    val releases: List<Release>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ReadEpisode
        return episodeId == that.episodeId
    }

    override fun hashCode(): Int {
        return episodeId
    }
}