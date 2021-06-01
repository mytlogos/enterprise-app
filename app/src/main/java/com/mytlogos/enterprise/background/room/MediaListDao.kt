package com.mytlogos.enterprise.background.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mytlogos.enterprise.background.room.model.RoomMediaList
import com.mytlogos.enterprise.background.room.model.RoomMediaList.MediaListMediaJoin
import com.mytlogos.enterprise.model.MediaList
import com.mytlogos.enterprise.model.MediaListSetting

@Dao
interface MediaListDao : MultiBaseDao<RoomMediaList> {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addJoin(listMediaJoin: MediaListMediaJoin)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addJoin(collection: Collection<MediaListMediaJoin>)
    fun clearJoins(collection: Collection<Int>) {
        for (integer in collection) {
            clearJoin(integer)
        }
    }

    @Query("DELETE FROM MediaListMediaJoin WHERE listId=:listId")
    fun clearJoin(listId: Int)

    @Query("DELETE FROM MediaListMediaJoin")
    fun clearJoins()

    @Delete
    fun removeJoin(listMediaJoin: MediaListMediaJoin)

    @Delete
    fun removeJoin(collection: Collection<MediaListMediaJoin>)

    @Query("SELECT listId FROM RoomMediaList;")
    fun loaded(): List<Int>

    @Query("SELECT mediumId FROM MediaListMediaJoin WHERE listId=:listId")
    fun getListItems(listId: Int): List<Int>

    @get:Query("SELECT listId,mediumId FROM MediaListMediaJoin")
    val listItems: List<MediaListMediaJoin>

    @Query("SELECT mediumId FROM MediaListMediaJoin WHERE listId=:listId")
    fun getLiveListItems(listId: Int): LiveData<MutableList<Int>>

    @get:Query("SELECT RoomMediaList.*, " +
            "(SELECT COUNT(*) FROM MediaListMediaJoin WHERE RoomMediaList.listId=MediaListMediaJoin.listId) as size " +
            "FROM RoomMediaList")
    val listViews: LiveData<MutableList<MediaList>>

    @Query("SELECT RoomMediaList.listId,RoomMediaList.uuid,medium,name,toDownload, " +
            "   (SELECT COUNT(*) FROM MediaListMediaJoin WHERE RoomMediaList.listId=MediaListMediaJoin.listId) as size " +
            "FROM RoomMediaList " +
            "LEFT JOIN " +
            "   (SELECT listId,1 as toDownload FROM RoomToDownload WHERE listId > 0) " +
            "as RoomToDownload ON RoomToDownload.listId=RoomMediaList.listId " +
            "WHERE RoomMediaList.listId=:id")
    fun getListSettings(id: Int): LiveData<MediaListSetting>

    @Query("SELECT RoomMediaList.listId,RoomMediaList.uuid,medium,name,toDownload, " +
            "   (SELECT COUNT(*) FROM MediaListMediaJoin WHERE RoomMediaList.listId=MediaListMediaJoin.listId) as size " +
            "FROM RoomMediaList " +
            "LEFT JOIN " +
            "   (SELECT listId,1 as toDownload FROM RoomToDownload WHERE listId > 0) " +
            "as RoomToDownload ON RoomToDownload.listId=RoomMediaList.listId " +
            "WHERE RoomMediaList.listId=:id")
    fun getListSettingsNow(id: Int): MediaListSetting

    @Query("SELECT 1 WHERE :listName IN (SELECT name FROM RoomMediaList)")
    fun listExists(listName: String): Boolean

    @Query("SELECT RoomMediaList.*, 0 as size FROM RoomMediaList WHERE :name IS NULL OR INSTR(lower(name), :name) > 0 LIMIT 5")
    fun getSuggestion(name: String): LiveData<MutableList<MediaList>>

    @get:Query("SELECT DISTINCT mediumId FROM MediaListMediaJoin")
    val allLinkedMedia: List<Int>

    @Transaction
    fun moveJoins(oldJoins: Collection<MediaListMediaJoin>, newJoins: Collection<MediaListMediaJoin>) {
        this.removeJoin(oldJoins)
        this.addJoin(newJoins)
    }

    @Query("SELECT COUNT(listId) FROM RoomMediaList")
    fun countLists(): LiveData<Int>

    @Query("DELETE FROM MediaListMediaJoin WHERE listId=:listId AND mediumId IN (:mediumId)")
    fun removeJoin(listId: Int, mediumId: Collection<Int>)

    @Query("DELETE FROM RoomMediaList WHERE listId IN (:listIds)")
    fun delete(listIds: Collection<Int>)
}