package com.mytlogos.enterprise.background.room;

import androidx.room.Dao;
import androidx.room.Query;

import com.mytlogos.enterprise.background.room.model.RoomExternalUser;

import java.util.List;

@Dao
public interface ExternalUserDao extends MultiBaseDao<RoomExternalUser> {

    @Query("SELECT uuid FROM RoomExternalUser;")
    List<String> loaded();
}
