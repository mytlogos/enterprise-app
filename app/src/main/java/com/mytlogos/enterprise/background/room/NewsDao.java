package com.mytlogos.enterprise.background.room;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.mytlogos.enterprise.background.room.model.RoomNews;
import com.mytlogos.enterprise.model.News;

import org.joda.time.DateTime;

import java.util.List;

@Dao
public interface NewsDao {

    @Query("SELECT link as url, title, timeStamp, newsId as id, read FROM RoomNews ORDER BY timeStamp DESC")
    DataSource.Factory<Integer, News> getNews();

    @Query("SELECT * FROM RoomNews  ORDER BY timeStamp DESC LIMIT 100")
    List<RoomNews> getCurrentNews();

    @Query("DELETE FROM RoomNews WHERE timeStamp >= :from AND timeStamp <= :to")
    void deleteNews(DateTime from, DateTime to);

    // todo test this
    @Query("DELETE FROM RoomNews WHERE timeStamp < strftime('%y-%m-%dT%H:%M:%f', datetime('now','-30 day'))")
    void deleteOldNews();

    @Query("SELECT * FROM RoomNews WHERE read=0  ORDER BY timeStamp DESC")
    LiveData<List<RoomNews>> geUnreadNews();

    @Insert
    void insertNews(RoomNews user);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertNews(List<RoomNews> user);

    @Update
    void updateNews(RoomNews user);

    @Update
    void updateNews(List<RoomNews> user);

    @Query("SELECT newsId FROM RoomNews;")
    List<Integer> loaded();

    @Query("SELECT COUNT(newsId) FROM RoomNews WHERE read=0")
    LiveData<Integer> countUnreadNews();
}
