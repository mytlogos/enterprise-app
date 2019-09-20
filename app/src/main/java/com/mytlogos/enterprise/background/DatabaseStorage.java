package com.mytlogos.enterprise.background;

import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.mytlogos.enterprise.model.DisplayEpisode;
import com.mytlogos.enterprise.model.Episode;
import com.mytlogos.enterprise.model.ExternalUser;
import com.mytlogos.enterprise.model.FailedEpisode;
import com.mytlogos.enterprise.model.HomeStats;
import com.mytlogos.enterprise.model.MediaList;
import com.mytlogos.enterprise.model.MediaListSetting;
import com.mytlogos.enterprise.model.MediumInWait;
import com.mytlogos.enterprise.model.MediumItem;
import com.mytlogos.enterprise.model.MediumSetting;
import com.mytlogos.enterprise.model.News;
import com.mytlogos.enterprise.model.NotificationItem;
import com.mytlogos.enterprise.model.ReadEpisode;
import com.mytlogos.enterprise.model.SimpleEpisode;
import com.mytlogos.enterprise.model.SimpleMedium;
import com.mytlogos.enterprise.model.SpaceMedium;
import com.mytlogos.enterprise.model.ToDownload;
import com.mytlogos.enterprise.model.TocEpisode;
import com.mytlogos.enterprise.model.User;
import com.mytlogos.enterprise.tools.Sortings;

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
    LiveData<User> getUser();

    LiveData<HomeStats> getHomeStats();

    void deleteAllUser();

    ClientModelPersister getPersister(Repository repository, LoadData loadedData);

    DependantGenerator getDependantGenerator(LoadData loadedData);

    void deleteOldNews();

    boolean isLoading();

    void setLoading(boolean loading);

    LoadData getLoadData();

    LiveData<PagedList<News>> getNews();

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

    LiveData<PagedList<DisplayEpisode>> getUnreadEpisodes(int saved, int medium, int read);

    LiveData<PagedList<DisplayEpisode>> getUnreadEpisodesGrouped(int saved, int medium);

    LiveData<List<MediaList>> getLists();

    void insertDanglingMedia(Collection<Integer> mediaIds);

    void removeDanglingMedia(Collection<Integer> mediaIds);

    LiveData<? extends MediaListSetting> getListSetting(int id, boolean isExternal);

    void updateToDownload(boolean add, ToDownload toDownload);

    LiveData<PagedList<MediumItem>> getAllMedia(Sortings sortings, String title, int medium, String author, DateTime lastUpdate, int minCountEpisodes, int minCountReadEpisodes);

    LiveData<MediumSetting> getMediumSettings(int mediumId);

    LiveData<PagedList<TocEpisode>> getToc(int mediumId, Sortings sortings, byte read, byte saved);

    LiveData<List<MediumItem>> getMediumItems(int listId, boolean isExternal);

    boolean listExists(String listName);

    int countSavedEpisodes(Integer mediumId);

    List<Integer> getSavedEpisodes(int mediumId);

    Episode getEpisode(int episodeId);

    List<SimpleEpisode> getSimpleEpisodes(Collection<Integer> ids);

    void updateProgress(Collection<Integer> episodeIds, float progress);

    LiveData<PagedList<MediumInWait>> getMediaInWaitBy(String filter, int mediumFilter, String hostFilter, Sortings sortings);

    LiveData<PagedList<ReadEpisode>> getReadTodayEpisodes();

    LiveData<List<MediaList>> getInternLists();

    void addItemsToList(int listId, Collection<Integer> ids);

    LiveData<List<MediumInWait>> getSimilarMediaInWait(MediumInWait mediumInWait);

    LiveData<List<SimpleMedium>> getMediaSuggestions(String title, int medium);

    LiveData<List<MediumInWait>> getMediaInWaitSuggestions(String title, int medium);

    LiveData<List<MediaList>> getListSuggestion(String name);

    LiveData<Boolean> onDownloadAble();

    void clearMediaInWait();

    void deleteMediaInWait(Collection<MediumInWait> toDelete);

    LiveData<List<MediumItem>> getAllDanglingMedia();

    void removeItemFromList(int listId, int mediumId);

    void removeItemFromList(int listId, Collection<Integer> mediumId);

    void moveItemsToList(int oldListId, int listId, Collection<Integer> ids);

    LiveData<PagedList<ExternalUser>> getExternalUser();

    SpaceMedium getSpaceMedium(int mediumId);

    int getMediumType(Integer mediumId);

    List<String> getReleaseLinks(int episodeId);

    void clearLocalMediaData();

    LiveData<PagedList<NotificationItem>> getNotifications();

    void updateFailedDownload(int episodeId);

    List<FailedEpisode> getFailedEpisodes(Collection<Integer> episodeIds);

    void addNotification(NotificationItem notification);

    SimpleEpisode getSimpleEpisode(int episodeId);

    SimpleMedium getSimpleMedium(int mediumId);

    void clearNotifications();

    void clearFailEpisodes();

    Collection<Integer> getAllEpisodes(int mediumId);

    void syncProgress();

    void updateDataStructure(List<Integer> mediaIds, List<Integer> partIds);

    List<Integer> getEpisodeIdsWithHigherIndex(double combiIndex, int mediumId, boolean read);

    List<Integer> getEpisodeIdsWithHigherIndex(double combiIndex, int mediumId);

    List<Integer> getEpisodeIdsWithLowerIndex(double combiIndex, int mediumId, boolean read);

    List<Integer> getEpisodeIdsWithLowerIndex(double combiIndex, int mediumId);

    Collection<Integer> getSavedEpisodeIdsWithHigherIndex(double combiIndex, int mediumId);

    Collection<Integer> getSavedEpisodeIdsWithLowerIndex(double combiIndex, int mediumId);

    void removeEpisodes(List<Integer> episodeIds);

    void removeParts(Collection<Integer> partIds);
}
