package com.mytlogos.enterprise.background.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.mytlogos.enterprise.background.room.model.RoomEpisode;
import com.mytlogos.enterprise.background.room.model.RoomRelease;
import com.mytlogos.enterprise.background.room.model.RoomUnReadEpisode;

import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;

@Dao
public interface EpisodeDao extends MultiBaseDao<RoomEpisode> {

    @Query("UPDATE RoomEpisode SET progress=:progress, readDate=:readDate WHERE episodeId=:episodeId")
    void update(int episodeId, float progress, DateTime readDate);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBulkRelease(Collection<RoomRelease> collection);

    @Update
    void updateBulkRelease(Collection<RoomRelease> collection);

    @Delete
    void deleteBulkRelease(Collection<RoomRelease> collection);

    @Query("SELECT episodeId FROM RoomEpisode;")
    List<Integer> loaded();

    @Query("SELECT episodeId FROM RoomEpisode WHERE saved=1;")
    List<Integer> getAllSavedEpisodes();

    @Query("SELECT episodeId FROM RoomEpisode WHERE progress = 1 AND saved = 1")
    List<Integer> getAllToDeleteLocalEpisodes();

    @Query("UPDATE RoomEpisode SET saved=:saved WHERE episodeId=:episodeId")
    void updateSaved(int episodeId, boolean saved);

    @Query("UPDATE RoomEpisode SET saved=:saved WHERE episodeId IN (:episodeIds)")
    void updateSaved(Collection<Integer> episodeIds, boolean saved);

    @Query("SELECT RoomEpisode.episodeId FROM RoomEpisode " +
            "INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId " +
            "WHERE progress < 1 AND saved = 0 AND RoomPart.mediumId =:mediumId")
    List<Integer> getDownloadableEpisodes(Integer mediumId);

    @Query("SELECT RoomEpisode.episodeId FROM RoomEpisode " +
            "INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId " +
            "WHERE progress < 1 AND saved = 0 AND RoomPart.mediumId IN (:mediaIds)")
    List<Integer> getDownloadableEpisodes(Collection<Integer> mediaIds);

    @Query("SELECT RoomEpisode.episodeId, saved, RoomEpisode.partialIndex, RoomEpisode.totalIndex," +
            "RoomMedium.mediumId,RoomMedium.title as mediumTitle, RoomRelease.releaseDate, " +
            "RoomRelease.url, RoomRelease.title,RoomPart.partId, " +
            "CASE RoomEpisode.progress WHEN 1 THEN 1 ELSE 0 END as read " +
            "FROM RoomEpisode " +
            "INNER JOIN (SELECT * FROM RoomRelease GROUP BY episodeId,releaseDate ORDER BY releaseDate DESC) " +
            "as RoomRelease ON RoomRelease.episodeId=RoomEpisode.episodeId " +
            "INNER JOIN RoomPart ON RoomEpisode.partId=RoomPart.partId " +
            "INNER JOIN RoomMedium ON RoomPart.mediumId=RoomMedium.mediumId " +
            "WHERE progress=0 " +
            "ORDER BY releaseDate DESC")
    LiveData<List<RoomUnReadEpisode>> getUnreadEpisodes();

    @Query("SELECT RoomEpisode.episodeId, saved, RoomEpisode.partialIndex, RoomEpisode.totalIndex," +
            "RoomMedium.mediumId,RoomMedium.title as mediumTitle,RoomRelease.releaseDate, " +
            "RoomRelease.url, RoomRelease.title, RoomPart.partId, " +
            "CASE RoomEpisode.progress WHEN 1 THEN 1 ELSE 0 END as read " +
            "FROM RoomEpisode " +
            "INNER JOIN (SELECT * FROM RoomRelease GROUP BY episodeId,releaseDate ORDER BY releaseDate DESC) " +
            "as RoomRelease ON RoomRelease.episodeId=RoomEpisode.episodeId " +
            "INNER JOIN RoomPart ON RoomEpisode.partId=RoomPart.partId " +
            "INNER JOIN RoomMedium ON RoomPart.mediumId=RoomMedium.mediumId " +
            "WHERE RoomMedium.mediumId=:mediumId " +
            "ORDER BY RoomEpisode.totalIndex DESC, RoomEpisode.partialIndex DESC")
    LiveData<List<RoomUnReadEpisode>> getEpisodes(int mediumId);

}
