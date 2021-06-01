package com.mytlogos.enterprise.model

class ChapterPage(val episodeId: Int, val page: Int, val path: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ChapterPage
        return if (episodeId != that.episodeId) false else page == that.page
    }

    override fun hashCode(): Int {
        var result = episodeId
        result = 31 * result + page
        return result
    }

    override fun toString(): String {
        return "ChapterPage{" +
                "episodeId=" + episodeId +
                ", page=" + page +
                '}'
    }
}