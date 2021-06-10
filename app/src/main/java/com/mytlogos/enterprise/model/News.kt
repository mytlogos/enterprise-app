package com.mytlogos.enterprise.model

import androidx.room.Ignore
import com.mytlogos.enterprise.Formatter
import org.joda.time.DateTime

class News @JvmOverloads constructor(
    private val title: String,
    private val timeStamp: DateTime,
    val id: Int,
    val read: Boolean,
    val url: String, // this is so ugly, but at the moment mediumType is not saved in storage
    @field:Ignore val mediumType: Int = ALL
) {

    val timeStampString: String
        get() = Formatter.formatDateTime(getTimeStamp())

    fun getTitle(): String {
        return title
    }

    fun getTimeStamp(): DateTime {
        return timeStamp
    }

    override fun toString(): String {
        return "News{" +
                "title='" + title + '\'' +
                ", timeStamp=" + timeStamp +
                ", id=" + id +
                ", read=" + read +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val news = other as News
        if (id != news.id) return false
        if (read != news.read) return false
        if (getTitle() != news.getTitle()) return false
        return getTimeStamp() == news.getTimeStamp()
    }

    override fun hashCode(): Int {
        var result = getTitle().hashCode()
        result = 31 * result + getTimeStamp().hashCode()
        result = 31 * result + id
        result = 31 * result + if (read) 1 else 0
        return result
    }
}