package com.mytlogos.enterprise.background.room.model

class RoomTocStat(val mediumId: Int, val tocCount: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as RoomTocStat
        return if (mediumId != that.mediumId) false else tocCount == that.tocCount
    }

    override fun hashCode(): Int {
        var result = mediumId
        result = 31 * result + tocCount
        return result
    }

    override fun toString(): String {
        return "RoomTocStat{" +
                "mediumId=" + mediumId +
                ", tocCount=" + tocCount +
                '}'
    }
}