package com.mytlogos.enterprise.background.room.model

import androidx.room.*
import com.mytlogos.enterprise.model.Part
import java.util.*

@Entity(foreignKeys = [ForeignKey(entity = RoomMedium::class,
        onDelete = ForeignKey.SET_NULL,
        parentColumns = arrayOf("mediumId"),
        childColumns = arrayOf("mediumId"))],
        indices = [Index(value = arrayOf("mediumId")), Index(value = arrayOf("partId"))])
class RoomPart(@field:PrimaryKey private val partId: Int, val mediumId: Int, private val title: String, private val totalIndex: Int, private val partialIndex: Int, val combiIndex: Double) : Part {

    @Ignore
    private val episodes: List<Int>
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val roomPart = o as RoomPart
        if (getPartId() != roomPart.getPartId()) return false
        if (mediumId != roomPart.mediumId) return false
        if (getTotalIndex() != roomPart.getTotalIndex()) return false
        if (getPartialIndex() != roomPart.getPartialIndex()) return false
        if (if (getTitle() != null) getTitle() != roomPart.getTitle() else roomPart.getTitle() != null) return false
        return if (getEpisodes() != null) getEpisodes() == roomPart.getEpisodes() else roomPart.getEpisodes() == null
    }

    override fun hashCode(): Int {
        var result = getPartId()
        result = 31 * result + mediumId
        result = 31 * result + if (getTitle() != null) getTitle().hashCode() else 0
        result = 31 * result + getTotalIndex()
        result = 31 * result + getPartialIndex()
        result = 31 * result + if (getEpisodes() != null) getEpisodes().hashCode() else 0
        return result
    }

    override fun toString(): String {
        return "RoomPart{" +
                "partId=" + partId +
                ", mediumId=" + mediumId +
                ", title='" + title + '\'' +
                ", totalIndex=" + totalIndex +
                ", partialIndex=" + partialIndex +
                ", episodes=" + episodes +
                '}'
    }

    override fun getPartId(): Int {
        return partId
    }

    override fun getTitle(): String {
        return title
    }

    override fun getTotalIndex(): Int {
        return totalIndex
    }

    override fun getPartialIndex(): Int {
        return partialIndex
    }

    override fun getEpisodes(): List<Int> {
        return episodes
    }

    init {
        episodes = ArrayList()
    }
}