package com.mytlogos.enterprise.background.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.mytlogos.enterprise.background.room.model.RoomPart;

import java.util.List;

@Dao
public interface PartDao extends MultiBaseDao<RoomPart> {

    @Query("SELECT partId FROM RoomPart;")
    List<Integer> loaded();

    @Query("SELECT * FROM RoomPart WHERE mediumId=:mediumId ORDER BY totalIndex DESC, partialIndex DESC")
    LiveData<List<RoomPart>> getParts(int mediumId);
}
