package com.mytlogos.enterprise.background.room;

import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Update;

import java.util.Collection;

public interface MultiBaseDao<T> extends BaseDao<T> {
    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateBulk(Collection<T> collection);

    @Delete
    void deleteBulk(Collection<T> collection);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBulk(Collection<T> collection);
}
