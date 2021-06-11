package com.mytlogos.enterprise.background.room.model

data class RoomProgressComparison(
    val mediumId: Int,
    val currentReadIndex: Double,
    val currentMaxReadIndex: Double,
)