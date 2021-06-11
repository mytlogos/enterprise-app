package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(foreignKeys = [ForeignKey(parentColumns = arrayOf("partId"),
        childColumns = arrayOf("partId"),
        onDelete = ForeignKey.CASCADE,
        entity = RoomPart::class), ForeignKey(parentColumns = arrayOf("mediumId"),
        childColumns = arrayOf("mediumId"),
        onDelete = ForeignKey.CASCADE,
        entity = RoomMedium::class)], indices = [Index(value = arrayOf("partId")), Index(value = arrayOf("mediumId"))], primaryKeys = ["mediumId", "partId"])
data class RoomMediumPart(
    val mediumId: Int,
    val partId: Int,
)