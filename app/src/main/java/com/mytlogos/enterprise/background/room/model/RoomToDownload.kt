package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = RoomMedium::class,
            onDelete = ForeignKey.CASCADE,
            parentColumns = ["mediumId"],
            childColumns = ["mediumId"]
        ),
        ForeignKey(
            entity = RoomMediaList::class,
            onDelete = ForeignKey.CASCADE,
            parentColumns = ["listId"],
            childColumns = ["listId"]
        ),
        ForeignKey(
            entity = RoomExternalMediaList::class,
            onDelete = ForeignKey.CASCADE,
            parentColumns = ["externalListId"],
            childColumns = ["externalListId"]
        )
    ],
    indices = [
        Index(value = ["mediumId"], unique = true),
        Index(value = ["listId"], unique = true),
        Index(value = ["externalListId"], unique = true)
    ]
)
class RoomToDownload(
    @field:PrimaryKey(autoGenerate = true) val toDownloadId: Int,
    val prohibited: Boolean,
    val mediumId: Int?,
    val listId: Int?,
    val externalListId: Int?,
) {
    init {
        val isMedium = mediumId != null && mediumId > 0
        val isList = listId != null && listId > 0
        val isExternalList = externalListId != null && externalListId > 0
        require(!(isMedium && (isList || isExternalList) || isList && isExternalList)) { "only one id allowed" }
        require(!(!isMedium && !isList && !isExternalList)) { "one id is necessary!" }
    }
}