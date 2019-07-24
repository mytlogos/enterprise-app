package com.mytlogos.enterprise.background.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.mytlogos.enterprise.background.room.model.RoomMediumInWait;

import java.util.List;

@Dao
public interface RoomMediumInWaitDao extends MultiBaseDao<RoomMediumInWait> {
    @Query("SELECT * FROM RoomMediumInWait")
    LiveData<List<RoomMediumInWait>> getAll();
}
