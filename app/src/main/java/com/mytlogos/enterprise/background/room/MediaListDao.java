package com.mytlogos.enterprise.background.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.mytlogos.enterprise.background.room.model.RoomMediaList;
import com.mytlogos.enterprise.model.MediaList;
import com.mytlogos.enterprise.model.MediaListSetting;

import java.util.Collection;
import java.util.List;

@Dao
public interface MediaListDao extends MultiBaseDao<RoomMediaList> {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addJoin(RoomMediaList.MediaListMediaJoin listMediaJoin);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addJoin(Collection<RoomMediaList.MediaListMediaJoin> collection);

    default void clearJoins(Collection<Integer> collection) {
        for (Integer integer : collection) {
            this.clearJoin(integer);
        }
    }

    @Query("DELETE FROM MediaListMediaJoin WHERE listId=:listId")
    void clearJoin(Integer listId);

    @Query("DELETE FROM MediaListMediaJoin")
    void clearJoins();

    @Delete
    void removeJoin(RoomMediaList.MediaListMediaJoin listMediaJoin);

    @Delete
    void removeJoin(Collection<RoomMediaList.MediaListMediaJoin> collection);

    @Query("SELECT listId FROM RoomMediaList;")
    List<Integer> loaded();

    @Query("SELECT mediumId FROM MediaListMediaJoin WHERE listId=:listId")
    List<Integer> getListItems(Integer listId);

    @Query("SELECT mediumId FROM MediaListMediaJoin WHERE listId=:listId")
    LiveData<List<Integer>> getLiveListItems(Integer listId);

    @Query("SELECT RoomMediaList.*, " +
            "(SELECT COUNT(*) FROM MediaListMediaJoin WHERE RoomMediaList.listId=MediaListMediaJoin.listId) as size " +
            "FROM RoomMediaList")
    LiveData<List<MediaList>> getListViews();

    @Query("SELECT RoomMediaList.listId,RoomMediaList.uuid,medium,name,toDownload, " +
            "   (SELECT COUNT(*) FROM MediaListMediaJoin WHERE RoomMediaList.listId=MediaListMediaJoin.listId) as size " +
            "FROM RoomMediaList " +
            "LEFT JOIN " +
            "   (SELECT listId,1 as toDownload FROM RoomToDownload WHERE listId > 0) " +
            "as RoomToDownload ON RoomToDownload.listId=RoomMediaList.listId " +
            "WHERE RoomMediaList.listId=:id")
    LiveData<MediaListSetting> getListSettings(int id);

    @Query("SELECT 1 WHERE :listName IN (SELECT name FROM RoomMediaList)")
    boolean listExists(String listName);

    @Query("SELECT RoomMediaList.*, 0 as size FROM RoomMediaList WHERE :name IS NULL OR INSTR(lower(name), :name) > 0 LIMIT 5")
    LiveData<List<MediaList>> getSuggestion(String name);


    @Query("SELECT DISTINCT mediumId FROM MediaListMediaJoin")
    List<Integer> getAllLinkedMedia();

    @Transaction
    default void moveJoins(Collection<RoomMediaList.MediaListMediaJoin> oldJoins, Collection<RoomMediaList.MediaListMediaJoin> newJoins) {
        this.removeJoin(oldJoins);
        this.addJoin(newJoins);

    }

    @Query("SELECT COUNT(listId) FROM RoomMediaList")
    LiveData<Integer> countLists();
}
