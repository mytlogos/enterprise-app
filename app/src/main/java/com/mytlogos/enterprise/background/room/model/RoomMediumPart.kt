package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    foreignKeys = [
        ForeignKey(
            parentColumns = ["partId"],
            childColumns = ["partId"],
            onDelete = ForeignKey.CASCADE,
            entity = RoomPart::class
        ),
        ForeignKey(parentColumns = ["mediumId"],
            childColumns = ["mediumId"],
            onDelete = ForeignKey.CASCADE,
            entity = RoomMedium::class
        )
    ],
    indices = [
        Index(value = ["partId"]),
        Index(value = ["mediumId"])
    ],
    primaryKeys = ["mediumId", "partId"]
)
data class RoomMediumPart(
    val mediumId: Int,
    val partId: Int,
)