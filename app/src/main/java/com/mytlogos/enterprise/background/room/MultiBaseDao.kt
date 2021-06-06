package com.mytlogos.enterprise.background.room

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface MultiBaseDao<T> : BaseDao<T> {
    @Update
    suspend fun updateBulk(collection: Collection<T>)

    @Delete
    suspend fun deleteBulk(collection: Collection<T>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBulk(collection: Collection<T>)
}