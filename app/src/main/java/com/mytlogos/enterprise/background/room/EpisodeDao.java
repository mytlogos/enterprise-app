package com.mytlogos.enterprise.background.room;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.mytlogos.enterprise.background.room.model.RoomEpisode;
import com.mytlogos.enterprise.background.room.model.RoomReadEpisode;
import com.mytlogos.enterprise.background.room.model.RoomRelease;
import com.mytlogos.enterprise.background.room.model.RoomTocEpisode;
import com.mytlogos.enterprise.background.room.model.RoomUnReadEpisode;
import com.mytlogos.enterprise.model.SimpleEpisode;

import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;

@Dao
public interface EpisodeDao extends MultiBaseDao<RoomEpisode> {

    @Query("UPDATE RoomEpisode SET progress=:progress, readDate=:readDate WHERE episodeId=:episodeId")
    void updateProgress(int episodeId, float progress, DateTime readDate);

    @Query("UPDATE RoomEpisode SET progress=:progress, readDate=:readDate WHERE episodeId IN (:episodeIds)")
    void updateProgress(Collection<Integer> episodeIds, float progress, DateTime readDate);

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
            "LEFT JOIN RoomFailedEpisode ON RoomFailedEpisode.episodeId=RoomEpisode.episodeId " +
            "INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId " +
            "INNER JOIN RoomMedium ON RoomPart.mediumId=RoomMedium.mediumId " +
            "WHERE " +
            "progress < 1 " +
            "AND saved = 0 " +
            "AND RoomPart.mediumId =:mediumId " +
            "AND RoomEpisode.episodeId IN (SELECT episodeId FROM RoomRelease WHERE locked=0)" +
            "ORDER BY " +
            "CASE RoomMedium.medium " +
            "WHEN 1 THEN 1 " +
            "WHEN 2 THEN 2 " +
            "WHEN 4 THEN 4 " +
            "WHEN 8 THEN 3 " +
            "ELSE 5 " +
            "END, " +
            "RoomEpisode.combiIndex LIMIT 50")
    List<Integer> getDownloadableEpisodes(Integer mediumId);

    @Query("SELECT RoomEpisode.episodeId FROM RoomEpisode " +
            "INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId " +
            "WHERE progress < 1 AND saved = 0 AND RoomPart.mediumId IN (:mediaIds) " +
            "ORDER BY mediumId, RoomEpisode.combiIndex")
    List<Integer> getDownloadableEpisodes(Collection<Integer> mediaIds);

    @Query("SELECT COUNT(RoomEpisode.episodeId) FROM RoomEpisode " +
            "INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId " +
            "WHERE saved = 1 AND RoomPart.mediumId =:mediumId")
    Integer countSavedEpisodes(Integer mediumId);

    @Query("SELECT episodeId FROM RoomEpisode " +
            "INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId " +
            "WHERE saved = 1 AND RoomPart.mediumId =:mediumId " +
            "ORDER BY RoomEpisode.combiIndex")
    List<Integer> getSavedEpisodes(int mediumId);

    @Transaction
    @Query("SELECT RoomEpisode.episodeId, saved, RoomEpisode.partialIndex, RoomEpisode.totalIndex,\n" +
            "RoomMedium.mediumId,RoomMedium.title as mediumTitle, \n" +
            "CASE RoomEpisode.progress WHEN 1 THEN 1 ELSE 0 END as read \n" +
            "FROM RoomEpisode \n" +
            "INNER JOIN RoomRelease ON RoomRelease.episodeId=RoomEpisode.episodeId \n" +
            "INNER JOIN RoomPart ON RoomEpisode.partId=RoomPart.partId \n" +
            "INNER JOIN RoomMedium ON RoomPart.mediumId=RoomMedium.mediumId \n" +
            "WHERE progress=0\n" +
            "AND (:medium = 0 OR (:medium & medium) > 0)\n" +
            "AND (:saved < 0 OR saved=:saved)\n" +
            "GROUP BY RoomEpisode.episodeId\n" +
            "ORDER BY RoomRelease.releaseDate DESC, RoomEpisode.combiIndex DESC")
    DataSource.Factory<Integer, RoomUnReadEpisode> getUnreadEpisodes(int saved, int medium);

    @Transaction
    @Query("SELECT * FROM \n" +
            "(\n" +
            "    SELECT RoomEpisode.episodeId, saved, RoomEpisode.partialIndex, RoomEpisode.totalIndex,\n" +
            "    RoomMedium.mediumId,RoomMedium.title as mediumTitle, \n" +
            "    CASE RoomEpisode.progress WHEN 1 THEN 1 ELSE 0 END as read \n" +
            "    FROM RoomEpisode \n" +
            "    INNER JOIN RoomPart ON RoomEpisode.partId=RoomPart.partId \n" +
            "    INNER JOIN RoomMedium ON RoomPart.mediumId=RoomMedium.mediumId \n" +
            "    WHERE progress=0\n" +
            "    AND (:medium = 0 OR (:medium & medium) > 0)\n" +
            "    ORDER BY RoomEpisode.combiIndex DESC\n" +
            ") as UnreadEpisode\n" +
            "WHERE (:saved < 0 OR saved=:saved)\n" +
            "GROUP BY UnreadEpisode.mediumId\n" +
            "ORDER BY (\n" +
            "    SELECT MIN(releaseDate) FROM RoomRelease WHERE RoomRelease.episodeId=UnreadEpisode.episodeId\n" +
            ") DESC")
    DataSource.Factory<Integer, RoomUnReadEpisode> getUnreadEpisodesGrouped(int saved, int medium);

    @Query("SELECT * FROM RoomEpisode WHERE episodeId=:episodeId")
    RoomEpisode getEpisode(int episodeId);

    @Query("SELECT episodeId, partialIndex, totalIndex, progress FROM RoomEpisode " +
            "WHERE episodeId IN (:ids) " +
            "ORDER BY combiIndex")
    List<SimpleEpisode> getSimpleEpisodes(Collection<Integer> ids);

    @Query("SELECT episodeId, partialIndex, totalIndex, progress FROM RoomEpisode WHERE episodeId =:episodeId")
    SimpleEpisode getSimpleEpisode(int episodeId);

    @Transaction
    @Query("SELECT RoomEpisode.episodeId, RoomEpisode.partialIndex, RoomEpisode.totalIndex," +
            "RoomMedium.mediumId,RoomMedium.title as mediumTitle " +
            "FROM RoomEpisode " +
            "INNER JOIN RoomPart ON RoomEpisode.partId=RoomPart.partId " +
            "INNER JOIN RoomMedium ON RoomPart.mediumId=RoomMedium.mediumId " +
            "WHERE date(readDate) = date('now') " +
            "ORDER BY RoomEpisode.readDate DESC")
    DataSource.Factory<Integer, RoomReadEpisode> getReadTodayEpisodes();

    @Query("SELECT COUNT(episodeId) FROM RoomEpisode " +
            "INNER JOIN RoomPart ON RoomEpisode.partId=RoomPart.partId " +
            "INNER JOIN RoomMedium ON RoomPart.mediumId=RoomMedium.mediumId " +
            "LEFT JOIN MediaListMediaJoin ON RoomMedium.mediumId=MediaListMediaJoin.mediumId " +
            "LEFT JOIN ExternalListMediaJoin ON RoomMedium.mediumId=ExternalListMediaJoin.mediumId " +
            "WHERE saved=0 AND progress < 1 " +
            "AND (RoomMedium.mediumId IN " +
            "(SELECT mediumId FROM RoomToDownload WHERE mediumId IS NOT NULL)" +
            "OR (ExternalListMediaJoin.listId IS NOT NULL " +
            "AND ExternalListMediaJoin.listId IN " +
            "(SELECT externalListId FROM RoomToDownload WHERE externalListId IS NOT NULL))" +
            "OR (MediaListMediaJoin.listId IS NOT NULL " +
            "AND MediaListMediaJoin.listId IN " +
            "(SELECT listId FROM RoomToDownload WHERE listId IS NOT NULL))) " +
            "AND episodeId IN (SELECT episodeId FROM RoomRelease WHERE locked=0)")
    LiveData<Integer> countDownloadableRows();

    @Transaction
    @Query("SELECT RoomEpisode.* FROM RoomEpisode " +
            "INNER JOIN RoomPart ON RoomEpisode.partId=RoomPart.partId " +
            "INNER JOIN RoomMedium ON RoomPart.mediumId=RoomMedium.mediumId " +
            "WHERE RoomMedium.mediumId=:mediumId " +
            "AND (:read < 0 OR (:read == 0 AND progress < 1) OR :read = progress)" +
            "AND (:saved < 0 OR :saved=saved)" +
            "ORDER BY RoomEpisode.combiIndex ASC")
    DataSource.Factory<Integer, RoomTocEpisode> getTocEpisodesAsc(int mediumId, byte read, byte saved);


    @Transaction
    @Query("SELECT RoomEpisode.* FROM RoomEpisode " +
            "INNER JOIN RoomPart ON RoomEpisode.partId=RoomPart.partId " +
            "INNER JOIN RoomMedium ON RoomPart.mediumId=RoomMedium.mediumId " +
            "WHERE RoomMedium.mediumId=:mediumId " +
            "AND (:read < 0 OR (:read == 0 AND progress < 1) OR :read = progress)" +
            "AND (:saved < 0 OR :saved=saved)" +
            "ORDER BY RoomEpisode.combiIndex DESC")
    DataSource.Factory<Integer, RoomTocEpisode> getTocEpisodesDesc(int mediumId, byte read, byte saved);

    @Query("SELECT url FROM RoomRelease WHERE episodeId=:episodeId")
    List<String> getReleaseLinks(int episodeId);

    @Query("SELECT episodeId FROM RoomEpisode " +
            "INNER JOIN RoomPart ON RoomEpisode.partId=RoomPart.partId " +
            "WHERE mediumId = :mediumId " +
            "AND CASE WHEN :toRead = 1 " +
            "THEN progress < 1 " +
            "ELSE progress = 1 " +
            "END " +
            "AND CASE WHEN :partCombiIndex < 0 " +
            "THEN RoomPart.totalIndex= -1 " +
            "ELSE RoomPart.combiIndex <= :partCombiIndex " +
            "END " +
            "AND RoomEpisode.combiIndex <= :episodeCombiIndex")
    List<Integer> getEpisodeIdsWithLowerIndex(int mediumId, double episodeCombiIndex, double partCombiIndex, boolean toRead);

    @Query("SELECT episodeId FROM RoomEpisode " +
            "INNER JOIN RoomPart ON RoomEpisode.partId=RoomPart.partId " +
            "WHERE mediumId = :mediumId " +
            "AND saved = 1 " +
            "AND CASE WHEN :partCombiIndex < 0 " +
            "THEN RoomPart.totalIndex= -1 " +
            "ELSE RoomPart.combiIndex <= :partCombiIndex " +
            "END " +
            "AND RoomEpisode.combiIndex <= :episodeCombiIndex")
    List<Integer> getSavedEpisodeIdsWithLowerIndex(int mediumId, double episodeCombiIndex, double partCombiIndex);

    @Query("DELETE FROM RoomRelease")
    void clearAllReleases();

    @Query("DELETE FROM RoomEpisode")
    void clearAll();

    @Query("SELECT episodeId FROM RoomPart " +
            "INNER JOIN RoomEpisode ON RoomPart.partId=RoomEpisode.partId " +
            "WHERE RoomPart.mediumId=:mediumId")
    List<Integer> getAllEpisodes(int mediumId);
}
