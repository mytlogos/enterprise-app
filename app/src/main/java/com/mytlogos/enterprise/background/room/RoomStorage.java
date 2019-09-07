package com.mytlogos.enterprise.background.room;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.mytlogos.enterprise.background.ClientConsumer;
import com.mytlogos.enterprise.background.ClientModelPersister;
import com.mytlogos.enterprise.background.DatabaseStorage;
import com.mytlogos.enterprise.background.DependantGenerator;
import com.mytlogos.enterprise.background.LoadData;
import com.mytlogos.enterprise.background.Repository;
import com.mytlogos.enterprise.background.RoomConverter;
import com.mytlogos.enterprise.background.TaskManager;
import com.mytlogos.enterprise.background.api.model.ClientEpisode;
import com.mytlogos.enterprise.background.api.model.ClientExternalMediaList;
import com.mytlogos.enterprise.background.api.model.ClientExternalUser;
import com.mytlogos.enterprise.background.api.model.ClientListQuery;
import com.mytlogos.enterprise.background.api.model.ClientMediaList;
import com.mytlogos.enterprise.background.api.model.ClientMedium;
import com.mytlogos.enterprise.background.api.model.ClientMediumInWait;
import com.mytlogos.enterprise.background.api.model.ClientMultiListQuery;
import com.mytlogos.enterprise.background.api.model.ClientNews;
import com.mytlogos.enterprise.background.api.model.ClientPart;
import com.mytlogos.enterprise.background.api.model.ClientReadEpisode;
import com.mytlogos.enterprise.background.api.model.ClientSimpleUser;
import com.mytlogos.enterprise.background.api.model.ClientUpdateUser;
import com.mytlogos.enterprise.background.api.model.ClientUser;
import com.mytlogos.enterprise.background.resourceLoader.DependantValue;
import com.mytlogos.enterprise.background.resourceLoader.DependencyTask;
import com.mytlogos.enterprise.background.resourceLoader.LoadWorkGenerator;
import com.mytlogos.enterprise.background.resourceLoader.LoadWorker;
import com.mytlogos.enterprise.background.room.model.RoomEpisode;
import com.mytlogos.enterprise.background.room.model.RoomExternListView;
import com.mytlogos.enterprise.background.room.model.RoomExternalMediaList;
import com.mytlogos.enterprise.background.room.model.RoomExternalUser;
import com.mytlogos.enterprise.background.room.model.RoomFailedEpisode;
import com.mytlogos.enterprise.background.room.model.RoomMediaList;
import com.mytlogos.enterprise.background.room.model.RoomMedium;
import com.mytlogos.enterprise.background.room.model.RoomMediumInWait;
import com.mytlogos.enterprise.background.room.model.RoomMediumPart;
import com.mytlogos.enterprise.background.room.model.RoomMediumProgress;
import com.mytlogos.enterprise.background.room.model.RoomNews;
import com.mytlogos.enterprise.background.room.model.RoomNotification;
import com.mytlogos.enterprise.background.room.model.RoomPart;
import com.mytlogos.enterprise.background.room.model.RoomPartEpisode;
import com.mytlogos.enterprise.background.room.model.RoomProgressComparison;
import com.mytlogos.enterprise.background.room.model.RoomRelease;
import com.mytlogos.enterprise.background.room.model.RoomToDownload;
import com.mytlogos.enterprise.background.room.model.RoomTocEpisode;
import com.mytlogos.enterprise.background.room.model.RoomUser;
import com.mytlogos.enterprise.model.DisplayUnreadEpisode;
import com.mytlogos.enterprise.model.Episode;
import com.mytlogos.enterprise.model.ExternalMediaList;
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
import com.mytlogos.enterprise.tools.Utils;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoomStorage implements DatabaseStorage {

    private final UserDao userDao;
    private final NewsDao newsDao;
    private final EpisodeDao episodeDao;
    private final PartDao partDao;
    private final MediumDao mediumDao;
    private final MediaListDao mediaListDao;
    private final ExternalMediaListDao externalMediaListDao;
    private final ExternalUserDao externalUserDao;
    private final NotificationDao notificationDao;
    private final FailedEpisodesDao failedEpisodesDao;
    private final LiveData<User> userLiveData;
    private final RoomMediumInWaitDao mediumInWaitDao;
    private final RoomDanglingDao roomDanglingDao;
    private final MediumProgressDao mediumProgressDao;
    private final DataStructureDao dataStructureDao;
    private boolean loading = false;
    private final ToDownloadDao toDownloadDao;

    public RoomStorage(Application application) {
        AbstractDatabase database = AbstractDatabase.getInstance(application);
        userDao = database.userDao();
        newsDao = database.newsDao();
        externalUserDao = database.externalUserDao();
        externalMediaListDao = database.externalMediaListDao();
        mediaListDao = database.mediaListDao();
        mediumDao = database.mediumDao();
        partDao = database.partDao();
        episodeDao = database.episodeDao();
        toDownloadDao = database.toDownloadDao();
        mediumInWaitDao = database.roomMediumInWaitDao();
        roomDanglingDao = database.roomDanglingDao();
        notificationDao = database.notificationDao();
        mediumProgressDao = database.mediumProgressDao();
        failedEpisodesDao = database.failedEpisodesDao();
        dataStructureDao = database.dataStructureDao();
        userLiveData = this.userDao.getUser();
    }

    @Override
    public LiveData<User> getUser() {
        return this.userLiveData;
    }

    @Override
    public LiveData<HomeStats> getHomeStats() {
        return this.userDao.getHomeStats();
    }

    @Override
    public void deleteAllUser() {
        TaskManager.runTask(userDao::deleteAllUser);
    }

    @Override
    public ClientModelPersister getPersister(Repository repository, LoadData loadedData) {
        return new RoomModelPersister(loadedData, repository);
    }

    @Override
    public DependantGenerator getDependantGenerator(LoadData loadedData) {
        return new RoomDependantGenerator(loadedData);
    }

    @Override
    public void deleteOldNews() {
        TaskManager.runTask(newsDao::deleteOldNews);
    }

    @Override
    public boolean isLoading() {
        return !this.loading;
    }

    @Override
    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    @Override
    public LoadData getLoadData() {
        // todo maybe load this asynchronous?
        LoadData data = new LoadData();
        data.getEpisodes().addAll(this.episodeDao.loaded());
        data.getPart().addAll(this.partDao.loaded());
        data.getNews().addAll(this.newsDao.loaded());
        data.getMedia().addAll(this.mediumDao.loaded());
        data.getExternalMediaList().addAll(this.externalMediaListDao.loaded());
        data.getExternalUser().addAll(this.externalUserDao.loaded());
        data.getMediaList().addAll(this.mediaListDao.loaded());
        return data;
    }

    @Override
    public LiveData<PagedList<News>> getNews() {
        return new LivePagedListBuilder<>(this.newsDao.getNews(), 50).build();
    }

    @Override
    public List<Integer> getSavedEpisodes() {
        return this.episodeDao.getAllSavedEpisodes();
    }

    @Override
    public List<Integer> getToDeleteEpisodes() {
        return this.episodeDao.getAllToDeleteLocalEpisodes();
    }

    @Override
    public void updateSaved(int episodeId, boolean saved) {
        this.episodeDao.updateSaved(episodeId, saved);
    }

    @Override
    public void updateSaved(Collection<Integer> episodeIds, boolean saved) {
        this.episodeDao.updateSaved(episodeIds, saved);

        if (saved) {
            this.failedEpisodesDao.deleteBulkPerId(episodeIds);
        }
    }

    @Override
    public List<ToDownload> getAllToDownloads() {
        return new RoomConverter().convertRoomToDownload(this.toDownloadDao.getAll());
    }

    @Override
    public void removeToDownloads(Collection<ToDownload> toDownloads) {
        for (RoomToDownload toDownload : new RoomConverter().convertToDownload(toDownloads)) {
            this.toDownloadDao.deleteToDownload(toDownload.getMediumId(), toDownload.getListId(), toDownload.getExternalListId());
        }
    }

    @Override
    public Collection<Integer> getListItems(Integer listId) {
        return this.mediaListDao.getListItems(listId);
    }


    @Override
    public LiveData<List<Integer>> getLiveListItems(Integer listId) {
        return this.mediaListDao.getLiveListItems(listId);
    }

    @Override
    public Collection<Integer> getExternalListItems(Integer externalListId) {
        return this.externalMediaListDao.getExternalListItems(externalListId);
    }

    @Override
    public LiveData<List<Integer>> getLiveExternalListItems(Integer externalListId) {
        return this.externalMediaListDao.getLiveExternalListItems(externalListId);
    }

    @Override
    public List<Integer> getDownloadableEpisodes(Integer mediumId) {
        return this.episodeDao.getDownloadableEpisodes(mediumId);
    }

    @Override
    public List<Integer> getDownloadableEpisodes(Collection<Integer> mediaIds) {
        return this.episodeDao.getDownloadableEpisodes(mediaIds);
    }

    @Override
    public LiveData<PagedList<DisplayUnreadEpisode>> getUnreadEpisodes(int saved, int medium) {
        RoomConverter converter = new RoomConverter();
        return new LivePagedListBuilder<>(
                this.episodeDao.getUnreadEpisodes(saved, medium).map(converter::convertRoomEpisode),
                50
        ).build();
    }

    @Override
    public LiveData<PagedList<DisplayUnreadEpisode>> getUnreadEpisodesGrouped(int saved, int medium) {
        RoomConverter converter = new RoomConverter();
        return new LivePagedListBuilder<>(
                this.episodeDao.getUnreadEpisodesGrouped(saved, medium).map(converter::convertRoomEpisode),
                50
        ).build();
    }

    @Override
    public LiveData<List<MediaList>> getLists() {
        MediatorLiveData<List<MediaList>> liveData = new MediatorLiveData<>();

        liveData.addSource(this.mediaListDao.getListViews(), mediaLists -> {
            HashSet<MediaList> set = new HashSet<>();

            if (liveData.getValue() != null) {
                set.addAll(liveData.getValue());
            }

            set.addAll(mediaLists);
            liveData.setValue(new ArrayList<>(set));
        });

        liveData.addSource(this.externalMediaListDao.getExternalListViews(), roomMediaLists -> {
            List<MediaList> mediaLists = new ArrayList<>();

            for (RoomExternListView list : roomMediaLists) {
                RoomExternalMediaList mediaList = list.getMediaList();
                mediaLists.add(new ExternalMediaList(
                        mediaList.uuid,
                        mediaList.externalListId,
                        mediaList.name,
                        mediaList.medium,
                        mediaList.url,
                        list.getSize()
                ));
            }

            HashSet<MediaList> set = new HashSet<>();

            if (liveData.getValue() != null) {
                set.addAll(liveData.getValue());
            }

            set.addAll(mediaLists);
            liveData.setValue(new ArrayList<>(set));
        });
        return liveData;
    }

    @Override
    public void insertDanglingMedia(Collection<Integer> mediaIds) {
        List<Integer> listMedia = this.mediaListDao.getAllLinkedMedia();
        List<Integer> externalListMedia = this.externalMediaListDao.getAllLinkedMedia();
        mediaIds.removeAll(listMedia);
        mediaIds.removeAll(externalListMedia);

        if (mediaIds.isEmpty()) {
            return;
        }
        RoomConverter converter = new RoomConverter();
        this.roomDanglingDao.insertBulk(converter.convertToDangling(mediaIds));
    }

    @Override
    public void removeDanglingMedia(Collection<Integer> mediaIds) {
        if (mediaIds.isEmpty()) {
            return;
        }
        RoomConverter converter = new RoomConverter();
        this.roomDanglingDao.deleteBulk(converter.convertToDangling(mediaIds));
    }

    @Override
    public LiveData<? extends MediaListSetting> getListSetting(int id, boolean isExternal) {
        if (isExternal) {
            return this.externalMediaListDao.getExternalListSetting(id);
        }
        return this.mediaListDao.getListSettings(id);
    }

    @Override
    public void updateToDownload(boolean add, ToDownload toDownload) {
        if (add) {
            this.toDownloadDao.insert(new RoomConverter().convert(toDownload));
        } else {
            this.toDownloadDao.deleteToDownload(
                    toDownload.getMediumId(),
                    toDownload.getListId(),
                    toDownload.getExternalListId()
            );
        }
    }

    @Override
    public LiveData<PagedList<MediumItem>> getAllMedia(Sortings sortings, String title, int medium, String author, DateTime lastUpdate, int minCountEpisodes, int minCountReadEpisodes) {
        int sortValue = sortings.getSortValue();
        if (sortValue > 0) {
            return new LivePagedListBuilder<>(this.mediumDao.getAllAsc(sortValue, title, medium, author, lastUpdate, minCountEpisodes, minCountReadEpisodes), 50).build();
        } else {
            sortValue = -sortValue;
            return new LivePagedListBuilder<>(this.mediumDao.getAllDesc(sortValue, title, medium, author, lastUpdate, minCountEpisodes, minCountReadEpisodes), 50).build();
        }
    }

    @Override
    public LiveData<MediumSetting> getMediumSettings(int mediumId) {
        return this.mediumDao.getMediumSettings(mediumId);
    }

    @Override
    public LiveData<PagedList<TocEpisode>> getToc(int mediumId, Sortings sortings, byte read, byte saved) {
        DataSource.Factory<Integer, RoomTocEpisode> episodes;

        if (sortings.getSortValue() > 0) {
            episodes = this.episodeDao.getTocEpisodesAsc(mediumId, read, saved);
        } else {
            episodes = this.episodeDao.getTocEpisodesDesc(mediumId, read, saved);
        }
        RoomConverter converter = new RoomConverter();

        return new LivePagedListBuilder<>(episodes.map(converter::convertTocEpisode), 50).build();
    }

    @Override
    public LiveData<List<MediumItem>> getMediumItems(int listId, boolean isExternal) {
        if (isExternal) {
            return this.mediumDao.getExternalListMedia(listId);
        } else {
            return this.mediumDao.getListMedia(listId);
        }
    }

    @Override
    public boolean listExists(String listName) {
        return this.mediaListDao.listExists(listName);
    }

    @Override
    public int countSavedEpisodes(Integer mediumId) {
        return this.episodeDao.countSavedEpisodes(mediumId);
    }

    @Override
    public List<Integer> getSavedEpisodes(int mediumId) {
        return this.episodeDao.getSavedEpisodes(mediumId);
    }

    @Override
    public Episode getEpisode(int episodeId) {
        RoomConverter converter = new RoomConverter();
        RoomEpisode roomEpisode = this.episodeDao.getEpisode(episodeId);
        return converter.convert(roomEpisode);
    }

    @Override
    public List<SimpleEpisode> getSimpleEpisodes(Collection<Integer> ids) {
        return this.episodeDao.getSimpleEpisodes(ids);
    }

    @Override
    public void updateProgress(Collection<Integer> episodeIds, float progress) {
        this.episodeDao.updateProgress(episodeIds, progress, DateTime.now());
    }

    @Override
    public LiveData<PagedList<MediumInWait>> getMediaInWaitBy(String filter, int mediumFilter, String hostFilter, Sortings sortings) {
        DataSource.Factory<Integer, RoomMediumInWait> factory;
        int sortValue = sortings.getSortValue();

        if (sortValue < 0) {
            sortValue = -sortValue;
            factory = this.mediumInWaitDao.getByDesc(sortValue, filter, mediumFilter, hostFilter);
        } else {
            factory = this.mediumInWaitDao.getByAsc(sortValue, filter, mediumFilter, hostFilter);
        }
        RoomConverter converter = new RoomConverter();
        return new LivePagedListBuilder<>(
                factory.map(converter::convert),
                50
        ).build();
    }

    @Override
    public LiveData<PagedList<ReadEpisode>> getReadTodayEpisodes() {
        RoomConverter converter = new RoomConverter();
        return new LivePagedListBuilder<>(
                this.episodeDao.getReadTodayEpisodes().map(converter::convert),
                50
        ).build();
    }

    @Override
    public LiveData<List<MediaList>> getInternLists() {
        return this.mediaListDao.getListViews();
    }

    @Override
    public void addItemsToList(int listId, Collection<Integer> ids) {
        List<RoomMediaList.MediaListMediaJoin> joins = new ArrayList<>();
        for (Integer id : ids) {
            joins.add(new RoomMediaList.MediaListMediaJoin(listId, id));
        }
        this.mediaListDao.addJoin(joins);
    }

    @Override
    public LiveData<List<MediumInWait>> getSimilarMediaInWait(MediumInWait mediumInWait) {
        return this.mediumInWaitDao.getSimilar(mediumInWait.getTitle(), mediumInWait.getMedium());
    }

    @Override
    public LiveData<List<SimpleMedium>> getMediaSuggestions(String title, int medium) {
        return this.mediumDao.getSuggestions(title, medium);
    }

    @Override
    public LiveData<List<MediumInWait>> getMediaInWaitSuggestions(String title, int medium) {
        return this.mediumInWaitDao.getSuggestions(title, medium);
    }

    @Override
    public LiveData<List<MediaList>> getListSuggestion(String name) {
        return this.mediaListDao.getSuggestion(name);
    }

    @Override
    public LiveData<Boolean> onDownloadAble() {
        MutableLiveData<Integer> previousDownloadCount = new MutableLiveData<>();
        MutableLiveData<Integer> previousEpisodeCount = new MutableLiveData<>();

        LiveData<Integer> toDownloadCount = this.toDownloadDao.countMediaRows();
        LiveData<Integer> downloadableEpisodeCount = this.episodeDao.countDownloadableRows();

        MediatorLiveData<Boolean> downloadAbles = new MediatorLiveData<>();
        downloadAbles.addSource(toDownloadCount, input -> {
            int previous = this.getOr(previousDownloadCount.getValue(), 0);
            int current = this.getOr(input, 0);

            previousDownloadCount.setValue(current);
            downloadAbles.postValue(current > previous);
        });
        downloadAbles.addSource(downloadableEpisodeCount, input -> {
            int previous = this.getOr(previousEpisodeCount.getValue(), 0);
            int current = this.getOr(input, 0);

            previousEpisodeCount.setValue(current);
            downloadAbles.postValue(current > previous);
        });
        return downloadAbles;
    }

    @Override
    public void clearMediaInWait() {
        this.mediumInWaitDao.clear();
    }

    @Override
    public void deleteMediaInWait(Collection<MediumInWait> toDelete) {
        RoomConverter converter = new RoomConverter();
        this.mediumInWaitDao.deleteBulk(converter.convertMediaInWait(toDelete));
    }

    @Override
    public LiveData<List<MediumItem>> getAllDanglingMedia() {
        return this.roomDanglingDao.getAll();
    }

    @Override
    public void removeItemFromList(int listId, int mediumId) {
        this.mediaListDao.removeJoin(new RoomMediaList.MediaListMediaJoin(listId, mediumId));
    }

    @Override
    public void removeItemFromList(int listId, Collection<Integer> mediumId) {
        this.mediaListDao.removeJoin(listId, mediumId);
    }

    @Override
    public void moveItemsToList(int oldListId, int newListId, Collection<Integer> ids) {
        Collection<RoomMediaList.MediaListMediaJoin> oldJoins = new ArrayList<>();
        Collection<RoomMediaList.MediaListMediaJoin> newJoins = new ArrayList<>();

        for (Integer id : ids) {
            oldJoins.add(new RoomMediaList.MediaListMediaJoin(oldListId, id));
            newJoins.add(new RoomMediaList.MediaListMediaJoin(newListId, id));
        }

        this.mediaListDao.moveJoins(oldJoins, newJoins);
    }

    @Override
    public LiveData<PagedList<ExternalUser>> getExternalUser() {
        return new LivePagedListBuilder<>(this.externalUserDao.getAll(), 50).build();
    }

    @Override
    public SpaceMedium getSpaceMedium(int mediumId) {
        return this.mediumDao.getSpaceMedium(mediumId);
    }

    @Override
    public int getMediumType(Integer mediumId) {
        return this.mediumDao.getMediumType(mediumId);
    }

    @Override
    public List<String> getReleaseLinks(int episodeId) {
        return this.episodeDao.getReleaseLinks(episodeId);
    }

    @Override
    public List<Integer> getSavedEpisodeIdsWithLowerIndex(int episodeId, boolean read) {
        RoomEpisode episode = this.episodeDao.getEpisode(episodeId);
        RoomPart part = this.partDao.getPart(episode.getPartId());

        return this.episodeDao.getEpisodeIdsWithLowerIndex(
                part.getMediumId(),
                episode.getCombiIndex(),
                part.getCombiIndex(),
                read
        );
    }

    @Override
    public void clearLocalMediaData() {
        this.failedEpisodesDao.clearAll();
        this.episodeDao.clearAllReleases();
        this.episodeDao.clearAll();
        this.partDao.clearAll();
        this.externalMediaListDao.clearJoins();
        this.mediaListDao.clearJoins();
        this.clearMediaInWait();
    }

    @Override
    public LiveData<PagedList<NotificationItem>> getNotifications() {
        return new LivePagedListBuilder<>(this.notificationDao.getNotifications(), 50).build();
    }

    @Override
    public void updateFailedDownload(int episodeId) {
        RoomFailedEpisode failedEpisode = this.failedEpisodesDao.getFailedEpisode(episodeId);
        int failedCount = 0;

        if (failedEpisode != null) {
            failedCount = failedEpisode.getFailCount();
        }
        failedCount++;
        this.failedEpisodesDao.insert(new RoomFailedEpisode(episodeId, failedCount));
    }

    @Override
    public List<FailedEpisode> getFailedEpisodes(Collection<Integer> episodeIds) {
        return this.failedEpisodesDao.getFailedEpisodes(episodeIds);
    }


    @Override
    public void addNotification(NotificationItem notification) {
        if (notification == null) {
            return;
        }
        this.notificationDao.insert(new RoomNotification(
                notification.getTitle(),
                notification.getDescription(),
                notification.getDateTime()
        ));
    }

    @Override
    public SimpleEpisode getSimpleEpisode(int episodeId) {
        return this.episodeDao.getSimpleEpisode(episodeId);
    }

    @Override
    public SimpleMedium getSimpleMedium(int mediumId) {
        return this.mediumDao.getSimpleMedium(mediumId);
    }

    @Override
    public void clearNotifications() {
        this.notificationDao.deleteAll();
    }

    @Override
    public void clearFailEpisodes() {
        this.failedEpisodesDao.clearAll();
    }

    @Override
    public Collection<Integer> getAllEpisodes(int mediumId) {
        return this.episodeDao.getAllEpisodes(mediumId);
    }

    @Override
    public Collection<Integer> getSavedEpisodeIdsWithLowerIndex(int episodeId) {
        RoomEpisode episode = this.episodeDao.getEpisode(episodeId);
        RoomPart part = this.partDao.getPart(episode.getPartId());

        return this.episodeDao.getSavedEpisodeIdsWithLowerIndex(
                part.getMediumId(),
                episode.getCombiIndex(),
                part.getCombiIndex()
        );
    }

    @Override
    public void syncProgress() {
        List<RoomProgressComparison> all = this.mediumProgressDao.getComparison();

        for (RoomProgressComparison comparison : all) {
            if (comparison.getCurrentMaxReadIndex() != 0) {
                this.mediumProgressDao.update(new RoomMediumProgress(
                        comparison.getMediumId(),
                        comparison.getCurrentMaxReadIndex())
                );
                continue;
            }
            List<RoomPart> parts = this.partDao.getPartsNow(comparison.getMediumId());

            for (RoomPart part : parts) {
                List<Integer> episodeIds = this.episodeDao.getEpisodeIdsWithLowerIndex(
                        comparison.getMediumId(),
                        comparison.getCurrentReadIndex(),
                        part.getCombiIndex(),
                        true
                );
                try {
                    Utils.doPartitioned(episodeIds, ids -> {
                        this.episodeDao.updateProgress(ids, 1, DateTime.now());
                        return false;
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void updateDataStructure(List<Integer> mediaIds, List<Integer> partIds) {
        for (Integer mediumId : mediaIds) {
            List<Integer> mediumPartIds = this.dataStructureDao.getPartJoin(mediumId);
            List<Integer> availablePartIds = this.partDao.getPartsIds(mediumId);
            availablePartIds.removeAll(mediumPartIds);

            if (!availablePartIds.isEmpty()) {
                this.partDao.deletePerId(availablePartIds);
            }
        }

        for (Integer partId : partIds) {
            List<Integer> episodePartIds = this.dataStructureDao.getEpisodeJoin(partId);
            List<Integer> availableEpisodeIds = this.episodeDao.getEpisodeIds(partId);
            availableEpisodeIds.removeAll(episodePartIds);

            if (!availableEpisodeIds.isEmpty()) {
                try {
                    Utils.doPartitioned(availableEpisodeIds, ids -> {
                        this.episodeDao.deletePerId(ids);
                        return true;
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private <E> E getOr(E value, @SuppressWarnings("SameParameterValue") E defaultValue) {
        return value == null ? defaultValue : value;
    }

    private class RoomDependantGenerator implements DependantGenerator {

        private final LoadData loadedData;

        private RoomDependantGenerator(LoadData loadedData) {
            this.loadedData = loadedData;
        }

        @Override
        public Collection<DependencyTask<?>> generateReadEpisodesDependant(LoadWorkGenerator.FilteredReadEpisodes readEpisodes) {
            Set<DependencyTask<?>> tasks = new HashSet<>();
            LoadWorker worker = LoadWorker.getWorker();

            for (LoadWorkGenerator.IntDependency<ClientReadEpisode> dependency : readEpisodes.dependencies) {
                tasks.add(new DependencyTask<>(
                        dependency.id,
                        new DependantValue(dependency.dependency),
                        worker.EPISODE_LOADER
                ));
            }

            return tasks;
        }

        @Override
        public Collection<DependencyTask<?>> generatePartsDependant(LoadWorkGenerator.FilteredParts parts) {
            Set<DependencyTask<?>> tasks = new HashSet<>();

            LoadWorker worker = LoadWorker.getWorker();
            for (LoadWorkGenerator.IntDependency<ClientPart> dependency : parts.mediumDependencies) {
                tasks.add(new DependencyTask<>(
                        dependency.id,
                        new DependantValue(
                                dependency.dependency,
                                dependency.dependency.getId(),
                                worker.PART_LOADER
                        ),
                        worker.MEDIUM_LOADER
                ));
            }
            return tasks;
        }

        @Override
        public Collection<DependencyTask<?>> generateEpisodesDependant(LoadWorkGenerator.FilteredEpisodes episodes) {
            Set<DependencyTask<?>> tasks = new HashSet<>();
            LoadWorker worker = LoadWorker.getWorker();

            for (LoadWorkGenerator.IntDependency<ClientEpisode> dependency : episodes.partDependencies) {
                tasks.add(new DependencyTask<>(
                        dependency.id,
                        new DependantValue(
                                dependency.dependency,
                                dependency.dependency.getId(),
                                worker.EPISODE_LOADER
                        ),
                        worker.PART_LOADER
                ));
            }
            return tasks;
        }

        @Override
        public Collection<DependencyTask<?>> generateMediaDependant(LoadWorkGenerator.FilteredMedia media) {
            Set<DependencyTask<?>> tasks = new HashSet<>();

            LoadWorker worker = LoadWorker.getWorker();
            for (LoadWorkGenerator.IntDependency<ClientMedium> dependency : media.episodeDependencies) {
                tasks.add(new DependencyTask<>(
                        dependency.id,
                        new DependantValue(
                                dependency.dependency,
                                dependency.dependency.getId(),
                                worker.MEDIUM_LOADER
                        ),
                        worker.EPISODE_LOADER
                ));
            }
            for (Integer unloadedPart : media.unloadedParts) {
                tasks.add(new DependencyTask<>(unloadedPart, null, worker.PART_LOADER));
            }
            return tasks;
        }

        @Override
        public Collection<DependencyTask<?>> generateMediaListsDependant(LoadWorkGenerator.FilteredMediaList mediaLists) {
            Set<DependencyTask<?>> tasks = new HashSet<>();

            LoadWorker worker = LoadWorker.getWorker();
            RoomConverter converter = new RoomConverter(this.loadedData);

            for (LoadWorkGenerator.IntDependency<List<LoadWorkGenerator.ListJoin>> dependency : mediaLists.mediumDependencies) {
                int tmpListId = 0;
                if (!dependency.dependency.isEmpty()) {
                    tmpListId = dependency.dependency.get(0).listId;
                }
                int listId = tmpListId;

                tasks.add(new DependencyTask<>(
                        dependency.id,
                        new DependantValue(
                                converter.convertListJoin(dependency.dependency),
                                () -> RoomStorage.this.mediaListDao.clearJoin(listId)
                        ),
                        worker.MEDIUM_LOADER
                ));
            }
            return tasks;
        }

        @Override
        public Collection<DependencyTask<?>> generateExternalMediaListsDependant(LoadWorkGenerator.FilteredExtMediaList externalMediaLists) {
            Set<DependencyTask<?>> tasks = new HashSet<>();

            LoadWorker worker = LoadWorker.getWorker();
            RoomConverter converter = new RoomConverter(this.loadedData);

            for (LoadWorkGenerator.IntDependency<List<LoadWorkGenerator.ListJoin>> dependency : externalMediaLists.mediumDependencies) {
                int tmpListId = 0;
                if (!dependency.dependency.isEmpty()) {
                    tmpListId = dependency.dependency.get(0).listId;
                }
                int listId = tmpListId;

                tasks.add(new DependencyTask<>(
                        dependency.id,
                        new DependantValue(
                                converter.convertExListJoin(dependency.dependency),
                                () -> RoomStorage.this.externalMediaListDao.clearJoin(listId)
                        ),
                        worker.MEDIUM_LOADER
                ));
            }
            for (LoadWorkGenerator.Dependency<String, ClientExternalMediaList> dependency : externalMediaLists.userDependencies) {
                tasks.add(new DependencyTask<>(
                        dependency.id,
                        new DependantValue(
                                converter.convert(dependency.dependency),
                                dependency.dependency.getId(),
                                worker.EXTERNAL_MEDIALIST_LOADER
                        ),
                        worker.EXTERNAL_USER_LOADER
                ));
            }
            return tasks;
        }

        @Override
        public Collection<DependencyTask<?>> generateExternalUsersDependant(LoadWorkGenerator.FilteredExternalUser externalUsers) {
            Set<DependencyTask<?>> tasks = new HashSet<>();

            LoadWorker worker = LoadWorker.getWorker();
            RoomConverter converter = new RoomConverter(this.loadedData);

            for (LoadWorkGenerator.IntDependency<List<LoadWorkGenerator.ListJoin>> dependency : externalUsers.mediumDependencies) {
                int tmpListId = 0;
                if (!dependency.dependency.isEmpty()) {
                    tmpListId = dependency.dependency.get(0).listId;
                }
                int listId = tmpListId;

                tasks.add(new DependencyTask<>(
                        dependency.id,
                        new DependantValue(
                                converter.convertExListJoin(dependency.dependency),
                                () -> RoomStorage.this.externalMediaListDao.clearJoin(listId)
                        ),
                        worker.MEDIUM_LOADER
                ));
            }
            return tasks;
        }
    }


    private class RoomModelPersister implements ClientModelPersister {
        private final Collection<ClientConsumer<?>> consumer = new ArrayList<>();
        private final LoadData loadedData;
        private final Repository repository;
        private final LoadWorkGenerator generator;


        RoomModelPersister(LoadData loadedData, Repository repository) {
            this.loadedData = loadedData;
            this.repository = repository;
            this.generator = new LoadWorkGenerator(loadedData);
            this.initConsumer();
        }

        private void initConsumer() {
            consumer.add(new ClientConsumer<ClientReadEpisode>() {
                @Override
                public Class<ClientReadEpisode> getType() {
                    return ClientReadEpisode.class;
                }

                @Override
                public void consume(Collection<ClientReadEpisode> clientEpisodes) {
                    RoomModelPersister.this.persistReadEpisodes(clientEpisodes);
                }
            });
            consumer.add(new ClientConsumer<ClientEpisode>() {
                @Override
                public Class<ClientEpisode> getType() {
                    return ClientEpisode.class;
                }

                @Override
                public void consume(Collection<ClientEpisode> clientEpisodes) {
                    RoomModelPersister.this.persistEpisodes(clientEpisodes);
                }
            });
            consumer.add(new ClientConsumer<ClientPart>() {
                @Override
                public Class<ClientPart> getType() {
                    return ClientPart.class;
                }

                @Override
                public void consume(Collection<ClientPart> parts) {
                    RoomModelPersister.this.persistParts(parts);
                }
            });
            consumer.add(new ClientConsumer<ClientMedium>() {
                @Override
                public Class<ClientMedium> getType() {
                    return ClientMedium.class;
                }

                @Override
                public void consume(Collection<ClientMedium> media) {
                    RoomModelPersister.this.persistMedia(media);
                }
            });
            consumer.add(new ClientConsumer<RoomMediaList.MediaListMediaJoin>() {
                @Override
                public Class<RoomMediaList.MediaListMediaJoin> getType() {
                    return RoomMediaList.MediaListMediaJoin.class;
                }

                @Override
                public void consume(Collection<RoomMediaList.MediaListMediaJoin> joins) {
                    RoomStorage.this.mediaListDao.addJoin(joins);
                }
            });
            consumer.add(new ClientConsumer<RoomExternalMediaList.ExternalListMediaJoin>() {
                @Override
                public Class<RoomExternalMediaList.ExternalListMediaJoin> getType() {
                    return RoomExternalMediaList.ExternalListMediaJoin.class;
                }

                @Override
                public void consume(Collection<RoomExternalMediaList.ExternalListMediaJoin> joins) {
                    RoomStorage.this.externalMediaListDao.addJoin(joins);
                }
            });
            consumer.add(new ClientConsumer<ClientExternalMediaList>() {
                @Override
                public Class<ClientExternalMediaList> getType() {
                    return ClientExternalMediaList.class;
                }

                @Override
                public void consume(Collection<ClientExternalMediaList> extLists) {
                    RoomModelPersister.this.persistExternalMediaLists(extLists);
                }
            });
            consumer.add(new ClientConsumer<ClientMediaList>() {
                @Override
                public Class<ClientMediaList> getType() {
                    return ClientMediaList.class;
                }

                @Override
                public void consume(Collection<ClientMediaList> lists) {
                    RoomModelPersister.this.persistMediaLists(lists);
                }
            });
            consumer.add(new ClientConsumer<ClientExternalUser>() {
                @Override
                public Class<ClientExternalUser> getType() {
                    return ClientExternalUser.class;
                }

                @Override
                public void consume(Collection<ClientExternalUser> extUsers) {
                    RoomModelPersister.this.persistExternalUsers(extUsers);
                }
            });
        }

        @Override
        public Collection<com.mytlogos.enterprise.background.ClientConsumer<?>> getConsumer() {
            return consumer;
        }

        @Override
        public ClientModelPersister persistEpisodes(Collection<ClientEpisode> episodes) {
            LoadWorkGenerator.FilteredEpisodes filteredEpisodes = this.generator.filterEpisodes(episodes);

            LoadWorker worker = this.repository.getLoadWorker();

            for (LoadWorkGenerator.IntDependency<ClientEpisode> dependency : filteredEpisodes.partDependencies) {
                worker.addIntegerIdTask(
                        dependency.id,
                        new DependantValue(
                                dependency.dependency,
                                dependency.dependency.getId(),
                                worker.EPISODE_LOADER
                        ),
                        worker.PART_LOADER
                );
            }

            return persist(filteredEpisodes);
        }

        @Override
        public ClientModelPersister persist(LoadWorkGenerator.FilteredEpisodes filteredEpisodes) {
            RoomConverter converter = new RoomConverter(this.loadedData);

            List<RoomEpisode> list = converter.convertEpisodes(filteredEpisodes.newEpisodes);
            List<RoomEpisode> update = converter.convertEpisodes(filteredEpisodes.updateEpisodes);

            List<RoomRelease> roomReleases = converter.convertReleases(filteredEpisodes.releases);

            RoomStorage.this.episodeDao.insertBulk(list);
            RoomStorage.this.episodeDao.updateBulk(update);
            RoomStorage.this.episodeDao.insertBulkRelease(roomReleases);

            for (RoomEpisode episode : list) {
                this.loadedData.getEpisodes().add(episode.getEpisodeId());
            }
            return this;
        }

        @Override
        public ClientModelPersister persistMediaLists(Collection<ClientMediaList> mediaLists) {
            LoadWorkGenerator.FilteredMediaList filteredMediaList = this.generator.filterMediaLists(mediaLists);
            RoomConverter converter = new RoomConverter(this.loadedData);

            LoadWorker worker = this.repository.getLoadWorker();

            for (LoadWorkGenerator.IntDependency<List<LoadWorkGenerator.ListJoin>> dependency : filteredMediaList.mediumDependencies) {
                int tmpListId = 0;
                if (!dependency.dependency.isEmpty()) {
                    tmpListId = dependency.dependency.get(0).listId;
                }
                int listId = tmpListId;
                worker.addIntegerIdTask(
                        dependency.id,
                        new DependantValue(
                                converter.convertListJoin(dependency.dependency),
                                () -> RoomStorage.this.mediaListDao.clearJoin(listId)
                        ),
                        worker.MEDIUM_LOADER
                );
            }

            return this.persist(filteredMediaList, converter);
        }

        @Override
        public ClientModelPersister persist(LoadWorkGenerator.FilteredMediaList filteredMediaList) {
            return this.persist(filteredMediaList, new RoomConverter(this.loadedData));
        }


        private ClientModelPersister persist(LoadWorkGenerator.FilteredMediaList filteredMediaList, RoomConverter converter) {
            List<RoomMediaList> list = converter.convertMediaList(filteredMediaList.newList);
            List<RoomMediaList> update = converter.convertMediaList(filteredMediaList.updateList);
            List<RoomMediaList.MediaListMediaJoin> joins = converter.convertListJoin(filteredMediaList.joins);
            List<Integer> clearListJoin = filteredMediaList.clearJoins;

            RoomStorage.this.mediaListDao.insertBulk(list);
            RoomStorage.this.mediaListDao.updateBulk(update);
            // first clear all possible out-of-date joins
            RoomStorage.this.mediaListDao.clearJoins(clearListJoin);
            // then add all up-to-date joins
            RoomStorage.this.mediaListDao.addJoin(joins);

            for (RoomMediaList mediaList : list) {
                this.loadedData.getMediaList().add(mediaList.listId);
            }
            return this;
        }

        @Override
        public ClientModelPersister persistExternalMediaLists(Collection<ClientExternalMediaList> externalMediaLists) {
            LoadWorkGenerator.FilteredExtMediaList filteredExtMediaList = this.generator.filterExternalMediaLists(externalMediaLists);
            RoomConverter converter = new RoomConverter(this.loadedData);

            LoadWorker worker = this.repository.getLoadWorker();

            for (LoadWorkGenerator.Dependency<String, ClientExternalMediaList> dependency : filteredExtMediaList.userDependencies) {
                worker.addStringIdTask(
                        dependency.id,
                        new DependantValue(
                                converter.convert(dependency.dependency),
                                dependency.dependency.getId(),
                                worker.EXTERNAL_MEDIALIST_LOADER
                        ),
                        worker.EXTERNAL_USER_LOADER
                );
            }
            for (LoadWorkGenerator.IntDependency<List<LoadWorkGenerator.ListJoin>> dependency : filteredExtMediaList.mediumDependencies) {
                int tmpListId = 0;
                if (!dependency.dependency.isEmpty()) {
                    tmpListId = dependency.dependency.get(0).listId;
                }
                int listId = tmpListId;
                worker.addIntegerIdTask(
                        dependency.id,
                        new DependantValue(
                                converter.convertExListJoin(dependency.dependency),
                                () -> RoomStorage.this.externalMediaListDao.clearJoin(listId)
                        ),
                        worker.MEDIUM_LOADER
                );
            }

            return this.persist(filteredExtMediaList, converter);
        }

        @Override
        public ClientModelPersister persist(LoadWorkGenerator.FilteredExtMediaList filteredExtMediaList) {
            return this.persist(filteredExtMediaList, new RoomConverter(this.loadedData));
        }

        private ClientModelPersister persist(LoadWorkGenerator.FilteredExtMediaList filteredExtMediaList, RoomConverter converter) {
            List<RoomExternalMediaList> list = converter.convertExternalMediaList(filteredExtMediaList.newList);
            List<RoomExternalMediaList> update = converter.convertExternalMediaList(filteredExtMediaList.updateList);

            List<RoomExternalMediaList.ExternalListMediaJoin> joins = converter.convertExListJoin(filteredExtMediaList.joins);
            List<Integer> clearListJoin = filteredExtMediaList.clearJoins;

            RoomStorage.this.externalMediaListDao.insertBulk(list);
            RoomStorage.this.externalMediaListDao.updateBulk(update);
            // first clear all possible out-of-date joins
            RoomStorage.this.externalMediaListDao.clearJoins(clearListJoin);
            // then add all up-to-date joins
            RoomStorage.this.externalMediaListDao.addJoin(joins);

            for (RoomExternalMediaList mediaList : list) {
                this.loadedData.getExternalMediaList().add(mediaList.externalListId);
            }
            return this;
        }

        @Override
        public ClientModelPersister persistExternalUsers(Collection<ClientExternalUser> externalUsers) {
            LoadWorkGenerator.FilteredExternalUser filteredExternalUser = this.generator.filterExternalUsers(externalUsers);

            LoadWorker worker = this.repository.getLoadWorker();
            RoomConverter converter = new RoomConverter(this.loadedData);

            for (LoadWorkGenerator.IntDependency<List<LoadWorkGenerator.ListJoin>> dependency : filteredExternalUser.mediumDependencies) {
                int tmpListId = 0;
                if (!dependency.dependency.isEmpty()) {
                    tmpListId = dependency.dependency.get(0).listId;
                }
                int listId = tmpListId;
                worker.addIntegerIdTask(
                        dependency.id,
                        new DependantValue(
                                converter.convertExListJoin(dependency.dependency),
                                () -> RoomStorage.this.externalMediaListDao.clearJoin(listId)
                        ),
                        worker.MEDIUM_LOADER
                );
            }

            return this.persist(filteredExternalUser);
        }

        @Override
        public ClientModelPersister persist(LoadWorkGenerator.FilteredExternalUser filteredExternalUser) {
            RoomConverter converter = new RoomConverter(this.loadedData);
            return this.persist(filteredExternalUser, converter);
        }

        private ClientModelPersister persist(LoadWorkGenerator.FilteredExternalUser filteredExternalUser, RoomConverter converter) {
            List<RoomExternalUser> list = converter.convertExternalUser(filteredExternalUser.newUser);
            List<RoomExternalUser> update = converter.convertExternalUser(filteredExternalUser.updateUser);

            List<RoomExternalMediaList> externalMediaLists = converter.convertExternalMediaList(filteredExternalUser.newList);
            List<RoomExternalMediaList> updateExternalMediaLists = converter.convertExternalMediaList(filteredExternalUser.updateList);

            List<RoomExternalMediaList.ExternalListMediaJoin> extListMediaJoin = converter.convertExListJoin(filteredExternalUser.joins);
            List<Integer> clearListJoin = filteredExternalUser.clearJoins;

            RoomStorage.this.externalUserDao.insertBulk(list);
            RoomStorage.this.externalUserDao.updateBulk(update);
            RoomStorage.this.externalMediaListDao.insertBulk(externalMediaLists);
            RoomStorage.this.externalMediaListDao.updateBulk(updateExternalMediaLists);
            // first clear all possible out-of-date joins
            RoomStorage.this.externalMediaListDao.clearJoins(clearListJoin);
            // then add all up-to-date joins
            RoomStorage.this.externalMediaListDao.addJoin(extListMediaJoin);

            for (RoomExternalUser user : list) {
                this.loadedData.getExternalUser().add(user.uuid);
            }

            for (RoomExternalMediaList mediaList : externalMediaLists) {
                this.loadedData.getExternalMediaList().add(mediaList.externalListId);
            }
            return this;
        }

        @Override
        public ClientModelPersister persistMedia(Collection<ClientMedium> media) {
            LoadWorkGenerator.FilteredMedia filteredMedia = this.generator.filterMedia(media);
            LoadWorker worker = this.repository.getLoadWorker();

            for (LoadWorkGenerator.IntDependency<ClientMedium> dependency : filteredMedia.episodeDependencies) {
                worker.addIntegerIdTask(
                        dependency.id,
                        new DependantValue(
                                dependency.dependency,
                                dependency.dependency.getId(),
                                worker.MEDIUM_LOADER
                        ),
                        worker.EPISODE_LOADER
                );
            }
            for (Integer part : filteredMedia.unloadedParts) {
                worker.addIntegerIdTask(part, null, worker.PART_LOADER);
            }
            return persist(filteredMedia);
        }

        @Override
        public ClientModelPersister persist(LoadWorkGenerator.FilteredMedia filteredMedia) {
            RoomConverter converter = new RoomConverter(this.loadedData);

            List<RoomMedium> list = converter.convertMedia(filteredMedia.newMedia);
            List<RoomMedium> update = converter.convertMedia(filteredMedia.updateMedia);

            RoomStorage.this.mediumDao.insertBulk(list);
            RoomStorage.this.mediumDao.updateBulk(update);

            for (RoomMedium medium : list) {
                this.loadedData.getMedia().add(medium.getMediumId());
            }
            LoadWorker worker = this.repository.getLoadWorker();

            for (ClientMedium medium : filteredMedia.updateMedia) {
                RoomStorage.this.dataStructureDao.clearPartJoin(medium.getId());
                List<RoomMediumPart> mediumParts = new ArrayList<>(medium.getParts().length);
                worker.enforceMediumStructure(medium.getId());

                for (int part : medium.getParts()) {
                    mediumParts.add(new RoomMediumPart(medium.getId(), part));
                }
                RoomStorage.this.dataStructureDao.addPartJoin(mediumParts);
            }
            return this;
        }

        @Override
        public ClientModelPersister persistNews(Collection<ClientNews> news) {
            List<RoomNews> list = new ArrayList<>();
            List<RoomNews> update = new ArrayList<>();
            RoomConverter converter = new RoomConverter();

            for (ClientNews clientNews : news) {
                RoomNews roomNews = converter.convert(clientNews);
                if (this.generator.isNewsLoaded(clientNews.getId())) {
                    update.add(roomNews);
                } else {
                    list.add(roomNews);
                }
            }
            RoomStorage.this.newsDao.insertNews(list);
            RoomStorage.this.newsDao.updateNews(update);

            for (RoomNews roomNews : list) {
                this.loadedData.getNews().add(roomNews.getNewsId());
            }
            System.out.println("from " + news.size() + " persisted: " + list);
            return this;
        }

        @Override
        public ClientModelPersister persistParts(Collection<ClientPart> parts) {
            LoadWorkGenerator.FilteredParts filteredParts = this.generator.filterParts(parts);
            LoadWorker worker = this.repository.getLoadWorker();

            for (LoadWorkGenerator.IntDependency<ClientPart> dependency : filteredParts.mediumDependencies) {
                worker.addIntegerIdTask(
                        dependency.id,
                        new DependantValue(
                                dependency.dependency,
                                dependency.dependency.getId(),
                                worker.PART_LOADER
                        ),
                        worker.MEDIUM_LOADER
                );
            }
            return persist(filteredParts);
        }

        @Override
        public ClientModelPersister persist(LoadWorkGenerator.FilteredParts filteredParts) {
            List<ClientEpisode> episodes = filteredParts.episodes;
            RoomConverter converter = new RoomConverter();

            List<RoomPart> list = converter.convertParts(filteredParts.newParts);
            List<RoomPart> update = converter.convertParts(filteredParts.updateParts);

            RoomStorage.this.partDao.insertBulk(list);
            RoomStorage.this.partDao.updateBulk(update);

            for (RoomPart part : list) {
                this.loadedData.getPart().add(part.getPartId());
            }
            LoadWorker worker = this.repository.getLoadWorker();

            for (ClientPart part : filteredParts.updateParts) {
                RoomStorage.this.dataStructureDao.clearEpisodeJoin(part.getId());
                List<RoomPartEpisode> partEpisodes = new ArrayList<>(part.getEpisodes().length);
                worker.enforcePartStructure(part.getId());

                for (ClientEpisode episode : part.getEpisodes()) {
                    partEpisodes.add(new RoomPartEpisode(part.getId(), episode.getId()));
                }
                RoomStorage.this.dataStructureDao.addEpisodeJoin(partEpisodes);
            }
            this.persistEpisodes(episodes);
            return this;
        }

        @Override
        public ClientModelPersister persistReadEpisodes(Collection<ClientReadEpisode> readEpisodes) {
            LoadWorkGenerator.FilteredReadEpisodes filteredReadEpisodes = this.generator.filterReadEpisodes(readEpisodes);
            LoadWorker worker = this.repository.getLoadWorker();

            for (LoadWorkGenerator.IntDependency dependency : filteredReadEpisodes.dependencies) {
                worker.addIntegerIdTask(
                        dependency.id,
                        new DependantValue(dependency.dependency),
                        worker.EPISODE_LOADER
                );
            }
            return this.persist(filteredReadEpisodes);
        }

        @Override
        public ClientModelPersister persist(LoadWorkGenerator.FilteredReadEpisodes filteredReadEpisodes) {
            for (ClientReadEpisode readEpisode : filteredReadEpisodes.episodeList) {
                RoomStorage.this.episodeDao.updateProgress(
                        readEpisode.getEpisodeId(),
                        readEpisode.getProgress(),
                        readEpisode.getReadDate()
                );
            }
            return this;
        }

        @Override
        public ClientModelPersister persist(ClientListQuery query) {
            this.persist(query.getMedia());
            this.persist(query.getList());
            return this;
        }

        @Override
        public ClientModelPersister persist(ClientMultiListQuery query) {
            this.persist(query.getMedia());
            this.persist(query.getList());
            return this;
        }

        @Override
        public ClientModelPersister persistToDownloads(Collection<ToDownload> toDownloads) {
            List<RoomToDownload> roomToDownloads = new RoomConverter().convertToDownload(toDownloads);
            RoomStorage.this.toDownloadDao.insertBulk(roomToDownloads);
            return this;
        }

        @Override
        public ClientModelPersister persist(ClientUpdateUser user) {
            User value = RoomStorage.this.userLiveData.getValue();
            if (value == null) {
                throw new IllegalArgumentException("cannot update user if none is stored in the database");
            }
            if (!user.getUuid().equals(value.getUuid())) {
                throw new IllegalArgumentException("cannot update user which do not share the same uuid");
            }
            // at the moment the only thing that can change for the user on client side is the name
            if (user.getName().equals(value.getName())) {
                return this;
            }
            RoomStorage.this.userDao.update(new RoomUser(user.getName(), value.getUuid(), value.getSession()));
            return this;
        }

        @Override
        public ClientModelPersister persist(ToDownload toDownload) {
            RoomStorage.this.toDownloadDao.insert(new RoomConverter().convert(toDownload));
            return this;
        }

        @Override
        public void persistMediaInWait(List<ClientMediumInWait> medium) {
            RoomStorage.this.mediumInWaitDao.insertBulk(new RoomConverter().convertClientMediaInWait(medium));
        }

        @Override
        public ClientModelPersister persist(ClientSimpleUser user) {
            // short cut version
            if (user == null) {
                RoomStorage.this.deleteAllUser();
                return this;
            }
            RoomConverter converter = new RoomConverter();
            RoomUser newRoomUser = converter.convert(user);

            RoomUser currentUser = RoomStorage.this.userDao.getUserNow();

            if (currentUser != null && newRoomUser.getUuid().equals(currentUser.getUuid())) {
                // update user, so previous one wont be deleted
                RoomStorage.this.userDao.update(newRoomUser);
            } else {
                RoomStorage.this.userDao.deleteAllUser();
                // persist user
                RoomStorage.this.userDao.insert(newRoomUser);
            }

            return this;
        }

        @Override
        public ClientModelPersister persist(ClientUser clientUser) {
            // short cut version
            if (clientUser == null) {
                RoomStorage.this.deleteAllUser();
                return this;
            }

            RoomConverter converter = new RoomConverter();
            RoomUser newRoomUser = converter.convert(clientUser);

            RoomUser currentUser = RoomStorage.this.userDao.getUserNow();

            if (currentUser != null && newRoomUser.getUuid().equals(currentUser.getUuid())) {
                // update user, so previous one wont be deleted
                RoomStorage.this.userDao.update(newRoomUser);
            } else {
                RoomStorage.this.userDao.deleteAllUser();
                // persist user
                RoomStorage.this.userDao.insert(newRoomUser);
            }

            LoadWorker worker = this.repository.getLoadWorker();

            for (int clientReadChapter : clientUser.getUnreadChapter()) {
                if (!this.generator.isEpisodeLoaded(clientReadChapter)) {
                    worker.addIntegerIdTask(clientReadChapter, null, worker.EPISODE_LOADER);
                }
            }

            for (ClientNews clientNews : clientUser.getUnreadNews()) {
                int id = clientNews.getId();

                if (!this.generator.isNewsLoaded(id)) {
                    worker.addIntegerIdTask(id, null, worker.NEWS_LOADER);
                }
            }

            for (ClientReadEpisode clientReadEpisode : clientUser.getReadToday()) {
                int episodeId = clientReadEpisode.getEpisodeId();

                if (!this.generator.isEpisodeLoaded(episodeId)) {
                    worker.addIntegerIdTask(episodeId, null, worker.EPISODE_LOADER);
                }
            }

            // persist lists
            this.persist(clientUser.getLists());
            // persist externalUser
            this.persist(clientUser.getExternalUser());
            // persist loaded unread News
            this.persist(clientUser.getUnreadNews());
            // persist/update media with data
            this.persist(clientUser.getReadToday());

            return this;
        }

        @Override
        public void finish() {
            this.repository.getLoadWorker().work();
        }
    }
}
