package com.mytlogos.enterprise.background.room;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Query;

import com.mytlogos.enterprise.background.room.model.RoomMedium;
import com.mytlogos.enterprise.model.MediumItem;
import com.mytlogos.enterprise.model.MediumSetting;
import com.mytlogos.enterprise.model.SimpleMedium;
import com.mytlogos.enterprise.model.SpaceMedium;

import org.joda.time.DateTime;

import java.util.List;

@Dao
public interface MediumDao extends MultiBaseDao<RoomMedium> {

    @Query("SELECT mediumId FROM RoomMedium;")
    List<Integer> loaded();

    @Query("SELECT * FROM " +
            "(SELECT title, RoomMedium.mediumId, author, artist, medium, stateTL, stateOrigin, " +
            "countryOfOrigin, languageOfOrigin, lang, series, universe, " +
            "(" +
            "   SELECT episodeId FROM RoomEpisode " +
            "   INNER JOIN RoomPart ON RoomPart.partId= RoomEpisode.partId " +
            "   WHERE mediumId=RoomMedium.mediumId " +
            "   ORDER BY RoomEpisode.combiIndex DESC " +
            "   LIMIT 1" +
            ") as currentRead," +
            "(" +
            "    SELECT RoomEpisode.combiIndex \n" +
            "    FROM RoomEpisode\n" +
            "    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId\n" +
            "    WHERE RoomPart.mediumId=RoomMedium.mediumId AND RoomEpisode.progress=1\n" +
            "    ORDER BY RoomEpisode.combiIndex DESC\n" +
            "    LIMIT 1" +
            ") as currentReadEpisode," +
            "(" +
            "   SELECT MAX(RoomEpisode.combiIndex) FROM RoomEpisode " +
            "   INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId  " +
            "   WHERE RoomPart.mediumId=RoomMedium.mediumId" +
            ") as lastEpisode , " +
            "(" +
            "   SELECT MAX(RoomRelease.releaseDate) FROM RoomEpisode " +
            "   INNER JOIN RoomRelease ON RoomEpisode.episodeId=RoomRelease.episodeId " +
            "   INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId  " +
            "   WHERE RoomPart.mediumId=RoomMedium.mediumId" +
            ") as lastUpdated " +
            "FROM RoomMedium" +
            ") as RoomMedium " +
            "WHERE " +
            "(:title IS NULL OR INSTR(lower(title), :title) > 0) " +
            "AND (:medium = 0 OR (:medium & medium) > 0) " +
            "AND (:author IS NULL OR INSTR(lower(author), :author) > 0) " +
            "AND (:lastUpdate IS NULL OR datetime(lastUpdated) >= datetime(:lastUpdate)) " +
            "AND (:minCountEpisodes < 0 OR :minCountEpisodes >= lastEpisode) " +
            "AND (:minCountReadEpisodes < 0 OR :minCountReadEpisodes >= currentReadEpisode) " +
            "ORDER BY " +
            "CASE :sortValue " +
            "WHEN 2 THEN medium " +
            "WHEN 3 THEN title " +
            "WHEN 5 THEN author " +
            "WHEN 7 THEN lastEpisode " +
            "WHEN 8 THEN currentReadEpisode " +
            "WHEN 9 THEN lastUpdated " +
            "ELSE title " +
            "END ASC")
    DataSource.Factory<Integer, MediumItem> getAllAsc(int sortValue, String title, int medium, String author, DateTime lastUpdate, int minCountEpisodes, int minCountReadEpisodes);

    @Query("SELECT * FROM " +
            "(SELECT title, RoomMedium.mediumId, author, artist, medium, stateTL, stateOrigin, " +
            "countryOfOrigin, languageOfOrigin, lang, series, universe, " +
            "(" +
            "   SELECT episodeId FROM RoomEpisode " +
            "   INNER JOIN RoomPart ON RoomPart.partId= RoomEpisode.partId " +
            "   WHERE mediumId=RoomMedium.mediumId " +
            "   ORDER BY RoomEpisode.combiIndex DESC " +
            "   LIMIT 1" +
            ") as currentRead," +
            "(" +
            "    SELECT RoomEpisode.combiIndex \n" +
            "    FROM RoomEpisode\n" +
            "    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId\n" +
            "    WHERE RoomPart.mediumId=RoomMedium.mediumId AND RoomEpisode.progress=1" +
            "    ORDER BY RoomEpisode.combiIndex DESC" +
            "    LIMIT 1" +
            ") as currentReadEpisode," +
            "(" +
            "   SELECT MAX((COALESCE(RoomEpisode.totalIndex,0) + COALESCE(RoomEpisode.partialIndex,0))) FROM RoomEpisode " +
            "   INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId  " +
            "   WHERE RoomPart.mediumId=RoomMedium.mediumId" +
            ") as lastEpisode , " +
            "(" +
            "   SELECT MAX(RoomRelease.releaseDate) FROM RoomEpisode " +
            "   INNER JOIN RoomRelease ON RoomEpisode.episodeId=RoomRelease.episodeId " +
            "   INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId  " +
            "   WHERE RoomPart.mediumId=RoomMedium.mediumId" +
            ") as lastUpdated " +
            "FROM RoomMedium" +
            ") as RoomMedium " +
            "WHERE " +
            "(:title IS NULL OR INSTR(lower(title), :title) > 0) " +
            "AND (:medium = 0 OR (:medium & medium) > 0) " +
            "AND (:author IS NULL OR INSTR(lower(author), :author) > 0) " +
            "AND (:lastUpdate IS NULL OR datetime(lastUpdated) >= datetime(:lastUpdate)) " +
            "AND (:minCountEpisodes < 0 OR :minCountEpisodes >= lastEpisode) " +
            "AND (:minCountReadEpisodes < 0 OR :minCountReadEpisodes >= currentReadEpisode) " +
            "ORDER BY " +
            "CASE :sortValue " +
            "WHEN 2 THEN medium " +
            "WHEN 3 THEN title " +
            "WHEN 5 THEN author " +
            "WHEN 7 THEN lastEpisode " +
            "WHEN 8 THEN currentReadEpisode " +
            "WHEN 9 THEN lastUpdated " +
            "ELSE title " +
            "END DESC")
    DataSource.Factory<Integer, MediumItem> getAllDesc(int sortValue, String title, int medium, String author, DateTime lastUpdate, int minCountEpisodes, int minCountReadEpisodes);

    @Query("SELECT title, RoomMedium.mediumId, author, artist, medium, stateTL, stateOrigin, " +
            "countryOfOrigin, languageOfOrigin, lang, series, universe, " +
            "(" +
            "   SELECT episodeId FROM RoomEpisode " +
            "   INNER JOIN RoomPart ON RoomPart.partId= RoomEpisode.partId " +
            "   WHERE mediumId=RoomMedium.mediumId " +
            "   ORDER BY RoomEpisode.combiIndex DESC " +
            "   LIMIT 1" +
            ") as currentRead," +
            "(" +
            "    SELECT RoomEpisode.combiIndex \n" +
            "    FROM RoomEpisode\n" +
            "    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId\n" +
            "    WHERE RoomPart.mediumId=RoomMedium.mediumId AND RoomEpisode.progress=1" +
            "    ORDER BY RoomEpisode.combiIndex DESC" +
            "    LIMIT 1" +
            ") as currentReadEpisode," +
            "(" +
            "   SELECT MAX(RoomEpisode.combiIndex) FROM RoomEpisode " +
            "   INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId  " +
            "   WHERE RoomPart.mediumId=RoomMedium.mediumId" +
            ") as lastEpisode , " +
            "(" +
            "   SELECT MAX(RoomRelease.releaseDate) FROM RoomEpisode " +
            "   INNER JOIN RoomRelease ON RoomEpisode.episodeId=RoomRelease.episodeId " +
            "   INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId  " +
            "   WHERE RoomPart.mediumId=RoomMedium.mediumId" +
            ") as lastUpdated " +
            "FROM RoomMedium INNER JOIN MediaListMediaJoin " +
            "ON MediaListMediaJoin.mediumId=RoomMedium.mediumId " +
            "WHERE listId=:listId " +
            "ORDER BY title")
    LiveData<List<MediumItem>> getListMedia(int listId);

    @Query("SELECT title, RoomMedium.mediumId, author, artist, medium, stateTL, stateOrigin, \n" +
            "countryOfOrigin, languageOfOrigin, lang, series, universe," +
            "(" +
            "   SELECT episodeId FROM RoomEpisode " +
            "   INNER JOIN RoomPart ON RoomPart.partId= RoomEpisode.partId " +
            "   WHERE mediumId=RoomMedium.mediumId " +
            "   ORDER BY RoomEpisode.combiIndex DESC " +
            "   LIMIT 1" +
            ") as currentRead, \n" +
            "(\n" +
            "    SELECT RoomEpisode.combiIndex \n" +
            "    FROM RoomEpisode\n" +
            "    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId\n" +
            "    WHERE RoomPart.mediumId=RoomMedium.mediumId AND RoomEpisode.progress=1" +
            "    ORDER BY RoomEpisode.combiIndex DESC" +
            "    LIMIT 1" +
            ") as currentReadEpisode,\n" +
            "(\n" +
            "    SELECT MAX(RoomEpisode.combiIndex) \n" +
            "    FROM RoomEpisode\n" +
            "    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId\n" +
            "    WHERE RoomPart.mediumId=RoomMedium.mediumId\n" +
            ") as lastEpisode,\n" +
            "(\n" +
            "    SELECT MAX(RoomRelease.releaseDate) \n" +
            "    FROM RoomEpisode\n" +
            "    INNER JOIN RoomRelease ON RoomEpisode.episodeId=RoomRelease.episodeId\n" +
            "    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId \n" +
            "    WHERE RoomPart.mediumId=RoomMedium.mediumId\n" +
            ") as lastUpdated \n" +
            "FROM RoomMedium \n" +
            "INNER JOIN ExternalListMediaJoin ON ExternalListMediaJoin.mediumId=RoomMedium.mediumId \n" +
            "WHERE listId=:listId ORDER BY title")
    LiveData<List<MediumItem>> getExternalListMedia(int listId);

    @Query("SELECT title, RoomMedium.mediumId, author, artist, medium, stateTL, stateOrigin, " +
            "countryOfOrigin, languageOfOrigin, lang, series, universe, toDownload, " +
            "(" +
            "    SELECT RoomEpisode.combiIndex \n" +
            "    FROM RoomEpisode\n" +
            "    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId\n" +
            "    WHERE RoomPart.mediumId=RoomMedium.mediumId AND RoomEpisode.progress=1" +
            "    ORDER BY RoomEpisode.combiIndex DESC" +
            "    LIMIT 1" +
            ") as currentReadEpisode," +
            "(" +
            "   SELECT episodeId FROM RoomEpisode " +
            "   INNER JOIN RoomPart ON RoomPart.partId= RoomEpisode.partId " +
            "   WHERE mediumId=RoomMedium.mediumId " +
            "   ORDER BY RoomEpisode.combiIndex DESC " +
            "   LIMIT 1" +
            ") as currentRead, " +
            "(" +
            "   SELECT MAX(RoomEpisode.combiIndex) FROM RoomEpisode " +
            "   INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId  " +
            "   WHERE RoomPart.mediumId=RoomMedium.mediumId" +
            ") as lastEpisode, " +
            "(" +
            "   SELECT MAX(RoomRelease.releaseDate) FROM RoomEpisode " +
            "   INNER JOIN RoomRelease ON RoomEpisode.episodeId=RoomRelease.episodeId " +
            "   INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId  " +
            "   WHERE RoomPart.mediumId=RoomMedium.mediumId" +
            ") as lastUpdated " +
            "FROM RoomMedium " +
            "LEFT JOIN " +
            "(SELECT mediumId,1 as toDownload FROM RoomToDownload WHERE mediumId > 0) " +
            "as RoomToDownload ON RoomToDownload.mediumId=RoomMedium.mediumId " +
            "WHERE RoomMedium.mediumId=:mediumId")
    LiveData<MediumSetting> getMediumSettings(int mediumId);

    @Query("SELECT title, medium, mediumId FROM RoomMedium " +
            "WHERE medium=:medium AND INSTR(lower(title), :title) ORDER BY title LIMIT 10")
    LiveData<List<SimpleMedium>> getSuggestions(String title, int medium);

    @Query("SELECT title, medium, mediumId FROM RoomMedium WHERE mediumId=:mediumId")
    SimpleMedium getSimpleMedium(int mediumId);

    @Query("SELECT mediumId, title," +
            "(" +
            "   SELECT COUNT(episodeId) FROM RoomEpisode " +
            "   INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId  " +
            "   WHERE RoomPart.mediumId=:mediumId AND saved=1" +
            ") as savedEpisodes " +
            "FROM RoomMedium " +
            "WHERE mediumId=:mediumId")
    SpaceMedium getSpaceMedium(int mediumId);

    @Query("SELECT medium FROM RoomMedium WHERE mediumId=:mediumId")
    int getMediumType(Integer mediumId);

    @Query("SELECT mediumId FROM RoomPart " +
            "INNER JOIN RoomEpisode ON RoomPart.partId=RoomEpisode.partId " +
            "WHERE RoomEpisode.episodeId=:episodeId " +
            "LIMIT 1")
    Integer getMediumId(int episodeId);

}
