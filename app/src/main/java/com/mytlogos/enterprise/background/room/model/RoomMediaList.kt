package com.mytlogos.enterprise.background.room.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = RoomUser::class,
            childColumns = ["uuid"],
            parentColumns = ["uuid"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["uuid"]),
        Index(value = ["listId"])
    ]
)
class RoomMediaList(
    @field:PrimaryKey val listId: Int,
    val uuid: String?,
    val name: String?,
    val medium: Int,
) {
    override fun toString(): String {
        return "RoomMediaList{" +
                "listId=" + listId +
                ", uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", medium=" + medium +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as RoomMediaList
        if (listId != that.listId) return false
        if (medium != that.medium) return false
        return if (uuid != that.uuid) false else name == that.name
    }

    override fun hashCode(): Int {
        var result = listId
        result = 31 * result + (uuid?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + medium
        return result
    }

    @Entity(primaryKeys = ["listId", "mediumId"],
        foreignKeys = [ForeignKey(entity = RoomMedium::class,
            parentColumns = ["mediumId"],
            childColumns = ["mediumId"],
            onDelete = ForeignKey.CASCADE), ForeignKey(entity = RoomMediaList::class,
            parentColumns = ["listId"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE)],
        indices = [Index(value = ["listId"]), Index(value = ["mediumId"])])
    class MediaListMediaJoin(override val listId: Int, override val mediumId: Int) : ListMediaJoin {
        override fun toString(): String {
            return "MediaListMediaJoin{" +
                    "listId=" + listId +
                    ", mediumId=" + mediumId +
                    '}'
        }

        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o == null || javaClass != o.javaClass) return false
            val that = o as MediaListMediaJoin
            return if (listId != that.listId) false else mediumId == that.mediumId
        }

        override fun hashCode(): Int {
            var result = listId
            result = 31 * result + mediumId
            return result
        }
    }
}