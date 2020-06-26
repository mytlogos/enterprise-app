package com.mytlogos.enterprise.background.room;

import androidx.room.Dao;
import androidx.room.Query;

import com.mytlogos.enterprise.background.room.model.RoomToc;
import com.mytlogos.enterprise.background.room.model.RoomTocStat;

import java.util.Collection;
import java.util.List;

@Dao
interface TocDao extends MultiBaseDao<RoomToc> {
    @Query("SELECT mediumId, count(link) as tocCount FROM RoomToc GROUP BY mediumId;")
    List<RoomTocStat> getStat();

    @Query("SELECT mediumId, link FROM RoomToc WHERE mediumId IN (:mediumIds)")
    List<RoomToc> getTocs(Collection<Integer> mediumIds);

    @Query("SELECT mediumId, link FROM RoomToc")
    List<RoomToc> getTocs();
}
