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
    suspend fun getPartJoin(mediumId: Int): List<Int>

    @Insert
    suspend fun addPartJoin(mediumParts: Collection<RoomMediumPart>)

    @Delete
    suspend fun deletePartJoin(mediumParts: Collection<RoomMediumPart>)

    @Query("DELETE FROM RoomMediumPart WHERE mediumId=:mediumId")
    suspend fun clearPartJoin(mediumId: Int)

    @Query("SELECT episodeId FROM RoomPartEpisode WHERE partId=:partId")
    suspend fun getEpisodeJoin(partId: Int): List<Int>

    @Query("DELETE FROM RoomPartEpisode WHERE partId=:partId")
    suspend fun clearEpisodeJoin(partId: Int)

    @Insert
    suspend fun addEpisodeJoin(partEpisodes: Collection<RoomPartEpisode>)

    @Delete
    suspend fun deleteEpisodeJoin(partEpisodes: Collection<RoomPartEpisode>)
}