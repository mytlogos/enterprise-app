package com.mytlogos.enterprise.background.room;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import com.mytlogos.enterprise.background.room.model.RoomUser;
import com.mytlogos.enterprise.background.room.modelImpl.UserImpl;

import java.util.Collection;


@Dao
public interface UserDao extends BaseDao<RoomUser> {

    @Transaction
    @Query("SELECT * FROM RoomUser")
    LiveData<UserImpl> getUser();

    @Transaction
    @Query("SELECT * FROM RoomUser")
    UserImpl getCurrentUser();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addUnReadNews(Collection<RoomUser.UserUnReadNewsJoin> unReadNewsJoins);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addUnReadNews(RoomUser.UserUnReadNewsJoin unReadNewsJoin);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addUnReadChapter(Collection<RoomUser.UserUnReadChapterJoin> unReadChapterJoins);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addUnReadChapter(RoomUser.UserUnReadChapterJoin unReadChapterJoin);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addReadToday(Collection<RoomUser.UserReadTodayJoin> readTodayJoins);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addReadToday(RoomUser.UserReadTodayJoin readTodayJoin);

    @Delete
    void deleteReadToday(RoomUser.UserReadTodayJoin readTodayJoin);

    @Delete
    void deleteUnReadChapter(RoomUser.UserUnReadChapterJoin unReadChapterJoin);

    @Delete
    void deleteUnReadNews(RoomUser.UserUnReadNewsJoin unReadNewsJoin);


   /* @Query("SELECT * FROM RoomMediaList WHERE uuid=:uuid;")
    RoomMediaList getMediaLists(String uuid);

    @Query("SELECT * FROM RoomExternalUser WHERE uuid=:uuid;")
    RoomExternalUser getExternalUser(String uuid);*/

    @Query("DELETE FROM RoomUser")
    void deleteAllUser();
}
