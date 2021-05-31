package com.mytlogos.enterprise.background.room

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface MultiBaseDao<T> : BaseDao<T> {
    @Update
    fun updateBulk(collection: Collection<T>)

    @Delete
    fun deleteBulk(collection: Collection<T>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBulk(collection: Collection<T>)
}