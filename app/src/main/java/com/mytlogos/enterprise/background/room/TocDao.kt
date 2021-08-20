package com.mytlogos.enterprise.background.room

import androidx.room.Dao
import androidx.room.Query
import com.mytlogos.enterprise.background.room.model.RoomToc
import com.mytlogos.enterprise.background.room.model.RoomTocStat

@Dao
interface TocDao : MultiBaseDao<RoomToc> {
    @Query("SELECT mediumId, count(link) as tocCount FROM RoomToc GROUP BY mediumId;")
    suspend fun getStat(): List<RoomTocStat>

    @Query("SELECT mediumId, link FROM RoomToc WHERE mediumId IN (:mediumIds)")
    suspend fun getTocs(mediumIds: Collection<Int>): List<RoomToc>

    @Query("SELECT mediumId, link FROM RoomToc")
    suspend fun getTocs(): List<RoomToc>
}