package com.mytlogos.enterprise.background.room

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.PagingSource
import androidx.room.*
import com.mytlogos.enterprise.background.room.model.RoomMedium
import com.mytlogos.enterprise.model.*
import org.joda.time.DateTime

@Dao
interface MediumDao : MultiBaseDao<RoomMedium> {
    @Query("SELECT mediumId FROM RoomMedium;")
    suspend fun loaded(): List<Int>

    @Query("""
        SELECT
        title, RoomMedium.mediumId, author, artist, medium, stateTL, stateOrigin, 
        countryOfOrigin, languageOfOrigin, lang, series, universe,
        lastUpdated, COALESCE(lastEpisodeIndex, 0) as lastEpisode,
        COALESCE(currentReadEpisodeIndex, 0) as currentReadEpisode, COALESCE(currentReadId, 0) as currentRead
        FROM RoomMedium 
        LEFT JOIN (
            SELECT MAX(RoomEpisode.combiIndex) as currentReadEpisodeIndex,
            RoomEpisode.episodeId as currentReadId,
            RoomPart.mediumId
            FROM RoomEpisode
            INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId
            WHERE RoomEpisode.progress=1
            GROUP BY RoomPart.mediumId
        ) as cre 
        ON cre.mediumId=RoomMedium.mediumId
        LEFT JOIN (
            SELECT MAX(RoomEpisode.combiIndex) as lastEpisodeIndex, RoomPart.mediumId FROM RoomEpisode
            INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId
            GROUP BY RoomPart.mediumId
        ) as le
        ON le.mediumId=RoomMedium.mediumId
        LEFT JOIN (
            SELECT MAX(RoomRelease.releaseDate) as lastUpdated, mediumId FROM RoomEpisode
            INNER JOIN RoomRelease ON RoomEpisode.episodeId=RoomRelease.episodeId
            INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId
            GROUP BY RoomPart.mediumId
        ) as lastUpdated
        ON lastUpdated.mediumId=RoomMedium.mediumId
        WHERE (:title IS NULL OR INSTR(lower(title), :title) > 0) 
        AND (:medium = 0 OR (:medium & medium) > 0) 
        AND (:author IS NULL OR INSTR(lower(author), :author) > 0) 
        AND (:lastUpdate IS NULL OR datetime(lastUpdated) >= datetime(:lastUpdate)) 
        AND (:minCountEpisodes < 0 OR :minCountEpisodes >= lastEpisode) 
        AND (:minCountReadEpisodes < 0 OR :minCountReadEpisodes >= currentReadEpisode) 
        ORDER BY CASE :sortValue 
            WHEN 2 THEN medium
            WHEN 3 THEN title
            WHEN 5 THEN author
            WHEN 7 THEN lastEpisode
            WHEN 8 THEN currentReadEpisode
            WHEN 9 THEN lastUpdated 
            ELSE title END ASC
    """)
    fun getAllAsc(
        sortValue: Int,
        title: String?,
        medium: Int,
        author: String?,
        lastUpdate: DateTime?,
        minCountEpisodes: Int,
        minCountReadEpisodes: Int,
    ): PagingSource<Int, MediumItem>

    @Query("""
        SELECT
        title, RoomMedium.mediumId, author, artist, medium, stateTL, stateOrigin, 
        countryOfOrigin, languageOfOrigin, lang, series, universe,
        lastUpdated, COALESCE(lastEpisodeIndex, 0) as lastEpisode,
        COALESCE(currentReadEpisodeIndex, 0) as currentReadEpisode, COALESCE(currentReadId, 0) as currentRead
        FROM RoomMedium 
        LEFT JOIN (
            SELECT MAX(RoomEpisode.combiIndex) as currentReadEpisodeIndex,
            RoomEpisode.episodeId as currentReadId,
            RoomPart.mediumId
            FROM RoomEpisode
            INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId
            WHERE RoomEpisode.progress=1
            GROUP BY RoomPart.mediumId
        ) as cre 
        ON cre.mediumId=RoomMedium.mediumId
        LEFT JOIN (
            SELECT MAX(RoomEpisode.combiIndex) as lastEpisodeIndex, RoomPart.mediumId FROM RoomEpisode
            INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId
            GROUP BY RoomPart.mediumId
        ) as le
        ON le.mediumId=RoomMedium.mediumId
        LEFT JOIN (
            SELECT MAX(RoomRelease.releaseDate) as lastUpdated, mediumId FROM RoomEpisode
            INNER JOIN RoomRelease ON RoomEpisode.episodeId=RoomRelease.episodeId
            INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId
            GROUP BY RoomPart.mediumId
        ) as lastUpdated
        ON lastUpdated.mediumId=RoomMedium.mediumId
        WHERE (:title IS NULL OR INSTR(lower(title), :title) > 0) 
        AND (:medium = 0 OR (:medium & medium) > 0) 
        AND (:author IS NULL OR INSTR(lower(author), :author) > 0) 
        AND (:lastUpdate IS NULL OR datetime(lastUpdated) >= datetime(:lastUpdate)) 
        AND (:minCountEpisodes < 0 OR :minCountEpisodes >= lastEpisode) 
        AND (:minCountReadEpisodes < 0 OR :minCountReadEpisodes >= currentReadEpisode) 
        ORDER BY CASE :sortValue 
            WHEN 2 THEN medium
            WHEN 3 THEN title
            WHEN 5 THEN author
            WHEN 7 THEN lastEpisode
            WHEN 8 THEN currentReadEpisode
            WHEN 9 THEN lastUpdated 
            ELSE title END DESC
    """)
    fun getAllDesc(
        sortValue: Int,
        title: String?,
        medium: Int,
        author: String?,
        lastUpdate: DateTime?,
        minCountEpisodes: Int,
        minCountReadEpisodes: Int,
    ): PagingSource<Int, MediumItem>

    @Query("""SELECT title, RoomMedium.mediumId, author, artist, medium, stateTL, stateOrigin, countryOfOrigin, languageOfOrigin, lang, series, universe, (   SELECT episodeId FROM RoomEpisode    INNER JOIN RoomPart ON RoomPart.partId= RoomEpisode.partId    WHERE mediumId=RoomMedium.mediumId    ORDER BY RoomEpisode.combiIndex DESC    LIMIT 1) as currentRead,(    SELECT RoomEpisode.combiIndex 
    FROM RoomEpisode
    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId
    WHERE RoomPart.mediumId=RoomMedium.mediumId AND RoomEpisode.progress=1    ORDER BY RoomEpisode.combiIndex DESC    LIMIT 1) as currentReadEpisode,(   SELECT MAX(RoomEpisode.combiIndex) FROM RoomEpisode    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId     WHERE RoomPart.mediumId=RoomMedium.mediumId) as lastEpisode , (   SELECT MAX(RoomRelease.releaseDate) FROM RoomEpisode    INNER JOIN RoomRelease ON RoomEpisode.episodeId=RoomRelease.episodeId    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId     WHERE RoomPart.mediumId=RoomMedium.mediumId) as lastUpdated FROM RoomMedium INNER JOIN MediaListMediaJoin ON MediaListMediaJoin.mediumId=RoomMedium.mediumId WHERE listId=:listId ORDER BY title""")
    fun getListMedia(listId: Int): LiveData<MutableList<MediumItem>>

    @Query("""SELECT title, RoomMedium.mediumId, author, artist, medium, stateTL, stateOrigin, 
countryOfOrigin, languageOfOrigin, lang, series, universe,(   SELECT episodeId FROM RoomEpisode    INNER JOIN RoomPart ON RoomPart.partId= RoomEpisode.partId    WHERE mediumId=RoomMedium.mediumId    ORDER BY RoomEpisode.combiIndex DESC    LIMIT 1) as currentRead, 
(
    SELECT RoomEpisode.combiIndex 
    FROM RoomEpisode
    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId
    WHERE RoomPart.mediumId=RoomMedium.mediumId AND RoomEpisode.progress=1    ORDER BY RoomEpisode.combiIndex DESC    LIMIT 1) as currentReadEpisode,
(
    SELECT MAX(RoomEpisode.combiIndex) 
    FROM RoomEpisode
    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId
    WHERE RoomPart.mediumId=RoomMedium.mediumId
) as lastEpisode,
(
    SELECT MAX(RoomRelease.releaseDate) 
    FROM RoomEpisode
    INNER JOIN RoomRelease ON RoomEpisode.episodeId=RoomRelease.episodeId
    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId 
    WHERE RoomPart.mediumId=RoomMedium.mediumId
) as lastUpdated 
FROM RoomMedium 
INNER JOIN ExternalListMediaJoin ON ExternalListMediaJoin.mediumId=RoomMedium.mediumId 
WHERE listId=:listId ORDER BY title""")
    fun getExternalListMedia(listId: Int): LiveData<MutableList<MediumItem>>

    @Query("""SELECT title, RoomMedium.mediumId, author, artist, medium, stateTL, stateOrigin, countryOfOrigin, languageOfOrigin, lang, series, universe, toDownload, (    SELECT RoomEpisode.combiIndex 
    FROM RoomEpisode
    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId
    WHERE RoomPart.mediumId=RoomMedium.mediumId AND RoomEpisode.progress=1    ORDER BY RoomEpisode.combiIndex DESC    LIMIT 1) as currentReadEpisode,(   SELECT episodeId FROM RoomEpisode    INNER JOIN RoomPart ON RoomPart.partId= RoomEpisode.partId    WHERE mediumId=RoomMedium.mediumId    ORDER BY RoomEpisode.combiIndex DESC    LIMIT 1) as currentRead, (   SELECT MAX(RoomEpisode.combiIndex) FROM RoomEpisode    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId     WHERE RoomPart.mediumId=RoomMedium.mediumId) as lastEpisode, (   SELECT MAX(RoomRelease.releaseDate) FROM RoomEpisode    INNER JOIN RoomRelease ON RoomEpisode.episodeId=RoomRelease.episodeId    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId     WHERE RoomPart.mediumId=RoomMedium.mediumId) as lastUpdated FROM RoomMedium LEFT JOIN (SELECT mediumId,1 as toDownload FROM RoomToDownload WHERE mediumId > 0) as RoomToDownload ON RoomToDownload.mediumId=RoomMedium.mediumId WHERE RoomMedium.mediumId=:mediumId""")
    fun getMediumSettings(mediumId: Int): LiveData<MediumSetting>

    @Query("""SELECT title, RoomMedium.mediumId, author, artist, medium, stateTL, stateOrigin, countryOfOrigin, languageOfOrigin, lang, series, universe, toDownload, (    SELECT RoomEpisode.combiIndex 
    FROM RoomEpisode
    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId
    WHERE RoomPart.mediumId=RoomMedium.mediumId AND RoomEpisode.progress=1    ORDER BY RoomEpisode.combiIndex DESC    LIMIT 1) as currentReadEpisode,(   SELECT episodeId FROM RoomEpisode    INNER JOIN RoomPart ON RoomPart.partId= RoomEpisode.partId    WHERE mediumId=RoomMedium.mediumId    ORDER BY RoomEpisode.combiIndex DESC    LIMIT 1) as currentRead, (   SELECT MAX(RoomEpisode.combiIndex) FROM RoomEpisode    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId     WHERE RoomPart.mediumId=RoomMedium.mediumId) as lastEpisode, (   SELECT MAX(RoomRelease.releaseDate) FROM RoomEpisode    INNER JOIN RoomRelease ON RoomEpisode.episodeId=RoomRelease.episodeId    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId     WHERE RoomPart.mediumId=RoomMedium.mediumId) as lastUpdated FROM RoomMedium LEFT JOIN (SELECT mediumId,1 as toDownload FROM RoomToDownload WHERE mediumId > 0) as RoomToDownload ON RoomToDownload.mediumId=RoomMedium.mediumId WHERE RoomMedium.mediumId=:mediumId""")
    suspend fun getMediumSettingsNow(mediumId: Int): MediumSetting

    @Query("SELECT title, medium, mediumId FROM RoomMedium " +
            "WHERE medium=:medium AND INSTR(lower(title), :title) ORDER BY title LIMIT 10")
    fun getSuggestions(title: String, medium: Int): LiveData<MutableList<SimpleMedium>>

    @Query("SELECT title, medium, mediumId FROM RoomMedium WHERE mediumId=:mediumId")
    suspend fun getSimpleMedium(mediumId: Int): SimpleMedium

    @Query("SELECT mediumId, title," +
            "(" +
            "   SELECT COUNT(episodeId) FROM RoomEpisode " +
            "   INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId  " +
            "   WHERE RoomPart.mediumId=:mediumId AND saved=1" +
            ") as savedEpisodes " +
            "FROM RoomMedium " +
            "WHERE mediumId=:mediumId")
    suspend fun getSpaceMedium(mediumId: Int): SpaceMedium

    @Query("SELECT medium FROM RoomMedium WHERE mediumId=:mediumId")
    suspend fun getMediumType(mediumId: Int): Int

    @Query("SELECT mediumId FROM RoomPart " +
            "INNER JOIN RoomEpisode ON RoomPart.partId=RoomEpisode.partId " +
            "WHERE RoomEpisode.episodeId=:episodeId " +
            "LIMIT 1")
    suspend fun getMediumId(episodeId: Int): Int
}