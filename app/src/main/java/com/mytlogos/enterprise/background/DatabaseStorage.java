package com.mytlogos.enterprise.background;

import androidx.lifecycle.LiveData;

import com.mytlogos.enterprise.model.MediaList;
import com.mytlogos.enterprise.model.MediaListSetting;
import com.mytlogos.enterprise.model.MediumItem;
import com.mytlogos.enterprise.model.MediumSetting;
import com.mytlogos.enterprise.model.News;
import com.mytlogos.enterprise.model.ToDownload;
import com.mytlogos.enterprise.model.UnreadEpisode;
import com.mytlogos.enterprise.model.User;

import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;


/**
 * Interface for querying and deleting data.
 * It models a server-driven database.
 * <p>
 * To insert or update data, {@link #getPersister(Repository, LoadData)} is used to persist data
 * from the server.
 * </p>
 */
public interface DatabaseStorage {
    LiveData<? extends User> getUser();

    void deleteAllUser();

    ClientModelPersister getPersister(Repository repository, LoadData loadedData);

    DependantGenerator getDependantGenerator(LoadData loadedData);

    void deleteOldNews();

    boolean isLoading();

    void setLoading(boolean loading);

    void setNewsInterval(DateTime from, DateTime to);

    LoadData getLoadData();

    LiveData<List<News>> getNews();

    List<Integer> getSavedEpisodes();

    List<Integer> getToDeleteEpisodes();

    void updateSaved(int episodeId, boolean saved);

    void updateSaved(Collection<Integer> episodeIds, boolean saved);

    List<ToDownload> getAllToDownloads();

    void removeToDownloads(Collection<ToDownload> toDownloads);

    Collection<Integer> getListItems(Integer listId);

    LiveData<List<Integer>> getLiveListItems(Integer listId);

    Collection<Integer> getExternalListItems(Integer externalListId);

    LiveData<List<Integer>> getLiveExternalListItems(Integer externalListId);

    List<Integer> getDownloadableEpisodes(Integer mediumId);

    List<Integer> getDownloadableEpisodes(Collection<Integer> mediumId);

    LiveData<List<UnreadEpisode>> getUnreadEpisodes();

    LiveData<List<MediaList>> getLists();

    LiveData<? extends MediaListSetting> getListSetting(int id, boolean isExternal);

    void updateToDownload(boolean add, ToDownload toDownload);

    LiveData<List<MediumItem>> getAllMedia();

    LiveData<MediumSetting> getMediumSettings(int mediumId);
}
