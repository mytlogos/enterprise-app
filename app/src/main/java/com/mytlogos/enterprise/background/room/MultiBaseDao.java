package com.mytlogos.enterprise.background.room;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Update;

import java.util.Collection;

public interface MultiBaseDao<T> extends BaseDao<T> {
    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateBulk(Collection<T> collection);

    @Delete
    void deleteBulk(Collection<T> collection);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBulk(Collection<T> collection);
}
