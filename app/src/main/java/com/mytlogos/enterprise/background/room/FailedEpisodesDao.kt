package com.mytlogos.enterprise.background.room

import androidx.room.Dao
import androidx.room.Query
import com.mytlogos.enterprise.background.room.model.RoomFailedEpisode
import com.mytlogos.enterprise.model.FailedEpisode

@Dao
interface FailedEpisodesDao : MultiBaseDao<RoomFailedEpisode> {
    @get:Query("SELECT * FROM RoomFailedEpisode")
    val failedEpisodes: List<FailedEpisode>

    @Query("SELECT * FROM RoomFailedEpisode WHERE episodeId=:episodeId")
    fun getFailedEpisode(episodeId: Int): RoomFailedEpisode?

    @Query("SELECT * FROM RoomFailedEpisode WHERE episodeId IN (:episodeIds)")
    fun getFailedEpisodes(episodeIds: Collection<Int>): List<FailedEpisode>

    @Query("DELETE FROM RoomFailedEpisode")
    fun clearAll()

    @Query("DELETE FROM RoomFailedEpisode WHERE episodeId IN (:episodeIds)")
    fun deleteBulkPerId(episodeIds: Collection<Int>)
}