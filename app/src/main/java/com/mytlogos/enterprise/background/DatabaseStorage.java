package com.mytlogos.enterprise.background;

import android.arch.lifecycle.LiveData;

import com.mytlogos.enterprise.model.News;
import com.mytlogos.enterprise.model.User;

import org.joda.time.DateTime;

import java.util.List;


/**
 * Interface for querying and deleting data.
 * It models a server-driven database.
 * <p>
 * To insert or update data, {@link #getPersister(Repository, LoadData, LoadData)} is used to persist data
 * from the server.
 * </p>
 */
public interface DatabaseStorage {
    LiveData<? extends User> getUser();

    void deleteAllUser();

    ClientModelPersister getPersister(Repository repository, LoadData unLoadedData, LoadData loadedData);

    void deleteOldNews();

    boolean isLoading();

    void setLoading(boolean loading);

    void setNewsInterval(DateTime from, DateTime to);

    LoadData getLoadData();

    LiveData<List<News>> getNews();
}
