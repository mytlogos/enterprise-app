package com.mytlogos.enterprise.background.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.mytlogos.enterprise.background.room.model.RoomMediumPart;
import com.mytlogos.enterprise.background.room.model.RoomPartEpisode;

import java.util.Collection;
import java.util.List;

@Dao
public interface DataStructureDao {
    @Query("SELECT partId FROM RoomMediumPart WHERE mediumId=:mediumId")
    List<Integer> getPartJoin(int mediumId);

    @Insert
    void addPartJoin(Collection<RoomMediumPart> mediumParts);

    @Delete
    void deletePartJoin(Collection<RoomMediumPart> mediumParts);

    @Query("DELETE FROM RoomMediumPart WHERE mediumId=:mediumId")
    void clearPartJoin(int mediumId);

    @Query("SELECT episodeId FROM RoomPartEpisode WHERE partId=:partId")
    List<Integer> getEpisodeJoin(int partId);

    @Query("DELETE FROM RoomPartEpisode WHERE partId=:partId")
    void clearEpisodeJoin(int partId);

    @Insert
    void addEpisodeJoin(Collection<RoomPartEpisode> partEpisodes);

    @Delete
    void deleteEpisodeJoin(Collection<RoomPartEpisode> partEpisodes);
}
