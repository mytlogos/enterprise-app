package com.mytlogos.enterprise.background.api.model

import org.joda.time.DateTime

/**
 * API Model for PureNews.
 */
class ClientNews(val title: String, val link: String, val date: DateTime, val id: Int, val isRead: Boolean) {
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as ClientNews
        return id == that.id
    }

    override fun hashCode(): Int {
        return id
    }

    override fun toString(): String {
        return "ClientNews{" +
                "title='" + title + '\'' +
                ", link='" + link + '\'' +
                ", date=" + date +
                ", id=" + id +
                ", read=" + isRead +
                '}'
    }
}