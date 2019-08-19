package com.mytlogos.enterprise.background.room;

import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Query;

import com.mytlogos.enterprise.background.room.model.RoomNotification;
import com.mytlogos.enterprise.model.NotificationItem;

@Dao
public interface NotificationDao extends MultiBaseDao<RoomNotification> {

    @Query("SELECT * FROM RoomNotification ORDER BY dateTime DESC, title DESC")
    DataSource.Factory<Integer, NotificationItem> getNotifications();

    @Query("DELETE FROM RoomNotification")
    void deleteAll();
}
