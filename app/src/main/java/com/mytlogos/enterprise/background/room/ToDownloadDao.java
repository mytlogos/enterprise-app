package com.mytlogos.enterprise.background.room;

import androidx.room.Dao;
import androidx.room.Query;

import com.mytlogos.enterprise.background.room.model.RoomToDownload;

import java.util.List;

@Dao
public interface ToDownloadDao extends MultiBaseDao<RoomToDownload> {
    @Query("SELECT * FROM RoomToDownload")
    List<RoomToDownload> getAll();

    @Query("DELETE FROM RoomToDownload WHERE listId is :listId AND externalListId is :externalListId AND mediumId is :mediumId")
    void deleteToDownload(Integer mediumId, Integer listId, Integer externalListId);
}
