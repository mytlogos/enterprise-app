package com.mytlogos.enterprise.model

open class SimpleEpisode(
    val episodeId: Int,
    override val totalIndex: Int,
    override val partialIndex: Int,
    val progress: Float
) : Indexable {
    val formattedTitle: String
        get() = if (partialIndex > 0) {
            "#$totalIndex.$partialIndex"
        } else {
            "#$totalIndex"
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