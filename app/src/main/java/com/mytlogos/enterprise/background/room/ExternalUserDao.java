package com.mytlogos.enterprise.background.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.mytlogos.enterprise.background.room.model.RoomExternalUser;

import java.util.List;

@Dao
public interface ExternalUserDao extends MultiBaseDao<RoomExternalUser> {
    @Query("SELECT uuid FROM RoomExternalUser;")
    List<String> loaded();
}
