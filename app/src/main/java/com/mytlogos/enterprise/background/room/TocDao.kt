package com.mytlogos.enterprise.background.room

import androidx.room.Dao
import androidx.room.Query
import com.mytlogos.enterprise.background.room.model.RoomToc
import com.mytlogos.enterprise.background.room.model.RoomTocStat

@Dao
interface TocDao : MultiBaseDao<RoomToc> {
    @get:Query("SELECT mediumId, count(link) as tocCount FROM RoomToc GROUP BY mediumId;")
    val stat: List<RoomTocStat>

    @Query("SELECT mediumId, link FROM RoomToc WHERE mediumId IN (:mediumIds)")
    fun getTocs(mediumIds: Collection<Int>): List<RoomToc>

    @get:Query("SELECT mediumId, link FROM RoomToc")
    val tocs: List<RoomToc>
}