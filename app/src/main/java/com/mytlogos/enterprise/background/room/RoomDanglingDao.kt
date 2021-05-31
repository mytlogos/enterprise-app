package com.mytlogos.enterprise.background.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mytlogos.enterprise.background.room.model.RoomDanglingMedium
import com.mytlogos.enterprise.model.*

@Dao
interface RoomDanglingDao : MultiBaseDao<RoomDanglingMedium> {
    @get:Query("""SELECT title, RoomMedium.mediumId, author, artist, medium, stateTL, stateOrigin, countryOfOrigin, languageOfOrigin, lang, series, universe, (   SELECT episodeId FROM RoomEpisode    INNER JOIN RoomPart ON RoomPart.partId= RoomEpisode.partId    WHERE mediumId=RoomMedium.mediumId    ORDER BY RoomEpisode.combiIndex DESC    LIMIT 1) as currentRead,(    SELECT RoomEpisode.combiIndex 
    FROM RoomEpisode
    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId
    WHERE RoomPart.mediumId=RoomMedium.mediumId AND RoomEpisode.progress=1    ORDER BY RoomEpisode.combiIndex DESC    LIMIT 1) as currentReadEpisode,(   SELECT MAX(RoomEpisode.combiIndex) FROM RoomEpisode    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId     WHERE RoomPart.mediumId=RoomMedium.mediumId) as lastEpisode , (   SELECT MAX(RoomRelease.releaseDate) FROM RoomEpisode    INNER JOIN RoomRelease ON RoomEpisode.episodeId=RoomRelease.episodeId    INNER JOIN RoomPart ON RoomPart.partId=RoomEpisode.partId     WHERE RoomPart.mediumId=RoomMedium.mediumId) as lastUpdated FROM RoomDanglingMedium INNER JOIN RoomMedium  ON RoomMedium.mediumId=RoomDanglingMedium.mediumId ORDER BY title""")
    val all: LiveData<List<MediumItem>>
}