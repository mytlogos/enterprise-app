package com.mytlogos.enterprise.background.room;

import androidx.room.Dao;
import androidx.room.Query;

import com.mytlogos.enterprise.background.room.model.RoomEditEvent;

import java.util.List;

@Dao
public interface EditDao extends MultiBaseDao<RoomEditEvent> {
    @Query("SELECT * FROM RoomEditEvent")
    List<RoomEditEvent> getAll();

    @Query(
            "SELECT * FROM RoomEditEvent " +
                    "WHERE (:id <= 0 OR id=:id) " +
                    "AND (:objectType <= 0 OR objectType=:objectType)" +
                    "AND (:eventType <= 0 OR eventType=:eventType)"
    )
    List<RoomEditEvent> getAll(int id, int objectType, int eventType);
}
