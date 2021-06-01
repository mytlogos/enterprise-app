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
    fun addJoin(listMediaJoin: ExternalListMediaJoin)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addJoin(collection: Collection<ExternalListMediaJoin>)
    fun clearJoins(collection: Collection<Int>) {
        for (integer in collection) {
            clearJoin(integer)
        }
    }

    @Query("DELETE FROM ExternalListMediaJoin WHERE listId=:listId")
    fun clearJoin(listId: Int)

    @Query("DELETE FROM ExternalListMediaJoin")
    fun clearJoins()

    @Delete
    fun removeJoin(listMediaJoin: ExternalListMediaJoin)

    @Delete
    fun removeJoin(collection: Collection<ExternalListMediaJoin>)

    @Query("SELECT externalListId FROM RoomExternalMediaList;")
    fun loaded(): List<Int>

    @Query("SELECT mediumId FROM ExternalListMediaJoin WHERE listId=:externalListId")
    fun getExternalListItems(externalListId: Int): List<Int>

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
    fun getExternalListSettingNow(id: Int): ExternalMediaListSetting

    @Query("SELECT mediumId FROM ExternalListMediaJoin WHERE listId=:externalListId")
    fun getLiveExternalListItems(externalListId: Int): LiveData<MutableList<Int>>

    @get:Query("SELECT DISTINCT mediumId FROM ExternalListMediaJoin")
    val allLinkedMedia: List<Int>

    @Query("SELECT COUNT(externalListId) FROM RoomExternalMediaList")
    fun countLists(): LiveData<Int>

    @get:Query("SELECT listId, mediumId FROM ExternalListMediaJoin")
    val listItems: List<ExternalListMediaJoin>

    @get:Query("SELECT externalListId as listId, uuid FROM RoomExternalMediaList")
    val listUser: List<RoomListUser>

    @Query("DELETE FROM RoomExternalMediaList WHERE externalListId IN (:listIds)")
    fun delete(listIds: Collection<Int>)
}