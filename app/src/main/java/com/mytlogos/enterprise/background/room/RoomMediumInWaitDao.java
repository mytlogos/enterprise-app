package com.mytlogos.enterprise.background.room;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Query;

import com.mytlogos.enterprise.background.room.model.RoomMediumInWait;
import com.mytlogos.enterprise.model.MediumInWait;

import java.util.List;

@Dao
public interface RoomMediumInWaitDao extends MultiBaseDao<RoomMediumInWait> {

    @Query("SELECT * FROM RoomMediumInWait " +
            "WHERE " +
            "(:titleFilter IS NULL OR INSTR(lower(title), :titleFilter) > 0) " +
            "AND (:mediumFilter = 0 OR (:mediumFilter & medium) > 0) " +
            "AND (:hostFilter IS NULL OR INSTR(lower(link), :hostFilter) > 0) " +
            "ORDER BY " +
            "CASE :sortBy " +
            "WHEN 1 THEN link " +
            "WHEN 2 THEN medium " +
            "WHEN 3 THEN title " +
            "ELSE title " +
            "END ASC")
    DataSource.Factory<Integer, RoomMediumInWait> getByAsc(int sortBy, String titleFilter, int mediumFilter, String hostFilter);

    @Query("SELECT * FROM RoomMediumInWait " +
            "WHERE (:titleFilter IS NULL OR INSTR(lower(title), :titleFilter) > 0) " +
            "AND (:mediumFilter = 0 OR (:mediumFilter & medium) > 0) " +
            "AND (:hostFilter IS NULL OR INSTR(lower(link), :hostFilter) > 0) " +
            "ORDER BY " +
            "CASE :sortBy " +
            "WHEN 1 THEN link " +
            "WHEN 2 THEN medium " +
            "WHEN 3 THEN title " +
            "ELSE title " +
            "END DESC")
    DataSource.Factory<Integer, RoomMediumInWait> getByDesc(int sortBy, String titleFilter, int mediumFilter, String hostFilter);

    @Query("SELECT * FROM RoomMediumInWait WHERE INSTR(title, :title) > 0 AND medium=:medium")
    LiveData<List<MediumInWait>> getSimilar(String title, int medium);

    @Query("SELECT * FROM RoomMediumInWait " +
            "WHERE medium=:medium AND INSTR(lower(title), :title) ORDER BY title LIMIT 10")
    LiveData<List<MediumInWait>> getSuggestions(String title, int medium);

    @Query("DELETE FROM RoomMediumInWait")
    void clear();

    @Query("SELECT COUNT(*) FROM RoomMediumInWait")
    LiveData<Integer> countMedia();
}
