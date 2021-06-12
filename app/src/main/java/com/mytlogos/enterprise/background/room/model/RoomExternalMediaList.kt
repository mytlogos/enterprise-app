package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = RoomExternalUser::class,
            parentColumns = ["uuid"],
            childColumns = ["uuid"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["uuid"]),
        Index(value = ["externalListId"])
    ]
)
data class RoomExternalMediaList(
    val uuid: String?,
    @field:PrimaryKey val externalListId: Int,
    val name: String?,
    val medium: Int,
    val url: String?,
) {
    @Entity(primaryKeys = ["listId", "mediumId"],
        foreignKeys = [ForeignKey(entity = RoomMedium::class,
            parentColumns = ["mediumId"],
            childColumns = ["mediumId"],
            onDelete = ForeignKey.CASCADE), ForeignKey(entity = RoomExternalMediaList::class,
            parentColumns = ["externalListId"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE)],
        indices = [Index(value = ["listId"]), Index(value = ["mediumId"])])
    data class ExternalListMediaJoin(
        override val listId: Int,
        override val mediumId: Int,
    ) : ListMediaJoin
}