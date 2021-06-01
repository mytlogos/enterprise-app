package com.mytlogos.enterprise.background.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mytlogos.enterprise.background.room.model.RoomPart

@Dao
interface PartDao : MultiBaseDao<RoomPart> {
    @Query("SELECT partId FROM RoomPart;")
    fun loaded(): List<Int>

    @Query("SELECT * FROM RoomPart WHERE mediumId=:mediumId ORDER BY combiIndex DESC")
    fun getParts(mediumId: Int): LiveData<MutableList<RoomPart>>

    @Query("SELECT * FROM RoomPart WHERE mediumId=:mediumId ORDER BY combiIndex DESC")
    fun getPartsNow(mediumId: Int): List<RoomPart>

    @Query("SELECT * FROM RoomPart WHERE partId=:partId")
    fun getPart(partId: Int): RoomPart

    @Query("DELETE FROM RoomPart")
    fun clearAll()

    @Query("SELECT partId FROM RoomPart WHERE mediumId=:mediumId")
    fun getPartsIds(mediumId: Int): MutableList<Int>

    @Query("DELETE FROM RoomPart WHERE partId IN (:ids)")
    fun deletePerId(ids: Collection<Int>)
}