package com.mytlogos.enterprise.background.room;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.mytlogos.enterprise.background.room.model.RoomNews;

import org.joda.time.DateTime;

import java.util.List;

@Dao
public interface NewsDao {

    @Query("SELECT * FROM roomnews")
    LiveData<List<RoomNews>> getNews();

    @Query("DELETE FROM roomnews WHERE timeStamp >= :from AND timeStamp <= :to")
    void deleteNews(DateTime from, DateTime to);

    @Query("DELETE FROM roomnews")
    void deleteOldNews();

    @Insert
    void insertNews(RoomNews user);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertNews(List<RoomNews> user);

    @Query("SELECT newsId FROM RoomNews;")
    List<Integer> loaded();
}
