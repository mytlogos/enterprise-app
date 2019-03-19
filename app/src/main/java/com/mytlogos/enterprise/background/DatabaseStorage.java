package com.mytlogos.enterprise.background;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.mytlogos.enterprise.model.User;

import org.joda.time.DateTime;


public interface DatabaseStorage {
    LiveData<? extends User> getUser();

    void updateUser(@NonNull User user);

    void insertUser(@NonNull User user);

    void deleteAllUser();

    ClientModelPersister getPersister(LoadData unLoadedData, LoadData loadedData);

    void deleteOldNews();

    void changeUser(User newUser);

    boolean isLoading();

    void setLoading(boolean loading);

    void setNewsInterval(DateTime from, DateTime to);

    LoadData getLoadData();
}
