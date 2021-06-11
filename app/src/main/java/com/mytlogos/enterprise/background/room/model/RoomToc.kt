package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.mytlogos.enterprise.model.Toc

@Entity(primaryKeys = ["mediumId", "link"],
        foreignKeys = [ForeignKey(entity = RoomMedium::class,
                onDelete = ForeignKey.CASCADE,
                childColumns = arrayOf("mediumId"),
                parentColumns = arrayOf("mediumId"))],
        indices = [Index("mediumId"), Index("link")])
data class RoomToc(
    override val mediumId: Int,
    override val link: String,
): Toc