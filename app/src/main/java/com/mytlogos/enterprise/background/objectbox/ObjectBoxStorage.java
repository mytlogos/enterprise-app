package com.mytlogos.enterprise.background.objectbox;

import android.arch.lifecycle.LiveData;

import com.mytlogos.enterprise.background.ClientModelPersister;
import com.mytlogos.enterprise.background.DatabaseStorage;
import com.mytlogos.enterprise.background.LoadData;
import com.mytlogos.enterprise.background.Repository;
import com.mytlogos.enterprise.model.News;
import com.mytlogos.enterprise.model.User;

import org.joda.time.DateTime;

import java.util.List;

public class ObjectBoxStorage implements DatabaseStorage {
    @Override
    public LiveData<? extends User> getUser() {
        return null;
    }

    @Override
    public void deleteAllUser() {

    }

    @Override
    public ClientModelPersister getPersister(Repository repository, LoadData unLoadedData, LoadData loadedData) {
        return null;
    }


    @Override
    public void deleteOldNews() {

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

    @Override
    public LiveData<List<News>> getNews() {
        return null;
    }
}
