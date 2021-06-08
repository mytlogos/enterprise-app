package com.mytlogos.enterprise.background.room

import androidx.paging.DataSource
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.room.*
import com.mytlogos.enterprise.background.room.model.RoomNotification
import com.mytlogos.enterprise.model.NotificationItem

@Dao
interface NotificationDao : MultiBaseDao<RoomNotification> {
    @get:Query("SELECT * FROM RoomNotification ORDER BY dateTime DESC, title DESC")
    val notifications: PagingSource<Int, NotificationItem>

    @Query("DELETE FROM RoomNotification")
    suspend fun deleteAll()
}