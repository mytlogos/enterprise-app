package com.mytlogos.enterprise.background.api.model

import java.util.*

/**
 * API Model for Medium.
 */
class ClientMedium {
    var parts: IntArray? = null
        private set
    var latestReleased: IntArray? = null
        private set
    var currentRead = 0
        private set
    var unreadEpisodes: IntArray? = null
        private set
    var id = 0
        private set
    var countryOfOrigin: String? = null
        private set
    var languageOfOrigin: String? = null
        private set
    var author: String? = null
        private set
    lateinit var title: String
        private set
    var medium = 0
        private set
    var artist: String? = null
        private set
    var lang: String? = null
        private set
    var stateOrigin = 0
        private set
    var stateTL = 0
        private set
    var series: String? = null
        private set
    var universe: String? = null
        private set

    constructor()
    constructor(parts: IntArray, latestReleased: IntArray, currentRead: Int, unreadEpisodes: IntArray, id: Int,
                countryOfOrigin: String?, languageOfOrigin: String?, author: String?,
                title: String, medium: Int, artist: String?, lang: String?, stateOrigin: Int,
                stateTL: Int, series: String?, universe: String?) {
        this.parts = parts
        this.latestReleased = latestReleased
        this.currentRead = currentRead
        this.unreadEpisodes = unreadEpisodes
        this.id = id
        this.countryOfOrigin = countryOfOrigin
        this.languageOfOrigin = languageOfOrigin
        this.author = author
        this.title = title
        this.medium = medium
        this.artist = artist
        this.lang = lang
        this.stateOrigin = stateOrigin
        this.stateTL = stateTL
        this.series = series
        this.universe = universe
    }

    constructor(id: Int, title: String, medium: Int) {
        this.id = id
        this.title = title
        this.medium = medium
    }

    override fun toString(): String {
        return "ClientMedium{" +
                "parts=" + Arrays.toString(parts) +
                ", latestReleased=" + Arrays.toString(latestReleased) +
                ", currentRead=" + currentRead +
                ", unreadEpisodes=" + Arrays.toString(unreadEpisodes) +
                ", id=" + id +
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
        val that = other as ClientMedium
        return id == that.id
    }

    override fun hashCode(): Int {
        return id
    }
}