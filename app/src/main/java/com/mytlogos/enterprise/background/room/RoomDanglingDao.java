package com.mytlogos.enterprise.background.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.mytlogos.enterprise.background.room.model.RoomDanglingMedium;
import com.mytlogos.enterprise.model.MediumItem;

import java.util.List;

@Dao
public interface RoomDanglingDao extends MultiBaseDao<RoomDanglingMedium> {

    @Query(
            "SELECT title, RoomMedium.mediumId, author, artist, medium, stateTL, stateOrigin, " +
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
                    "FROM RoomDanglingMedium INNER JOIN RoomMedium  " +
                    "ON RoomMedium.mediumId=RoomDanglingMedium.mediumId " +
                    "ORDER BY title")
    LiveData<List<MediumItem>> getAll();
}
