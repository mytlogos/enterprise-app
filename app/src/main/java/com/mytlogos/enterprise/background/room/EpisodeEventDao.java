package com.mytlogos.enterprise.background.room;

import androidx.room.Dao;

import com.mytlogos.enterprise.background.room.model.RoomEpisodeEvent;

@Dao
public interface EpisodeEventDao extends MultiBaseDao<RoomEpisodeEvent> {

}
