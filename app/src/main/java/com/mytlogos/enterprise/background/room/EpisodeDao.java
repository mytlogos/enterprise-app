package com.mytlogos.enterprise.background.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.mytlogos.enterprise.background.room.model.RoomEpisode;

import org.joda.time.DateTime;

import java.util.List;

@Dao
public interface EpisodeDao extends MultiBaseDao<RoomEpisode> {

    @Query("UPDATE RoomEpisode SET progress=:progress, readDate=:readDate WHERE episodeId=:episodeId")
    void update(int episodeId, float progress, DateTime readDate);

    @Query("SELECT episodeId FROM RoomEpisode;")
    List<Integer> loaded();
}
