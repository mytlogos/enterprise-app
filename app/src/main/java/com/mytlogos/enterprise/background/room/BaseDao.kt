package com.mytlogos.enterprise.background.room

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface BaseDao<T> {
    @Update
    suspend fun update(t: T)

    @Delete
    suspend fun delete(t: T)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(t: T)
}