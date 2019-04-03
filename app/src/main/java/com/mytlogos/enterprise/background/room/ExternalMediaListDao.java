package com.mytlogos.enterprise.background.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.mytlogos.enterprise.background.room.model.RoomExternalMediaList;

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
}
