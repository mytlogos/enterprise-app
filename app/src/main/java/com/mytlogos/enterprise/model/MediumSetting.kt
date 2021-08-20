package com.mytlogos.enterprise.model

import org.joda.time.DateTime

data class MediumSetting(
    private val title: String,
    val mediumId: Int,
    private val author: String,
    private val artist: String,
    val medium: Int,
    val stateTL: Int,
    val stateOrigin: Int,
    private val countryOfOrigin: String,
    private val languageOfOrigin: String,
    private val lang: String,
    private val series: String,
    private val universe: String,
    val currentRead: Int,
    val currentReadEpisode: Int,
    val lastEpisode: Int,
    private val lastUpdated: DateTime,
    val toDownload: Boolean
) {
    fun getTitle(): String {
        return title
    }

    fun getAuthor(): String {
        return author
    }

    fun getArtist(): String {
        return artist
    }

    fun getCountryOfOrigin(): String {
        return countryOfOrigin
    }

    fun getLanguageOfOrigin(): String {
        return languageOfOrigin
    }

    fun getLang(): String {
        return lang
    }

    fun getSeries(): String {
        return series
    }

    fun getUniverse(): String {
        return universe
    }

    fun getLastUpdated(): DateTime {
        return lastUpdated
    }
}