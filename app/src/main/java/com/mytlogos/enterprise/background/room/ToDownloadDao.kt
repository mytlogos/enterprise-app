package com.mytlogos.enterprise.background.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mytlogos.enterprise.background.room.model.RoomToDownload

@Dao
interface ToDownloadDao : MultiBaseDao<RoomToDownload> {
    @Query("SELECT * FROM RoomToDownload")
    suspend fun getAll(): List<RoomToDownload>

    @Query("DELETE FROM RoomToDownload WHERE listId is :listId AND externalListId is :externalListId AND mediumId is :mediumId")
    suspend fun deleteToDownload(mediumId: Int?, listId: Int?, externalListId: Int?)

    @Query("""SELECT COUNT(toDownloadId) FROM RoomToDownload
LEFT JOIN MediaListMediaJoin ON RoomToDownload.listId=MediaListMediaJoin.listId
LEFT JOIN ExternalListMediaJoin ON RoomToDownload.externalListId=ExternalListMediaJoin.listId """)
    fun countMediaRows(): LiveData<Int>
}