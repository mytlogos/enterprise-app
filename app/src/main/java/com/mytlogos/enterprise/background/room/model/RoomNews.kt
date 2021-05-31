package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.DateTime

@Entity
class RoomNews(@field:PrimaryKey val newsId: Int, val read: Boolean, private val title: String, private val timeStamp: DateTime, val link: String) {
    fun getTitle(): String {
        return title
    }

    fun getTimeStamp(): DateTime {
        return timeStamp
    }

    override fun toString(): String {
        return "RoomNews{" +
                "title='" + title + '\'' +
                ", timeStamp=" + timeStamp +
                ", newsId=" + newsId +
                ", read=" + read +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val roomNews = o as RoomNews
        if (newsId != roomNews.newsId) return false
        if (read != roomNews.read) return false
        if (if (getTitle() != null) getTitle() != roomNews.getTitle() else roomNews.getTitle() != null) return false
        return if (getTimeStamp() != null) getTimeStamp() == roomNews.getTimeStamp() else roomNews.getTimeStamp() == null
    }

    override fun hashCode(): Int {
        var result = if (getTitle() != null) getTitle().hashCode() else 0
        result = 31 * result + if (getTimeStamp() != null) getTimeStamp().hashCode() else 0
        result = 31 * result + newsId
        result = 31 * result + if (read) 1 else 0
        return result
    }
}