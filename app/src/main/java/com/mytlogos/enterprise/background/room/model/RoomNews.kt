package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.DateTime

@Entity
data class RoomNews(
    @field:PrimaryKey val newsId: Int,
    val read: Boolean,
    val title: String,
    val timeStamp: DateTime,
    val link: String,
)