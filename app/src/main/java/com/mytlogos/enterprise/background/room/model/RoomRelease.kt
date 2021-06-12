package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.mytlogos.enterprise.model.Release
import org.joda.time.DateTime

@Entity(
    primaryKeys = ["episodeId", "url"],
    foreignKeys = [
        ForeignKey(entity = RoomEpisode::class,
            onDelete = ForeignKey.CASCADE,
            childColumns = ["episodeId"],
            parentColumns = ["episodeId"]
        )
    ],
    indices = [
        Index("episodeId"),
    ]
)
class RoomRelease(
    override val episodeId: Int,
    override val title: String,
    override val url: String,
    override val releaseDate: DateTime,
    override val locked: Boolean,
) : Release