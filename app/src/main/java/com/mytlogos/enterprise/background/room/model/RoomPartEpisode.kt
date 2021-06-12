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
        ForeignKey(
            parentColumns = ["episodeId"],
            childColumns = ["episodeId"],
            onDelete = ForeignKey.CASCADE,
            entity = RoomEpisode::class
        )
    ],
    indices = [
        Index(value = ["partId"]),
        Index(value = ["episodeId"])
    ],
    primaryKeys = ["episodeId", "partId"]
)
data class RoomPartEpisode(val partId: Int, val episodeId: Int)