package com.mytlogos.enterprise.background.room

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.PagingSource
import androidx.room.*
import com.mytlogos.enterprise.background.room.model.*
import com.mytlogos.enterprise.model.DisplayRelease
import com.mytlogos.enterprise.model.SimpleEpisode
import org.joda.time.DateTime

@Dao
interface EpisodeDao : MultiBaseDao<RoomEpisode> {
    @Update(entity = RoomEpisode::class)
    fun updateBulkClient(episodes: Collection<ClientRoomEpisode>)

    @Query("UPDATE RoomEpisode SET progress=:progress, readDate=:readDate WHERE episodeId=:episodeId")
    fun updateProgress(episodeId: Int, progress: Float, readDate: DateTime)

    @Query("UPDATE RoomEpisode SET progress=:progress, readDate=:readDate WHERE episodeId IN (:episodeIds)")
    fun updateProgress(episodeIds: Collection<Int>, progress: Float, readDate: DateTime)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBulkRelease(collection: Collection<RoomRelease>)

    @Update
    fun updateBulkRelease(collection: Collection<RoomRelease>)

    @Delete
    fun deleteBulkRelease(collection: Collection<RoomRelease>)

    @Query("SELECT episodeId FROM RoomEpisode;")
    fun loaded(): List<Int>

    @get:Query("SELECT episodeId FROM RoomEpisode WHERE saved=1;")
    val allSavedEpisodes: List<Int>

    @get:Query("SELECT episodeId FROM RoomEpisode WHERE progress = 1 AND saved = 1")
    val allToDeleteLocalEpisodes: List<Int>

    @Query("UPDATE RoomEpisode SET saved=:saved WHERE episodeId=:episodeId")
    fun updateSaved(episodeId: Int, saved: Boolean)

    @Query("UPDATE RoomEpisode SET saved=:saved WHERE episodeId IN (:episodeIds)")
    fun updateSaved(episodeIds: Collection<Int>, saved: Boolean)

    @Query("SELECT episodeId FROM (SELECT RoomEpisode.episodeId, RoomEpisode.saved FROM RoomEpisode " +
            "LEFT JOIN RoomFailedEpisode ON RoomFailedEpisode.episodeId=RoomEpisode.episodeId " +
            "INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId " +
            "INNER JOIN RoomMedium ON RoomPart.mediumId=RoomMedium.mediumId " +
            "WHERE " +
            "progress < 1 " +
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
            "RoomEpisode.combiIndex LIMIT CASE WHEN :limit < 0 THEN 0 ELSE :limit END) " +
            "as RoomEpisode " +
            "WHERE saved = 0")
    fun getDownloadableEpisodes(mediumId: Int, limit: Int): List<Int>

    @Query("SELECT RoomEpisode.episodeId FROM RoomEpisode " +
            "INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId " +
            "WHERE progress < 1 AND saved = 0 AND RoomPart.mediumId IN (:mediaIds) " +
            "ORDER BY mediumId, RoomEpisode.combiIndex")
    fun getDownloadableEpisodes(mediaIds: Collection<Int>): List<Int>

    @Query("SELECT COUNT(RoomEpisode.episodeId) FROM RoomEpisode " +
            "INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId " +
            "WHERE saved = 1 AND RoomPart.mediumId =:mediumId")
    fun countSavedEpisodes(mediumId: Int): Int

    @Query("SELECT episodeId FROM RoomEpisode " +
            "INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId " +
            "WHERE saved = 1 AND RoomPart.mediumId =:mediumId " +
            "ORDER BY RoomEpisode.combiIndex")
    fun getSavedEpisodes(mediumId: Int): List<Int>

    @Query("""SELECT RoomEpisode.episodeId, saved, RoomEpisode.partialIndex, RoomEpisode.totalIndex,
RoomMedium.mediumId,RoomMedium.title as mediumTitle, 
CASE RoomEpisode.progress WHEN 1 THEN 1 ELSE 0 END as read, RoomRelease.*
FROM RoomEpisode 
INNER JOIN RoomRelease ON RoomRelease.episodeId=RoomEpisode.episodeId 
INNER JOIN RoomPart ON RoomEpisode.partId=RoomPart.partId 
INNER JOIN RoomMedium ON RoomPart.mediumId=RoomMedium.mediumId 
WHERE CASE :read 
WHEN 0 THEN progress < 1
WHEN 1 THEN progress = 1
ELSE 1 END 
AND (:listEmpty OR RoomMedium.mediumId IN (SELECT mediumId FROM MediaListMediaJoin WHERE listId IN (:listIds)))
AND (:medium = 0 OR (:medium & medium) > 0)
AND (:saved < 0 OR saved=:saved)
AND (:minIndex < 0 OR RoomEpisode.combiIndex >= :minIndex)
AND (:maxIndex < 0 OR RoomEpisode.combiIndex <= :maxIndex)
ORDER BY RoomRelease.releaseDate DESC, RoomEpisode.combiIndex DESC""")
    fun getDisplayEpisodes(
        saved: Int,
        read: Int,
        medium: Int,
        minIndex: Int,
        maxIndex: Int,
        listIds: Collection<Int>,
        listEmpty: Boolean,
    ): DataSource.Factory<Int, DisplayRelease>

    @Query("""SELECT RoomEpisode.episodeId, saved, RoomEpisode.partialIndex, RoomEpisode.totalIndex,
RoomMedium.mediumId,RoomMedium.title as mediumTitle, 
CASE RoomEpisode.progress WHEN 1 THEN 1 ELSE 0 END as read, RoomRelease.*
FROM RoomEpisode 
INNER JOIN RoomRelease ON RoomRelease.episodeId=RoomEpisode.episodeId 
INNER JOIN RoomPart ON RoomEpisode.partId=RoomPart.partId 
INNER JOIN RoomMedium ON RoomPart.mediumId=RoomMedium.mediumId 
WHERE CASE :read 
WHEN 0 THEN progress < 1
WHEN 1 THEN progress = 1
ELSE 1 END 
AND (:listEmpty OR RoomMedium.mediumId IN (SELECT mediumId FROM MediaListMediaJoin WHERE listId IN (:listIds)))
AND (:medium = 0 OR (:medium & medium) > 0)
AND (:saved < 0 OR saved=:saved)
AND (:minIndex < 0 OR RoomEpisode.combiIndex >= :minIndex)
AND (:maxIndex < 0 OR RoomEpisode.combiIndex <= :maxIndex)
GROUP BY RoomEpisode.episodeId 
ORDER BY RoomRelease.releaseDate DESC, RoomEpisode.combiIndex DESC""")
    fun getDisplayEpisodesLatestOnly(
        saved: Int,
        read: Int,
        medium: Int,
        minIndex: Int,
        maxIndex: Int,
        listIds: Collection<Int>,
        listEmpty: Boolean,
    ): DataSource.Factory<Int, DisplayRelease>

    @Transaction
    @Query("""SELECT * FROM 
(
    SELECT RoomEpisode.episodeId, saved, RoomEpisode.partialIndex, RoomEpisode.totalIndex,
    RoomMedium.mediumId,RoomMedium.title as mediumTitle, 
    CASE RoomEpisode.progress WHEN 1 THEN 1 ELSE 0 END as read 
    FROM RoomEpisode 
    INNER JOIN RoomPart ON RoomEpisode.partId=RoomPart.partId 
    INNER JOIN RoomMedium ON RoomPart.mediumId=RoomMedium.mediumId 
    WHERE progress=0
    AND (:medium = 0 OR (:medium & medium) > 0)
    ORDER BY RoomEpisode.combiIndex DESC
) as UnreadEpisode
WHERE (:saved < 0 OR saved=:saved)
GROUP BY UnreadEpisode.mediumId
ORDER BY (
    SELECT MIN(releaseDate) FROM RoomRelease WHERE RoomRelease.episodeId=UnreadEpisode.episodeId
) DESC""")
    fun getDisplayEpisodesGrouped(saved: Int, medium: Int):DataSource.Factory<Int, RoomDisplayEpisode>

    @Query("SELECT * FROM RoomEpisode WHERE episodeId=:episodeId")
    fun getEpisode(episodeId: Int): RoomEpisode

    @Query("SELECT episodeId, partialIndex, totalIndex, progress FROM RoomEpisode " +
            "WHERE episodeId IN (:ids) " +
            "ORDER BY combiIndex")
    fun getSimpleEpisodes(ids: Collection<Int>): List<SimpleEpisode>

    @Query("SELECT episodeId, partialIndex, totalIndex, progress FROM RoomEpisode WHERE episodeId =:episodeId")
    fun getSimpleEpisode(episodeId: Int): SimpleEpisode

    @get:Query("SELECT RoomEpisode.episodeId, RoomEpisode.partialIndex, RoomEpisode.totalIndex," +
            "RoomMedium.mediumId,RoomMedium.title as mediumTitle " +
            "FROM RoomEpisode " +
            "INNER JOIN RoomPart ON RoomEpisode.partId=RoomPart.partId " +
            "INNER JOIN RoomMedium ON RoomPart.mediumId=RoomMedium.mediumId " +
            "WHERE date(readDate) = date('now') " +
            "ORDER BY RoomEpisode.readDate DESC")
    @get:Transaction
    val readTodayEpisodes:DataSource.Factory<Int, RoomReadEpisode>

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
    fun countDownloadableRows(): LiveData<Int>

    @Transaction
    @Query("SELECT RoomEpisode.* FROM RoomEpisode " +
            "INNER JOIN RoomPart ON RoomEpisode.partId=RoomPart.partId " +
            "INNER JOIN RoomMedium ON RoomPart.mediumId=RoomMedium.mediumId " +
            "WHERE RoomMedium.mediumId=:mediumId " +
            "AND (:read < 0 OR (:read == 0 AND progress < 1) OR :read = progress)" +
            "AND (:saved < 0 OR :saved=saved)" +
            "ORDER BY RoomEpisode.combiIndex ASC")
    fun getTocEpisodesAsc(mediumId: Int, read: Byte, saved: Byte):DataSource.Factory<Int, RoomTocEpisode>

    @Transaction
    @Query("SELECT RoomEpisode.* FROM RoomEpisode " +
            "INNER JOIN RoomPart ON RoomEpisode.partId=RoomPart.partId " +
            "INNER JOIN RoomMedium ON RoomPart.mediumId=RoomMedium.mediumId " +
            "WHERE RoomMedium.mediumId=:mediumId " +
            "AND (:read < 0 OR (:read == 0 AND progress < 1) OR :read = progress)" +
            "AND (:saved < 0 OR :saved=saved)" +
            "ORDER BY RoomEpisode.combiIndex DESC")
    fun getTocEpisodesDesc(mediumId: Int, read: Byte, saved: Byte):DataSource.Factory<Int, RoomTocEpisode>

    @Query("SELECT url FROM RoomRelease WHERE episodeId=:episodeId")
    fun getReleaseLinks(episodeId: Int): List<String>

    @Query("SELECT episodeId FROM RoomEpisode " +
            "INNER JOIN RoomPart ON RoomEpisode.partId=RoomPart.partId " +
            "WHERE mediumId = :mediumId " +
            "AND CASE WHEN :toRead = 1 " +
            "THEN progress < 1 " +
            "ELSE progress = 1 " +
            "END " +
            "AND RoomEpisode.combiIndex <= :episodeCombiIndex")
    fun getEpisodeIdsWithLowerIndex(mediumId: Int, episodeCombiIndex: Double, toRead: Boolean): List<Int>

    @Query("SELECT episodeId FROM RoomEpisode " +
            "INNER JOIN RoomPart ON RoomEpisode.partId=RoomPart.partId " +
            "WHERE mediumId = :mediumId " +
            "AND CASE WHEN :toRead = 1 " +
            "THEN progress < 1 " +
            "ELSE progress = 1 " +
            "END " +
            "AND RoomEpisode.combiIndex >= :episodeCombiIndex")
    fun getEpisodeIdsWithHigherIndex(mediumId: Int, episodeCombiIndex: Double, toRead: Boolean): List<Int>

    @Query("SELECT episodeId FROM RoomEpisode " +
            "INNER JOIN RoomPart ON RoomEpisode.partId=RoomPart.partId " +
            "WHERE mediumId = :mediumId " +
            "AND RoomEpisode.combiIndex <= :episodeCombiIndex")
    fun getEpisodeIdsWithLowerIndex(mediumId: Int, episodeCombiIndex: Double): List<Int>

    @Query("SELECT episodeId FROM RoomEpisode " +
            "INNER JOIN RoomPart ON RoomEpisode.partId=RoomPart.partId " +
            "WHERE mediumId = :mediumId " +
            "AND RoomEpisode.combiIndex >= :episodeCombiIndex")
    fun getEpisodeIdsWithHigherIndex(mediumId: Int, episodeCombiIndex: Double): List<Int>

    @Query("SELECT episodeId FROM RoomEpisode " +
            "INNER JOIN RoomPart ON RoomEpisode.partId=RoomPart.partId " +
            "WHERE mediumId = :mediumId " +
            "AND saved = 1 " +
            "AND RoomEpisode.combiIndex <= :episodeCombiIndex")
    fun getSavedEpisodeIdsWithLowerIndex(mediumId: Int, episodeCombiIndex: Double): List<Int>

    @Query("SELECT episodeId FROM RoomEpisode " +
            "INNER JOIN RoomPart ON RoomEpisode.partId=RoomPart.partId " +
            "WHERE mediumId = :mediumId " +
            "AND saved = 1 " +
            "AND RoomEpisode.combiIndex >= :episodeCombiIndex")
    fun getSavedEpisodeIdsWithHigherIndex(mediumId: Int, episodeCombiIndex: Double): List<Int>

    @Query("DELETE FROM RoomRelease")
    fun clearAllReleases()

    @Query("DELETE FROM RoomEpisode")
    fun clearAll()

    @Query("SELECT episodeId FROM RoomPart " +
            "INNER JOIN RoomEpisode ON RoomPart.partId=RoomEpisode.partId " +
            "WHERE RoomPart.mediumId=:mediumId")
    fun getAllEpisodes(mediumId: Int): List<Int>

    @Query("SELECT episodeId FROM RoomEpisode WHERE partId=:partId")
    fun getEpisodeIds(partId: Int): MutableList<Int>

    @Query("DELETE FROM RoomEpisode WHERE episodeId IN (:ids)")
    fun deletePerId(ids: List<Int>)

    @Query("SELECT episodeId FROM RoomEpisode WHERE " +
            "CASE WHEN :read = 0 THEN progress < 1 ELSE progress = 1 END " +
            "AND episodeId IN (:episodeIds)")
    fun getReadEpisodes(episodeIds: Collection<Int>, read: Boolean): List<Int>

    @get:Query("SELECT partId, count(DISTINCT RoomEpisode.episodeId) as episodeCount, " +
            "sum(DISTINCT RoomEpisode.episodeId) as episodeSum, count(url) as releaseCount " +
            "FROM RoomEpisode LEFT JOIN RoomRelease ON RoomEpisode.episodeId=RoomRelease.episodeId " +
            "GROUP BY partId")
    val stat: List<RoomPartStat>

    @Query("SELECT partId, episodeId FROM RoomEpisode WHERE partId IN (:partIds)")
    fun getEpisodes(partIds: Collection<Int>): List<RoomPartEpisode>

    @Query("SELECT partId, RoomEpisode.episodeId, RoomRelease.url " +
            "FROM RoomEpisode INNER JOIN RoomRelease ON RoomRelease.episodeId=RoomEpisode.episodeId " +
            "WHERE partId IN (:partIds)")
    fun getReleases(partIds: Collection<Int>): List<RoomSimpleRelease>
}