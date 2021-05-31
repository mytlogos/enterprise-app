package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mytlogos.enterprise.model.Medium

@Entity(foreignKeys = [ForeignKey(parentColumns = arrayOf("episodeId"),
        childColumns = arrayOf("currentRead"),
        onDelete = ForeignKey.SET_NULL,
        entity = RoomEpisode::class)],
        indices = [Index(value = arrayOf("currentRead")), Index(value = arrayOf("mediumId"))])
class RoomMedium(
    private val currentRead: Int?,
    @field:PrimaryKey private val mediumId: Int,
    private val countryOfOrigin: String?,
    private val languageOfOrigin: String?,
    private val author: String?,
    private val title: String,
    private val medium: Int,
    private val artist: String?,
    private val lang: String?,
    private val stateOrigin: Int,
    private val stateTL: Int,
    private val series: String?,
    private val universe: String?
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

    override fun getCurrentRead(): Int? {
        return currentRead
    }

    override fun getMediumId(): Int {
        return mediumId
    }

    override fun getCountryOfOrigin(): String? {
        return countryOfOrigin
    }

    override fun getLanguageOfOrigin(): String? {
        return languageOfOrigin
    }

    override fun getAuthor(): String? {
        return author
    }

    override fun getTitle(): String {
        return title
    }

    override fun getMedium(): Int {
        return medium
    }

    override fun getArtist(): String? {
        return artist
    }

    override fun getLang(): String? {
        return lang
    }

    override fun getStateOrigin(): Int {
        return stateOrigin
    }

    override fun getStateTL(): Int {
        return stateTL
    }

    override fun getSeries(): String? {
        return series
    }

    override fun getUniverse(): String? {
        return universe
    }
}