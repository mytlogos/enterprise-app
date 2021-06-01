package com.mytlogos.enterprise.background.room

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.mytlogos.enterprise.background.room.model.RoomNews
import com.mytlogos.enterprise.model.News
import org.joda.time.DateTime

@Dao
interface NewsDao {
    @get:Query("SELECT link as url, title, timeStamp, newsId as id, read FROM RoomNews ORDER BY timeStamp DESC")
    val news: DataSource.Factory<Int, News>

    @get:Query("SELECT * FROM RoomNews  ORDER BY timeStamp DESC LIMIT 100")
    val currentNews: List<RoomNews>

    @Query("DELETE FROM RoomNews WHERE timeStamp >= :from AND timeStamp <= :to")
    fun deleteNews(from: DateTime, to: DateTime)

    // todo test this
    @Query("DELETE FROM RoomNews WHERE timeStamp < strftime('%y-%m-%dT%H:%M:%f', datetime('now','-30 day'))")
    fun deleteOldNews()

    @Query("SELECT * FROM RoomNews WHERE read=0  ORDER BY timeStamp DESC")
    fun geUnreadNews(): LiveData<MutableList<RoomNews>>

    @Insert
    fun insertNews(user: RoomNews)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNews(user: List<RoomNews>)

    @Update
    fun updateNews(user: RoomNews)

    @Update
    fun updateNews(user: List<RoomNews>)

    @Query("SELECT newsId FROM RoomNews;")
    fun loaded(): List<Int>

    @Query("SELECT COUNT(newsId) FROM RoomNews WHERE read=0")
    fun countUnreadNews(): LiveData<Int>
}