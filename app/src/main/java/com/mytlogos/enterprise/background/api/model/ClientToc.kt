package com.mytlogos.enterprise.background.api.model

import com.mytlogos.enterprise.model.Toc

/**
 * API Model for FullMediumToc.
 * TODO: Missing properties:
 * id, countryOfOrigin, languageOfOrigin, author, title, medium,
 * artist, lang, stateOrigin, stateTL, series, universe
 */
class ClientToc(
    override val mediumId: Int,
    override val link: String,
) : Toc {

    override fun hashCode(): Int {
        var result = mediumId
        result = 31 * result + (link.hashCode())
        return result
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is ClientToc) return false
        return if (mediumId != o.mediumId) false else link == o.link
    }

    override fun toString(): String {
        return "ClientToc{" +
                "mediumId=" + mediumId +
                ", link='" + link + '\'' +
                '}'
    }
}