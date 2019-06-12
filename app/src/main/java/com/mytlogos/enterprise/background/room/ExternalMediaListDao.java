package com.mytlogos.enterprise.background.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mytlogos.enterprise.background.room.model.RoomExternListView;
import com.mytlogos.enterprise.background.room.model.RoomExternalMediaList;
import com.mytlogos.enterprise.model.ExternalMediaListSetting;

import java.util.Collection;
import java.util.List;

@Dao
public interface ExternalMediaListDao extends MultiBaseDao<RoomExternalMediaList> {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addJoin(RoomExternalMediaList.ExternalListMediaJoin listMediaJoin);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addJoin(Collection<RoomExternalMediaList.ExternalListMediaJoin> collection);

    default void clearJoins(Collection<Integer> collection) {
        for (Integer integer : collection) {
            this.clearJoin(integer);
        }
    }

    @Query("DELETE FROM ExternalListMediaJoin WHERE listId=:listId")
    void clearJoin(Integer listId);

    @Delete
    void removeJoin(RoomExternalMediaList.ExternalListMediaJoin listMediaJoin);

    @Delete
    void removeJoin(Collection<RoomExternalMediaList.ExternalListMediaJoin> collection);

    @Query("SELECT externalListId FROM RoomExternalMediaList;")
    List<Integer> loaded();

    @Query("SELECT mediumId FROM ExternalListMediaJoin WHERE listId=:externalListId")
    List<Integer> getExternalListItems(Integer externalListId);

    @Query("SELECT RoomExternalMediaList.*, " +
            "(SELECT COUNT(*) FROM ExternalListMediaJoin WHERE RoomExternalMediaList.externalListId=ExternalListMediaJoin.listId) as size " +
            "FROM RoomExternalMediaList")
    LiveData<List<RoomExternListView>> getExternalListViews();


    @Query("SELECT RoomExternalMediaList.externalListId as listId,RoomExternalMediaList.uuid,url,medium,name,toDownload, " +
            "(SELECT COUNT(*) FROM ExternalListMediaJoin WHERE RoomExternalMediaList.externalListId=ExternalListMediaJoin.listId) as size " +
            "FROM RoomExternalMediaList " +
            "LEFT JOIN " +
            "(SELECT externalListId,1 as toDownload FROM RoomToDownload WHERE externalListId > 0) " +
            "as RoomToDownload ON RoomToDownload.externalListId=RoomExternalMediaList.externalListId " +
            "WHERE RoomExternalMediaList.externalListId=:id")
    LiveData<ExternalMediaListSetting> getExternalListSetting(int id);

    @Query("SELECT mediumId FROM ExternalListMediaJoin WHERE listId=:externalListId")
    LiveData<List<Integer>> getLiveExternalListItems(Integer externalListId);
}
