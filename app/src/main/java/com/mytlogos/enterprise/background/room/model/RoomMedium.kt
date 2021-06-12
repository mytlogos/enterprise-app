package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mytlogos.enterprise.model.Medium

@Entity(
    foreignKeys = [
        ForeignKey(
            parentColumns = ["episodeId"],
            childColumns = ["currentRead"],
            onDelete = ForeignKey.SET_NULL,
            entity = RoomEpisode::class
        )
    ],
    indices = [
        Index(value = ["currentRead"]),
        Index(value = ["mediumId"])
    ]
)
class RoomMedium(
    override val currentRead: Int?,
    @field:PrimaryKey override val mediumId: Int,
    override val countryOfOrigin: String,
    override val languageOfOrigin: String,
    override val author: String,
    override val title: String,
    override val medium: Int,
    override val artist: String,
    override val lang: String,
    override val stateOrigin: Int,
    override val stateTL: Int,
    override val series: String,
    override val universe: String,
) : Medium {
    override fun toString(): String {
        return "RoomMedium{" +
                ", mediumId=" + mediumId +
                ", currentRead=" + currentRead +
                ", countryOfOrigin='" + countryOfOrigin + '\'' +
                ", languageOfOrigin='" + languageOfOrigin + '\'' +
                ", author='" + author + '\'' +
                ", title='" + title + '\'' +
                ", medium=" + medium +
                ", artist='" + artist + '\'' +
                ", lang='" + lang + '\'' +
                ", stateOrigin=" + stateOrigin +
                ", stateTL=" + stateTL +
                ", series='" + series + '\'' +
                ", universe='" + universe + '\'' +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as RoomMedium
        return mediumId == that.mediumId
    }

    override fun hashCode(): Int {
        return mediumId
    }
}