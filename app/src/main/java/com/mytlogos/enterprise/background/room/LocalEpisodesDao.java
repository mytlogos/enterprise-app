package com.mytlogos.enterprise.background.room;

import androidx.room.Dao;
import androidx.room.Query;

import com.mytlogos.enterprise.background.room.model.RoomSavedEpisode;

import java.util.List;

@Dao
public interface LocalEpisodesDao extends MultiBaseDao<RoomSavedEpisode> {

    @Query("SELECT * FROM RoomSavedEpisode")
    List<RoomSavedEpisode> getAllLocalEpisodes();

    @Query("SELECT RoomSavedEpisode.episodeId, RoomSavedEpisode.path FROM RoomSavedEpisode " +
            "INNER JOIN RoomEpisode ON RoomSavedEpisode.episodeId=RoomEpisode.episodeId " +
            "WHERE progress = 1")
    List<RoomSavedEpisode> getAllToDeleteLocalEpisodes();
}
