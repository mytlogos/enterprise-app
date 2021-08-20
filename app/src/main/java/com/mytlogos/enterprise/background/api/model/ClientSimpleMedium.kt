package com.mytlogos.enterprise.background.api.model

/**
 * API Model for SimpleMedium.
 */
class ClientSimpleMedium {
    var id = 0
        private set
    var languageOfOrigin: String? = null
        private set
    var countryOfOrigin: String? = null
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
    constructor(medium: ClientMedium) {
        id = medium.id
        countryOfOrigin = medium.countryOfOrigin
        languageOfOrigin = medium.languageOfOrigin
        author = medium.author
        title = medium.title
        this.medium = medium.medium
        artist = medium.artist
        lang = medium.lang
        stateOrigin = medium.stateOrigin
        stateTL = medium.stateTL
        series = medium.series
        universe = medium.universe
    }

    constructor(id: Int, countryOfOrigin: String?, languageOfOrigin: String?, author: String?,
                title: String, medium: Int, artist: String?, lang: String?, stateOrigin: Int,
                stateTL: Int, series: String?, universe: String?) {
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
        return "ClientSimpleMedium{" +
                "id=" + id +
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

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as ClientSimpleMedium
        return id == that.id
    }

    override fun hashCode(): Int {
        return id
    }
}