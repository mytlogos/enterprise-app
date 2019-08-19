package com.mytlogos.enterprise.background.room;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Query;

import com.mytlogos.enterprise.background.room.model.RoomExternalUser;
import com.mytlogos.enterprise.model.ExternalUser;

import java.util.List;

@Dao
public interface ExternalUserDao extends MultiBaseDao<RoomExternalUser> {

    @Query("SELECT uuid FROM RoomExternalUser;")
    List<String> loaded();

    @Query("SELECT RoomExternalUser.uuid,RoomExternalUser.identifier, RoomExternalUser.type FROM RoomExternalUser")
    DataSource.Factory<Integer, ExternalUser> getAll();

    @Query("SELECT COUNT(uuid) FROM RoomExternalUser")
    LiveData<Integer> countUser();
}
