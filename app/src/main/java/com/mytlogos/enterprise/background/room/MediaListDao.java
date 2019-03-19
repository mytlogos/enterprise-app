package com.mytlogos.enterprise.background.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.mytlogos.enterprise.background.room.model.RoomMediaList;

import java.util.Collection;
import java.util.List;

@Dao
public interface MediaListDao extends MultiBaseDao<RoomMediaList> {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addJoin(RoomMediaList.MediaListMediaJoin listMediaJoin);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addJoin(Collection<RoomMediaList.MediaListMediaJoin> collection);

    @Delete
    void removeJoin(RoomMediaList.MediaListMediaJoin listMediaJoin);

    @Delete
    void removeJoin(Collection<RoomMediaList.MediaListMediaJoin> collection);

    @Query("SELECT listId FROM RoomMediaList;")
    List<Integer> loaded();
}
