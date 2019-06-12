package com.mytlogos.enterprise.background.room;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Update;

public interface BaseDao<T> {
    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(T t);

    @Delete
    void delete(T t);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(T t);
}
