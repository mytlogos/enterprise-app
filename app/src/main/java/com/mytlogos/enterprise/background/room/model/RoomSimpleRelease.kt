package com.mytlogos.enterprise.background.room.model

class RoomSimpleRelease(val partId: Int, val episodeId: Int, val url: String) {
    override fun toString(): String {
        return "RoomSimpleRelease{" +
                "partId=" + partId +
                ", episodeId=" + episodeId +
                ", url='" + url + '\'' +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as RoomSimpleRelease
        if (partId != that.partId) return false
        return if (episodeId != that.episodeId) false else url == that.url
    }

    override fun hashCode(): Int {
        var result = partId
        result = 31 * result + episodeId
        result = 31 * result + url.hashCode()
        return result
    }
}