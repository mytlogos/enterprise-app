package com.mytlogos.enterprise.background.room

import androidx.room.Dao
import androidx.room.Query
import com.mytlogos.enterprise.background.room.model.RoomEditEvent

@Dao
interface EditDao : MultiBaseDao<RoomEditEvent?> {
    @get:Query("SELECT * FROM RoomEditEvent")
    val all: MutableList<RoomEditEvent>

    @Query("""SELECT * FROM RoomEditEvent
WHERE (:id <= 0 OR id=:id)
AND (:objectType <= 0 OR objectType=:objectType)
AND (:eventType <= 0 OR eventType=:eventType)""")
    fun getAll(id: Int, objectType: Int, eventType: Int): List<RoomEditEvent>
}