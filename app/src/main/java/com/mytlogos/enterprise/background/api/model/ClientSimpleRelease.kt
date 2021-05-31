package com.mytlogos.enterprise.background.api.model

class ClientSimpleRelease (val url: String, val id: Int) {

    override fun toString(): String {
        return "ClientSimpleRelease{" +
                "url='" + url + '\'' +
                ", id=" + id +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ClientSimpleRelease
        return if (id != that.id) false else url == that.url
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + id
        return result
    }
}