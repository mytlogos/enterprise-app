package com.mytlogos.enterprise.background.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.mytlogos.enterprise.background.room.model.RoomUser;
import com.mytlogos.enterprise.model.HomeStats;
import com.mytlogos.enterprise.model.User;


@Dao
public interface UserDao extends BaseDao<RoomUser> {

    @Query("SELECT * FROM RoomUser LIMIT 1")
    LiveData<User> getUser();

    @Query("SELECT name FROM RoomUser LIMIT 1")
    LiveData<String> getUserName();

    @Query("SELECT * FROM RoomUser LIMIT 1")
    RoomUser getUserNow();

    @Query("DELETE FROM RoomUser")
    void deleteAllUser();

    @Query("SELECT name, " +
            "(SELECT COUNT(mediumId) FROM RoomMedium WHERE medium = 1) as textMedia," +
            "(SELECT COUNT(mediumId) FROM RoomMedium WHERE medium = 2) as audioMedia," +
            "(SELECT COUNT(mediumId) FROM RoomMedium WHERE medium = 4) as videoMedia," +
            "(SELECT COUNT(mediumId) FROM RoomMedium WHERE medium = 8) as imageMedia," +
            "(SELECT COUNT(listId) FROM RoomMediaList) as internalLists," +
            "(SELECT COUNT(externalListId) FROM RoomExternalMediaList) as externalLists," +
            "(SELECT COUNT(uuid) FROM RoomExternalUser) as externalUser," +
            "(SELECT COUNT(episodeId) FROM RoomEpisode WHERE progress < 1) as unreadChapterCount," +
            "(SELECT COUNT(episodeId) FROM RoomEpisode WHERE date(readDate) = date('now') AND progress=1) as readTodayCount," +
            "(SELECT COUNT(episodeId) FROM RoomEpisode WHERE progress=1) as readTotalCount," +
            "(SELECT COUNT(newsId) FROM RoomNews WHERE read=0) as unreadNewsCount," +
            "(SELECT COUNT(*) FROM RoomMediumInWait) as unusedMedia " +
            "FROM RoomUser")
    LiveData<HomeStats> getHomeStats();
}
