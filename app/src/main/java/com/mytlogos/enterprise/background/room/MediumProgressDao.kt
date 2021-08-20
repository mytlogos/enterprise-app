package com.mytlogos.enterprise.background.room

import androidx.room.Dao
import androidx.room.Query
import com.mytlogos.enterprise.background.room.model.RoomMediumProgress
import com.mytlogos.enterprise.background.room.model.RoomProgressComparison

@Dao
interface MediumProgressDao : MultiBaseDao<RoomMediumProgress> {
    @Query("""SELECT RoomMediumProgress.mediumId, currentReadIndex, MAX(RoomEpisode.combiIndex) as currentMaxReadIndex FROM RoomMediumProgress
INNER JOIN RoomPart ON RoomMediumProgress.mediumId=RoomPart.mediumId
INNER JOIN RoomEpisode ON RoomPart.partId=RoomEpisode.partId
WHERE progress = 1""")
    suspend fun getComparison(): List<RoomProgressComparison>
}