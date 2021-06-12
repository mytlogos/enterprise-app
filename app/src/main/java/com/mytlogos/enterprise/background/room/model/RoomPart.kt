package com.mytlogos.enterprise.background.room.model

import androidx.room.*
import com.mytlogos.enterprise.model.Part
import java.util.*

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = RoomMedium::class,
            onDelete = ForeignKey.SET_NULL,
            parentColumns = ["mediumId"],
            childColumns = ["mediumId"]
        )
    ],
    indices = [
        Index(value = ["mediumId"]),
        Index(value = ["partId"])
    ]
)
data class RoomPart(
    @field:PrimaryKey override val partId: Int,
    val mediumId: Int,
    override val title: String,
    override val totalIndex: Int,
    override val partialIndex: Int,
    val combiIndex: Double,
) : Part {

    @Ignore
    override val episodes: List<Int> = ArrayList()
}