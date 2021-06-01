package com.mytlogos.enterprise.background

import com.mytlogos.enterprise.model.Toc

/**
 *
 */
class SimpleToc(
    override val mediumId: Int,
    override val link: String,
) : Toc {
    override fun hashCode(): Int {
        var result = mediumId
        result = 31 * result + link.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SimpleToc) return false
        return if (mediumId != other.mediumId) false else link == other.link
    }

    override fun toString(): String {
        return "SimpleToc{" +
                "mediumId=" + mediumId +
                ", link='" + link + '\'' +
                '}'
    }
}