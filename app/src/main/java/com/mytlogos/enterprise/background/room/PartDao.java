package com.mytlogos.enterprise.background.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.mytlogos.enterprise.background.room.model.RoomPart;

import java.util.List;

@Dao
public interface PartDao extends MultiBaseDao<RoomPart> {

    @Query("SELECT partId FROM RoomPart;")
    List<Integer> loaded();
}
