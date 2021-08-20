package com.mytlogos.enterprise.background.room

import androidx.room.Dao
import androidx.room.Query
import com.mytlogos.enterprise.background.room.model.RoomFailedEpisode
import com.mytlogos.enterprise.model.FailedEpisode

@Dao
interface FailedEpisodesDao : MultiBaseDao<RoomFailedEpisode> {
    @Query("SELECT * FROM RoomFailedEpisode")
    suspend fun getFailedEpisodes(): List<FailedEpisode>

    @Query("SELECT * FROM RoomFailedEpisode WHERE episodeId=:episodeId")
    suspend fun getFailedEpisode(episodeId: Int): RoomFailedEpisode?

    @Query("SELECT * FROM RoomFailedEpisode WHERE episodeId IN (:episodeIds)")
    suspend fun getFailedEpisodes(episodeIds: Collection<Int>): List<FailedEpisode>

    @Query("DELETE FROM RoomFailedEpisode")
    suspend fun clearAll()

    @Query("DELETE FROM RoomFailedEpisode WHERE episodeId IN (:episodeIds)")
    suspend fun deleteBulkPerId(episodeIds: Collection<Int>)
}