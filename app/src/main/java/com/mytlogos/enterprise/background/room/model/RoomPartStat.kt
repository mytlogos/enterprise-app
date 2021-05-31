package com.mytlogos.enterprise.background.room.model

class RoomPartStat(val partId: Int, val episodeCount: Long, val episodeSum: Long, val releaseCount: Long) {
    override fun toString(): String {
        return "RoomPartStat{" +
                "partId=" + partId +
                ", episodeCount=" + episodeCount +
                ", episodeSum=" + episodeSum +
                ", releaseCount=" + releaseCount +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as RoomPartStat
        if (partId != that.partId) return false
        if (episodeCount != that.episodeCount) return false
        return if (episodeSum != that.episodeSum) false else releaseCount == that.releaseCount
    }

    override fun hashCode(): Int {
        var result = partId
        result = 31 * result + (episodeCount xor (episodeCount ushr 32)).toInt()
        result = 31 * result + (episodeSum xor (episodeSum ushr 32)).toInt()
        result = 31 * result + (releaseCount xor (releaseCount ushr 32)).toInt()
        return result
    }
}