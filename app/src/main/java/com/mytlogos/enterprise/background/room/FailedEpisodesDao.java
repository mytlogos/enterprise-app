package com.mytlogos.enterprise.background.room;

import androidx.room.Dao;
import androidx.room.Query;

import com.mytlogos.enterprise.background.room.model.RoomFailedEpisode;
import com.mytlogos.enterprise.model.FailedEpisode;

import java.util.Collection;
import java.util.List;

@Dao
public interface FailedEpisodesDao extends MultiBaseDao<RoomFailedEpisode> {
    @Query("SELECT * FROM RoomFailedEpisode")
    List<FailedEpisode> getFailedEpisodes();

    @Query("SELECT * FROM RoomFailedEpisode WHERE episodeId=:episodeId")
    RoomFailedEpisode getFailedEpisode(int episodeId);

    @Query("SELECT * FROM RoomFailedEpisode WHERE episodeId IN (:episodeIds)")
    List<FailedEpisode> getFailedEpisodes(Collection<Integer> episodeIds);

    @Query("DELETE FROM RoomFailedEpisode")
    void clearAll();

    @Query("DELETE FROM RoomFailedEpisode WHERE episodeId IN (:episodeIds)")
    void deleteBulkPerId(Collection<Integer> episodeIds);
}
