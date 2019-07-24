package com.mytlogos.enterprise.background.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.mytlogos.enterprise.background.room.model.RoomMedium;
import com.mytlogos.enterprise.model.MediumItem;
import com.mytlogos.enterprise.model.MediumSetting;

import java.util.List;

@Dao
public interface MediumDao extends MultiBaseDao<RoomMedium> {

    @Query("SELECT mediumId FROM RoomMedium;")
    List<Integer> loaded();

    @Query("SELECT title, RoomMedium.mediumId, author, artist, medium, stateTL, stateOrigin, " +
            "countryOfOrigin, languageOfOrigin, lang, series, universe, currentRead, " +
            "(" +
            "   SELECT (COALESCE(RoomEpisode.totalIndex,0) + COALESCE(RoomEpisode.partialIndex,0)) FROM RoomEpisode " +
            "   WHERE currentRead=episodeId" +
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
            "FROM RoomMedium")
    LiveData<List<MediumItem>> getAll();

    @Query("SELECT title, RoomMedium.mediumId, author, artist, medium, stateTL, stateOrigin, " +
            "countryOfOrigin, languageOfOrigin, lang, series, universe, currentRead, " +
            "(" +
            "   SELECT (COALESCE(RoomEpisode.totalIndex,0) + COALESCE(RoomEpisode.partialIndex,0)) FROM RoomEpisode " +
            "   WHERE currentRead=episodeId" +
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
            "FROM RoomMedium INNER JOIN MediaListMediaJoin " +
            "ON MediaListMediaJoin.mediumId=RoomMedium.mediumId " +
            "WHERE listId=:listId " +
            "ORDER BY title")
    LiveData<List<MediumItem>> getListMedia(int listId);

    @Query("SELECT title, RoomMedium.mediumId, author, artist, medium, stateTL, stateOrigin, " +
            "countryOfOrigin, languageOfOrigin, lang, series, universe, currentRead, " +
            "(" +
            "   SELECT (COALESCE(RoomEpisode.totalIndex,0) + COALESCE(RoomEpisode.partialIndex,0)) FROM RoomEpisode " +
            "   WHERE currentRead=episodeId" +
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
            "FROM RoomMedium INNER JOIN ExternalListMediaJoin " +
            "ON ExternalListMediaJoin.mediumId=RoomMedium.mediumId " +
            "WHERE listId=:listId " +
            "ORDER BY title")
    LiveData<List<MediumItem>> getExternalListMedia(int listId);

    @Query("SELECT title, RoomMedium.mediumId, author, artist, medium, stateTL, stateOrigin, " +
            "countryOfOrigin, languageOfOrigin, lang, series, universe, currentRead, toDownload, " +
            "(" +
            "   SELECT (COALESCE(RoomEpisode.totalIndex,0) + COALESCE(RoomEpisode.partialIndex,0)) FROM RoomEpisode " +
            "   WHERE currentRead=episodeId" +
            ") as currentReadEpisode," +
            "(" +
            "   SELECT MAX((COALESCE(RoomEpisode.totalIndex,0) + COALESCE(RoomEpisode.partialIndex,0))) FROM RoomEpisode " +
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
}
