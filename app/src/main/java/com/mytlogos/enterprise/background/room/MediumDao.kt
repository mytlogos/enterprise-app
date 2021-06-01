package com.mytlogos.enterprise.background.room

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.mytlogos.enterprise.background.room.model.RoomMedium
import com.mytlogos.enterprise.model.*
import org.joda.time.DateTime

@Dao
interface MediumDao : MultiBaseDao<RoomMedium> {
    @Query("SELECT mediumId FROM RoomMedium;")
    fun loaded(): List<Int>

    @Query("""
        SELECT * FROM (
            SELECT title, RoomMedium.mediumId, author, artist, medium, stateTL, stateOrigin, 
            countryOfOrigin, languageOfOrigin, lang, series, universe, (
                SELECT episodeId FROM RoomEpisode 
                INNER JOIN RoomPart ON RoomPart.partId= RoomEpisode.partId
                WHERE mediumId=RoomMedium.mediumId
                ORDER BY RoomEpisode.combiIndex DESC
                LIMIT 1
            ) as currentRead, (
            SELECT RoomEpisode.combiIndex 
            FROM RoomEpisode
            INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId
            WHERE RoomPart.mediumId=RoomMedium.mediumId AND RoomEpisode.progress=1
            ORDER BY RoomEpisode.combiIndex DESC
            LIMIT 1) as currentReadEpisode, (
            SELECT MAX(RoomEpisode.combiIndex) FROM RoomEpisode
            INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId
            WHERE RoomPart.mediumId=RoomMedium.mediumId
            ) as lastEpisode , (
            SELECT MAX(RoomRelease.releaseDate) FROM RoomEpisode
            INNER JOIN RoomRelease ON RoomEpisode.episodeId=RoomRelease.episodeId
            INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId
            WHERE RoomPart.mediumId=RoomMedium.mediumId
            ) as lastUpdated FROM RoomMedium
        ) as RoomMedium 
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
    fun getAllAsc(sortValue: Int, title: String?, medium: Int, author: String?, lastUpdate: DateTime?, minCountEpisodes: Int, minCountReadEpisodes: Int): DataSource.Factory<Int, MediumItem>

    @Query("""SELECT * FROM (SELECT title, RoomMedium.mediumId, author, artist, medium, stateTL, stateOrigin, countryOfOrigin, languageOfOrigin, lang, series, universe, (   SELECT episodeId FROM RoomEpisode    INNER JOIN RoomPart ON RoomPart.partId= RoomEpisode.partId    WHERE mediumId=RoomMedium.mediumId    ORDER BY RoomEpisode.combiIndex DESC    LIMIT 1) as currentRead,(    SELECT RoomEpisode.combiIndex 
    FROM RoomEpisode
    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId
    WHERE RoomPart.mediumId=RoomMedium.mediumId AND RoomEpisode.progress=1    ORDER BY RoomEpisode.combiIndex DESC    LIMIT 1) as currentReadEpisode,(   SELECT MAX((COALESCE(RoomEpisode.totalIndex,0) + COALESCE(RoomEpisode.partialIndex,0))) FROM RoomEpisode    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId     WHERE RoomPart.mediumId=RoomMedium.mediumId) as lastEpisode , (   SELECT MAX(RoomRelease.releaseDate) FROM RoomEpisode    INNER JOIN RoomRelease ON RoomEpisode.episodeId=RoomRelease.episodeId    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId     WHERE RoomPart.mediumId=RoomMedium.mediumId) as lastUpdated FROM RoomMedium) as RoomMedium WHERE (:title IS NULL OR INSTR(lower(title), :title) > 0) AND (:medium = 0 OR (:medium & medium) > 0) AND (:author IS NULL OR INSTR(lower(author), :author) > 0) AND (:lastUpdate IS NULL OR datetime(lastUpdated) >= datetime(:lastUpdate)) AND (:minCountEpisodes < 0 OR :minCountEpisodes >= lastEpisode) AND (:minCountReadEpisodes < 0 OR :minCountReadEpisodes >= currentReadEpisode) ORDER BY CASE :sortValue WHEN 2 THEN medium WHEN 3 THEN title WHEN 5 THEN author WHEN 7 THEN lastEpisode WHEN 8 THEN currentReadEpisode WHEN 9 THEN lastUpdated ELSE title END DESC""")
    fun getAllDesc(sortValue: Int, title: String?, medium: Int, author: String?, lastUpdate: DateTime?, minCountEpisodes: Int, minCountReadEpisodes: Int): DataSource.Factory<Int, MediumItem>

    @Query("""SELECT title, RoomMedium.mediumId, author, artist, medium, stateTL, stateOrigin, countryOfOrigin, languageOfOrigin, lang, series, universe, (   SELECT episodeId FROM RoomEpisode    INNER JOIN RoomPart ON RoomPart.partId= RoomEpisode.partId    WHERE mediumId=RoomMedium.mediumId    ORDER BY RoomEpisode.combiIndex DESC    LIMIT 1) as currentRead,(    SELECT RoomEpisode.combiIndex 
    FROM RoomEpisode
    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId
    WHERE RoomPart.mediumId=RoomMedium.mediumId AND RoomEpisode.progress=1    ORDER BY RoomEpisode.combiIndex DESC    LIMIT 1) as currentReadEpisode,(   SELECT MAX(RoomEpisode.combiIndex) FROM RoomEpisode    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId     WHERE RoomPart.mediumId=RoomMedium.mediumId) as lastEpisode , (   SELECT MAX(RoomRelease.releaseDate) FROM RoomEpisode    INNER JOIN RoomRelease ON RoomEpisode.episodeId=RoomRelease.episodeId    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId     WHERE RoomPart.mediumId=RoomMedium.mediumId) as lastUpdated FROM RoomMedium INNER JOIN MediaListMediaJoin ON MediaListMediaJoin.mediumId=RoomMedium.mediumId WHERE listId=:listId ORDER BY title""")
    fun getListMedia(listId: Int): LiveData<List<MediumItem>>

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
    fun getExternalListMedia(listId: Int): LiveData<List<MediumItem>>

    @Query("""SELECT title, RoomMedium.mediumId, author, artist, medium, stateTL, stateOrigin, countryOfOrigin, languageOfOrigin, lang, series, universe, toDownload, (    SELECT RoomEpisode.combiIndex 
    FROM RoomEpisode
    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId
    WHERE RoomPart.mediumId=RoomMedium.mediumId AND RoomEpisode.progress=1    ORDER BY RoomEpisode.combiIndex DESC    LIMIT 1) as currentReadEpisode,(   SELECT episodeId FROM RoomEpisode    INNER JOIN RoomPart ON RoomPart.partId= RoomEpisode.partId    WHERE mediumId=RoomMedium.mediumId    ORDER BY RoomEpisode.combiIndex DESC    LIMIT 1) as currentRead, (   SELECT MAX(RoomEpisode.combiIndex) FROM RoomEpisode    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId     WHERE RoomPart.mediumId=RoomMedium.mediumId) as lastEpisode, (   SELECT MAX(RoomRelease.releaseDate) FROM RoomEpisode    INNER JOIN RoomRelease ON RoomEpisode.episodeId=RoomRelease.episodeId    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId     WHERE RoomPart.mediumId=RoomMedium.mediumId) as lastUpdated FROM RoomMedium LEFT JOIN (SELECT mediumId,1 as toDownload FROM RoomToDownload WHERE mediumId > 0) as RoomToDownload ON RoomToDownload.mediumId=RoomMedium.mediumId WHERE RoomMedium.mediumId=:mediumId""")
    fun getMediumSettings(mediumId: Int): LiveData<MediumSetting?>

    @Query("""SELECT title, RoomMedium.mediumId, author, artist, medium, stateTL, stateOrigin, countryOfOrigin, languageOfOrigin, lang, series, universe, toDownload, (    SELECT RoomEpisode.combiIndex 
    FROM RoomEpisode
    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId
    WHERE RoomPart.mediumId=RoomMedium.mediumId AND RoomEpisode.progress=1    ORDER BY RoomEpisode.combiIndex DESC    LIMIT 1) as currentReadEpisode,(   SELECT episodeId FROM RoomEpisode    INNER JOIN RoomPart ON RoomPart.partId= RoomEpisode.partId    WHERE mediumId=RoomMedium.mediumId    ORDER BY RoomEpisode.combiIndex DESC    LIMIT 1) as currentRead, (   SELECT MAX(RoomEpisode.combiIndex) FROM RoomEpisode    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId     WHERE RoomPart.mediumId=RoomMedium.mediumId) as lastEpisode, (   SELECT MAX(RoomRelease.releaseDate) FROM RoomEpisode    INNER JOIN RoomRelease ON RoomEpisode.episodeId=RoomRelease.episodeId    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId     WHERE RoomPart.mediumId=RoomMedium.mediumId) as lastUpdated FROM RoomMedium LEFT JOIN (SELECT mediumId,1 as toDownload FROM RoomToDownload WHERE mediumId > 0) as RoomToDownload ON RoomToDownload.mediumId=RoomMedium.mediumId WHERE RoomMedium.mediumId=:mediumId""")
    fun getMediumSettingsNow(mediumId: Int): MediumSetting?

    @Query("SELECT title, medium, mediumId FROM RoomMedium " +
            "WHERE medium=:medium AND INSTR(lower(title), :title) ORDER BY title LIMIT 10")
    fun getSuggestions(title: String, medium: Int): LiveData<List<SimpleMedium>>

    @Query("SELECT title, medium, mediumId FROM RoomMedium WHERE mediumId=:mediumId")
    fun getSimpleMedium(mediumId: Int): SimpleMedium

    @Query("SELECT mediumId, title," +
            "(" +
            "   SELECT COUNT(episodeId) FROM RoomEpisode " +
            "   INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId  " +
            "   WHERE RoomPart.mediumId=:mediumId AND saved=1" +
            ") as savedEpisodes " +
            "FROM RoomMedium " +
            "WHERE mediumId=:mediumId")
    fun getSpaceMedium(mediumId: Int): SpaceMedium

    @Query("SELECT medium FROM RoomMedium WHERE mediumId=:mediumId")
    fun getMediumType(mediumId: Int): Int

    @Query("SELECT mediumId FROM RoomPart " +
            "INNER JOIN RoomEpisode ON RoomPart.partId=RoomEpisode.partId " +
            "WHERE RoomEpisode.episodeId=:episodeId " +
            "LIMIT 1")
    fun getMediumId(episodeId: Int): Int
}