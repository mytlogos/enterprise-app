package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.mytlogos.enterprise.model.Release
import org.joda.time.DateTime

@Entity(primaryKeys = ["episodeId", "url"],
        foreignKeys = [ForeignKey(entity = RoomEpisode::class,
                onDelete = ForeignKey.CASCADE,
                childColumns = arrayOf("episodeId"),
                parentColumns = arrayOf("episodeId"))],
        indices = [Index("episodeId")])
class RoomRelease(
    private val episodeId: Int,
    private val title: String,
    private val url: String,
    private val releaseDate: DateTime,
    private val locked: Boolean
) : Release {
    override fun isLocked(): Boolean {
        return locked
    }

    override fun getReleaseDate(): DateTime {
        return releaseDate
    }

    override fun getTitle(): String {
        return title
    }

    override fun getUrl(): String {
        return url
    }

    override fun getEpisodeId(): Int {
        return episodeId
    }
}