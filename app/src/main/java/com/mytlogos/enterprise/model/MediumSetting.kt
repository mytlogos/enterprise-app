package com.mytlogos.enterprise.model

import org.joda.time.DateTime

class MediumSetting(
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as MediumSetting
        if (mediumId != that.mediumId) return false
        if (medium != that.medium) return false
        if (stateTL != that.stateTL) return false
        if (stateOrigin != that.stateOrigin) return false
        if (currentRead != that.currentRead) return false
        if (currentReadEpisode != that.currentReadEpisode) return false
        if (lastEpisode != that.lastEpisode) return false
        if (toDownload != that.toDownload) return false
        if (getTitle() != that.getTitle()) return false
        if (getAuthor() != that.getAuthor()) return false
        if (getArtist() != that.getArtist()) return false
        if (getCountryOfOrigin() != that.getCountryOfOrigin()) return false
        if (getLanguageOfOrigin() != that.getLanguageOfOrigin()) return false
        if (getLang() != that.getLang()) return false
        if (getSeries() != that.getSeries()) return false
        if (getUniverse() != that.getUniverse()) return false
        return getLastUpdated() == that.getLastUpdated()
    }

    override fun hashCode(): Int {
        var result = getTitle().hashCode()
        result = 31 * result + mediumId
        result = 31 * result + getAuthor().hashCode()
        result = 31 * result + getArtist().hashCode()
        result = 31 * result + medium
        result = 31 * result + stateTL
        result = 31 * result + stateOrigin
        result = 31 * result + getCountryOfOrigin().hashCode()
        result = 31 * result + getLanguageOfOrigin().hashCode()
        result = 31 * result + getLang().hashCode()
        result = 31 * result + getSeries().hashCode()
        result = 31 * result + getUniverse().hashCode()
        result = 31 * result + currentRead
        result = 31 * result + currentReadEpisode
        result = 31 * result + lastEpisode
        result = 31 * result + getLastUpdated().hashCode()
        result = 31 * result + if (toDownload) 1 else 0
        return result
    }

    override fun toString(): String {
        return "MediumSetting{" +
                "title='" + title + '\'' +
                ", mediumId=" + mediumId +
                ", author='" + author + '\'' +
                ", artist='" + artist + '\'' +
                ", medium=" + medium +
                ", stateTL=" + stateTL +
                ", stateOrigin=" + stateOrigin +
                ", countryOfOrigin='" + countryOfOrigin + '\'' +
                ", languageOfOrigin='" + languageOfOrigin + '\'' +
                ", lang='" + lang + '\'' +
                ", series='" + series + '\'' +
                ", universe='" + universe + '\'' +
                ", currentRead=" + currentRead +
                ", currentReadEpisode=" + currentReadEpisode +
                ", lastEpisode=" + lastEpisode +
                ", lastUpdated=" + lastUpdated +
                ", toDownload=" + toDownload +
                '}'
    }

    class MediumSettingBuilder(setting: MediumSetting) {
        private var title: String
        private val mediumId: Int
        private var author: String
        private var artist: String
        private var medium: Int
        private var stateTL: Int
        private var stateOrigin: Int
        private var countryOfOrigin: String
        private var languageOfOrigin: String
        private var lang: String
        private var series: String
        private var universe: String
        private var currentRead: Int
        private val currentReadEpisode: Int
        private val lastEpisode: Int
        private val lastUpdated: DateTime
        private var toDownload: Boolean

        fun setTitle(title: String): MediumSettingBuilder {
            this.title = title
            return this
        }

        fun setAuthor(author: String): MediumSettingBuilder {
            this.author = author
            return this
        }

        fun setArtist(artist: String): MediumSettingBuilder {
            this.artist = artist
            return this
        }

        fun setMedium(medium: Int): MediumSettingBuilder {
            this.medium = medium
            return this
        }

        fun setStateTL(stateTL: Int): MediumSettingBuilder {
            this.stateTL = stateTL
            return this
        }

        fun setStateOrigin(stateOrigin: Int): MediumSettingBuilder {
            this.stateOrigin = stateOrigin
            return this
        }

        fun setCountryOfOrigin(countryOfOrigin: String): MediumSettingBuilder {
            this.countryOfOrigin = countryOfOrigin
            return this
        }

        fun setLanguageOfOrigin(languageOfOrigin: String): MediumSettingBuilder {
            this.languageOfOrigin = languageOfOrigin
            return this
        }

        fun setLang(lang: String): MediumSettingBuilder {
            this.lang = lang
            return this
        }

        fun setSeries(series: String): MediumSettingBuilder {
            this.series = series
            return this
        }

        fun setUniverse(universe: String): MediumSettingBuilder {
            this.universe = universe
            return this
        }

        fun setCurrentRead(currentRead: Int): MediumSettingBuilder {
            this.currentRead = currentRead
            return this
        }

        fun setToDownload(toDownload: Boolean): MediumSettingBuilder {
            this.toDownload = toDownload
            return this
        }

        fun createMediumSetting(): MediumSetting {
            return MediumSetting(title,
                mediumId,
                author,
                artist,
                medium,
                stateTL,
                stateOrigin,
                countryOfOrigin,
                languageOfOrigin,
                lang,
                series,
                universe,
                currentRead,
                currentReadEpisode,
                lastEpisode,
                lastUpdated,
                toDownload)
        }

        init {
            title = setting.title
            mediumId = setting.mediumId
            author = setting.author
            artist = setting.artist
            medium = setting.medium
            stateTL = setting.stateTL
            stateOrigin = setting.stateOrigin
            countryOfOrigin = setting.countryOfOrigin
            languageOfOrigin = setting.languageOfOrigin
            lang = setting.lang
            series = setting.series
            universe = setting.universe
            currentRead = setting.currentRead
            currentReadEpisode = setting.currentReadEpisode
            lastEpisode = setting.lastEpisode
            lastUpdated = setting.lastUpdated
            toDownload = setting.toDownload
        }
    }
}