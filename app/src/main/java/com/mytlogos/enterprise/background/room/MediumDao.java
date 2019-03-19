package com.mytlogos.enterprise.background.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.mytlogos.enterprise.background.room.model.RoomMedium;

import java.util.List;

@Dao
public interface MediumDao extends MultiBaseDao<RoomMedium> {

    @Query("SELECT mediumId FROM RoomMedium;")
    List<Integer> loaded();
}
