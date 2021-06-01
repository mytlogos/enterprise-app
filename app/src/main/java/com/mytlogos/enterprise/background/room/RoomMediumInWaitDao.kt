package com.mytlogos.enterprise.background.room

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.mytlogos.enterprise.background.room.model.RoomMediumInWait
import com.mytlogos.enterprise.model.MediumInWait

@Dao
interface RoomMediumInWaitDao : MultiBaseDao<RoomMediumInWait> {
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
    fun getByAsc(sortBy: Int, titleFilter: String?, mediumFilter: Int, hostFilter: String?):DataSource.Factory<Int, RoomMediumInWait>

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
    fun getByDesc(sortBy: Int, titleFilter: String?, mediumFilter: Int, hostFilter: String?): DataSource.Factory<Int, RoomMediumInWait>

    @Query("SELECT * FROM RoomMediumInWait WHERE INSTR(title, :title) > 0 AND medium=:medium")
    fun getSimilar(title: String, medium: Int): LiveData<MutableList<MediumInWait>>

    @Query("SELECT * FROM RoomMediumInWait " +
            "WHERE medium=:medium AND INSTR(lower(title), :title) ORDER BY title LIMIT 10")
    fun getSuggestions(title: String, medium: Int): LiveData<MutableList<MediumInWait>>

    @Query("DELETE FROM RoomMediumInWait")
    fun clear()

    @Query("SELECT COUNT(*) FROM RoomMediumInWait")
    fun countMedia(): LiveData<Int>
}