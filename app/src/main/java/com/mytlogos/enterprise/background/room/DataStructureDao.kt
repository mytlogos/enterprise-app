package com.mytlogos.enterprise.background.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.mytlogos.enterprise.background.room.model.RoomMediumPart
import com.mytlogos.enterprise.background.room.model.RoomPartEpisode

@Dao
interface DataStructureDao {
    @Query("SELECT partId FROM RoomMediumPart WHERE mediumId=:mediumId")
    fun getPartJoin(mediumId: Int): List<Int>

    @Insert
    fun addPartJoin(mediumParts: Collection<RoomMediumPart>)

    @Delete
    fun deletePartJoin(mediumParts: Collection<RoomMediumPart>)

    @Query("DELETE FROM RoomMediumPart WHERE mediumId=:mediumId")
    fun clearPartJoin(mediumId: Int)

    @Query("SELECT episodeId FROM RoomPartEpisode WHERE partId=:partId")
    fun getEpisodeJoin(partId: Int): List<Int>

    @Query("DELETE FROM RoomPartEpisode WHERE partId=:partId")
    fun clearEpisodeJoin(partId: Int)

    @Insert
    fun addEpisodeJoin(partEpisodes: Collection<RoomPartEpisode>)

    @Delete
    fun deleteEpisodeJoin(partEpisodes: Collection<RoomPartEpisode>)
}