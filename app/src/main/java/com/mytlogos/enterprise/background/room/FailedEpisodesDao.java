package com.mytlogos.enterprise.background.room;

import androidx.room.Dao;
import androidx.room.Query;

import com.mytlogos.enterprise.background.room.model.RoomFailedEpisode;

import java.util.List;

@Dao
public interface FailedEpisodesDao extends MultiBaseDao<RoomFailedEpisode> {
    @Query("SELECT * FROM RoomFailedEpisode")
    List<RoomFailedEpisode> getFailedEpisodes();

    @Query("SELECT * FROM RoomFailedEpisode WHERE episodeId=:episodeId")
    RoomFailedEpisode getFailedEpisode(int episodeId);
}
