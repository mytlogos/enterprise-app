package com.mytlogos.enterprise.background.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mytlogos.enterprise.background.room.model.RoomExternListView
import com.mytlogos.enterprise.background.room.model.RoomExternalMediaList
import com.mytlogos.enterprise.background.room.model.RoomExternalMediaList.ExternalListMediaJoin
import com.mytlogos.enterprise.background.room.model.RoomListUser
import com.mytlogos.enterprise.model.ExternalMediaListSetting

@Dao
interface ExternalMediaListDao : MultiBaseDao<RoomExternalMediaList?> {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addJoin(listMediaJoin: ExternalListMediaJoin)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addJoin(collection: Collection<ExternalListMediaJoin>)

    suspend fun clearJoins(collection: Collection<Int>) {
        for (integer in collection) {
            clearJoin(integer)
        }
    }

    @Query("DELETE FROM ExternalListMediaJoin WHERE listId=:listId")
    suspend fun clearJoin(listId: Int)

    @Query("DELETE FROM ExternalListMediaJoin")
    suspend fun clearJoins()

    @Delete
    suspend fun removeJoin(listMediaJoin: ExternalListMediaJoin)

    @Delete
    suspend fun removeJoin(collection: Collection<ExternalListMediaJoin>)

    @Query("SELECT externalListId FROM RoomExternalMediaList;")
    suspend fun loaded(): List<Int>

    @Query("SELECT mediumId FROM ExternalListMediaJoin WHERE listId=:externalListId")
    suspend fun getExternalListItems(externalListId: Int): List<Int>

    @get:Query("SELECT RoomExternalMediaList.*, " +
            "(SELECT COUNT(*) FROM ExternalListMediaJoin WHERE RoomExternalMediaList.externalListId=ExternalListMediaJoin.listId) as size " +
            "FROM RoomExternalMediaList")
    val externalListViews: LiveData<MutableList<RoomExternListView>>

    @Query("SELECT RoomExternalMediaList.externalListId as listId,RoomExternalMediaList.uuid,url,medium,name,toDownload, " +
            "(SELECT COUNT(*) FROM ExternalListMediaJoin WHERE RoomExternalMediaList.externalListId=ExternalListMediaJoin.listId) as size " +
            "FROM RoomExternalMediaList " +
            "LEFT JOIN " +
            "(SELECT externalListId,1 as toDownload FROM RoomToDownload WHERE externalListId > 0) " +
            "as RoomToDownload ON RoomToDownload.externalListId=RoomExternalMediaList.externalListId " +
            "WHERE RoomExternalMediaList.externalListId=:id")
    fun getExternalListSetting(id: Int): LiveData<ExternalMediaListSetting>

    @Query("SELECT RoomExternalMediaList.externalListId as listId,RoomExternalMediaList.uuid,url,medium,name,toDownload, " +
            "(SELECT COUNT(*) FROM ExternalListMediaJoin WHERE RoomExternalMediaList.externalListId=ExternalListMediaJoin.listId) as size " +
            "FROM RoomExternalMediaList " +
            "LEFT JOIN " +
            "(SELECT externalListId,1 as toDownload FROM RoomToDownload WHERE externalListId > 0) " +
            "as RoomToDownload ON RoomToDownload.externalListId=RoomExternalMediaList.externalListId " +
            "WHERE RoomExternalMediaList.externalListId=:id")
    suspend fun getExternalListSettingNow(id: Int): ExternalMediaListSetting

    @Query("SELECT mediumId FROM ExternalListMediaJoin WHERE listId=:externalListId")
    fun getLiveExternalListItems(externalListId: Int): LiveData<MutableList<Int>>

    @Query("SELECT DISTINCT mediumId FROM ExternalListMediaJoin")
    suspend fun getAllLinkedMedia(): List<Int>

    @Query("SELECT COUNT(externalListId) FROM RoomExternalMediaList")
    fun countLists(): LiveData<Int>

    @Query("SELECT listId, mediumId FROM ExternalListMediaJoin")
    suspend fun getListItems(): List<ExternalListMediaJoin>

    @Query("SELECT externalListId as listId, uuid FROM RoomExternalMediaList")
    suspend fun getListUser(): List<RoomListUser>

    @Query("DELETE FROM RoomExternalMediaList WHERE externalListId IN (:listIds)")
    suspend fun delete(listIds: Collection<Int>)
}