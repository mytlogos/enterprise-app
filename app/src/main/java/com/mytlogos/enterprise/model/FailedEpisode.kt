package com.mytlogos.enterprise.model

data class FailedEpisode(
    val episodeId: Int,
    val failCount: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as FailedEpisode
        return episodeId == that.episodeId
    }

    override fun hashCode(): Int {
        return episodeId
    }
}