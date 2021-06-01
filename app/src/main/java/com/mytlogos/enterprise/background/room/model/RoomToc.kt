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
class RoomToc(
    override val mediumId: Int,
    override val link: String,
): Toc {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val roomToc = other as RoomToc
        return if (mediumId != roomToc.mediumId) false else link == roomToc.link
    }

    override fun hashCode(): Int {
        var result = mediumId
        result = 31 * result + link.hashCode()
        return result
    }

    override fun toString(): String {
        return "RoomToc{" +
                "mediumId=" + mediumId +
                ", link='" + link + '\'' +
                '}'
    }
}