package com.mytlogos.enterprise.background.room.model

class RoomProgressComparison(val mediumId: Int, val currentReadIndex: Double, val currentMaxReadIndex: Double) {
    override fun toString(): String {
        return "RoomProgressComparison{" +
                "mediumId=" + mediumId +
                ", currentReadIndex=" + currentReadIndex +
                ", currentMaxReadIndex=" + currentMaxReadIndex +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as RoomProgressComparison
        if (mediumId != that.mediumId) return false
        return if (that.currentReadIndex.compareTo(currentReadIndex) != 0) false else that.currentMaxReadIndex.compareTo(currentMaxReadIndex) == 0
    }

    override fun hashCode(): Int {
        var result: Int = mediumId
        var temp: Long = java.lang.Double.doubleToLongBits(currentReadIndex)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        temp = java.lang.Double.doubleToLongBits(currentMaxReadIndex)
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        return result
    }
}