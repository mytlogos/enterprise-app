package com.mytlogos.enterprise.background.room;

import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Query;

import com.mytlogos.enterprise.background.room.model.RoomWorkerEvent;
import com.mytlogos.enterprise.model.WorkerEvent;

@Dao
public interface WorkerEventDao extends MultiBaseDao<RoomWorkerEvent> {
    @Query("SELECT * FROM RoomWorkerEvent ORDER BY dateTime DESC")
    DataSource.Factory<Integer, WorkerEvent> getAll();

    @Query("DELETE FROM RoomWorkerEvent")
    void clearAll();
}
