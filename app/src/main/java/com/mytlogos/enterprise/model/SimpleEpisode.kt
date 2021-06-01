package com.mytlogos.enterprise.model

open class SimpleEpisode(
    val episodeId: Int,
    val totalIndex: Int,
    val partialIndex: Int,
    val progress: Float
) {
    val formattedTitle: String
        get() = if (partialIndex > 0) {
            String.format("#%s.%s", totalIndex, partialIndex)
        } else {
            String.format("#%s", totalIndex)
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as SimpleEpisode
        return episodeId == that.episodeId
    }

    override fun hashCode(): Int {
        return episodeId
    }
}