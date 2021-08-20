package com.mytlogos.enterprise.model

import androidx.room.Ignore
import com.mytlogos.enterprise.formatDateTime
import org.joda.time.DateTime

data class News @JvmOverloads constructor(
    private val title: String,
    private val timeStamp: DateTime,
    val id: Int,
    val read: Boolean,
    val url: String, // this is so ugly, but at the moment mediumType is not saved in storage
    @field:Ignore
    val mediumType: Int = ALL
) {

    val timeStampString: String
        get() = getTimeStamp().formatDateTime()

    fun getTitle(): String {
        return title
    }

    fun getTimeStamp(): DateTime {
        return timeStamp
    }
}