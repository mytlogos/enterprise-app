package com.mytlogos.enterprise.background.room;

import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Update;

public interface BaseDao<T> {
    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(T t);

    @Delete
    void delete(T t);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(T t);
}
