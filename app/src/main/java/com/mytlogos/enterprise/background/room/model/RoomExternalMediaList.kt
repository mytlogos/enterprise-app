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
class RoomExternalMediaList(val uuid: String?, @field:PrimaryKey val externalListId: Int, val name: String?, val medium: Int, val url: String?) {
    override fun toString(): String {
        return "RoomExternalMediaList{" +
                "uuid='" + uuid + '\'' +
                ", externalListId=" + externalListId +
                ", name='" + name + '\'' +
                ", medium=" + medium +
                ", url='" + url + '\'' +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as RoomExternalMediaList
        if (externalListId != that.externalListId) return false
        if (medium != that.medium) return false
        if (uuid != that.uuid) return false
        return if (name != that.name) false else url == that.url
    }

    override fun hashCode(): Int {
        var result = uuid?.hashCode() ?: 0
        result = 31 * result + externalListId
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + medium
        result = 31 * result + (url?.hashCode() ?: 0)
        return result
    }

    @Entity(primaryKeys = ["listId", "mediumId"],
            foreignKeys = [ForeignKey(entity = RoomMedium::class,
                    parentColumns = arrayOf("mediumId"),
                    childColumns = arrayOf("mediumId"),
                    onDelete = ForeignKey.CASCADE), ForeignKey(entity = RoomExternalMediaList::class,
                    parentColumns = arrayOf("externalListId"),
                    childColumns = arrayOf("listId"),
                    onDelete = ForeignKey.CASCADE)],
            indices = [Index(value = arrayOf("listId")), Index(value = arrayOf("mediumId"))])
    class ExternalListMediaJoin(override val listId: Int, override val mediumId: Int) : ListMediaJoin {
        override fun toString(): String {
            return "ExternalListMediaJoin{" +
                    "listId=" + listId +
                    ", mediumId=" + mediumId +
                    '}'
        }

        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o == null || javaClass != o.javaClass) return false
            val that = o as ExternalListMediaJoin
            return if (listId != that.listId) false else mediumId == that.mediumId
        }

        override fun hashCode(): Int {
            var result = listId
            result = 31 * result + mediumId
            return result
        }
    }
}