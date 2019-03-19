package com.mytlogos.enterprise.background.objectbox;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.mytlogos.enterprise.background.ClientModelPersister;
import com.mytlogos.enterprise.background.DatabaseStorage;
import com.mytlogos.enterprise.background.LoadData;
import com.mytlogos.enterprise.model.User;

import org.joda.time.DateTime;

public class ObjectBoxStorage implements DatabaseStorage {
    @Override
    public LiveData<? extends User> getUser() {
        return null;
    }

    @Override
    public void updateUser(@NonNull User roomUser) {

    }

    @Override
    public void insertUser(@NonNull User roomUser) {

    }

    @Override
    public void deleteAllUser() {

    }

    @Override
    public ClientModelPersister getPersister(LoadData unLoadedData, LoadData loadedData) {
        return null;
    }

    @Override
    public void deleteOldNews() {

    }

    @Override
    public void changeUser(User newUser) {

    }

    @Override
    public boolean isLoading() {
        return false;
    }

    @Override
    public void setLoading(boolean loading) {

    }

    @Override
    public void setNewsInterval(DateTime from, DateTime to) {

    }

    @Override
    public LoadData getLoadData() {
        return null;
    }
}
