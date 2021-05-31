package com.mytlogos.enterprise.background.api.model

import com.mytlogos.enterprise.model.Toc

/**
 * API Model for FullMediumToc.
 * TODO: Missing properties:
 * id, countryOfOrigin, languageOfOrigin, author, title, medium,
 * artist, lang, stateOrigin, stateTL, series, universe
 */
class ClientToc(private val mediumId: Int, private val link: String) : Toc {
    override fun getMediumId(): Int {
        return mediumId
    }

    override fun getLink(): String {
        return link
    }

    override fun hashCode(): Int {
        var result = mediumId
        result = 31 * result + (link?.hashCode() ?: 0)
        return result
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is ClientToc) return false
        val clientToc = o
        return if (mediumId != clientToc.mediumId) false else link == clientToc.link
    }

    override fun toString(): String {
        return "ClientToc{" +
                "mediumId=" + mediumId +
                ", link='" + link + '\'' +
                '}'
    }
}