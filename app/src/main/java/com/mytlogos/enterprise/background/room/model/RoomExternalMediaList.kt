package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(foreignKeys = [ForeignKey(entity = RoomExternalUser::class,
        parentColumns = arrayOf("uuid"),
        childColumns = arrayOf("uuid"),
        onDelete = ForeignKey.CASCADE)],
        indices = [Index(value = arrayOf("uuid")), Index(value = arrayOf("externalListId"))])
data class RoomExternalMediaList(
    val uuid: String?,
    @field:PrimaryKey val externalListId: Int,
    val name: String?,
    val medium: Int,
    val url: String?,
) {
    @Entity(primaryKeys = ["listId", "mediumId"],
            foreignKeys = [ForeignKey(entity = RoomMedium::class,
                    parentColumns = arrayOf("mediumId"),
                    childColumns = arrayOf("mediumId"),
                    onDelete = ForeignKey.CASCADE), ForeignKey(entity = RoomExternalMediaList::class,
                    parentColumns = arrayOf("externalListId"),
                    childColumns = arrayOf("listId"),
                    onDelete = ForeignKey.CASCADE)],
            indices = [Index(value = arrayOf("listId")), Index(value = arrayOf("mediumId"))])
    data class ExternalListMediaJoin(
        override val listId: Int,
        override val mediumId: Int,
    ) : ListMediaJoin
}