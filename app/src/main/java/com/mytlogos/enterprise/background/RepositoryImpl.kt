package com.mytlogos.enterprise.background;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.paging.PagedList;
import androidx.work.Worker;

import com.mytlogos.enterprise.background.api.AndroidNetworkIdentificator;
import com.mytlogos.enterprise.background.api.Client;
import com.mytlogos.enterprise.background.api.NetworkIdentificator;
import com.mytlogos.enterprise.background.api.model.ClientDownloadedEpisode;
import com.mytlogos.enterprise.background.api.model.ClientEpisode;
import com.mytlogos.enterprise.background.api.model.ClientExternalMediaList;
import com.mytlogos.enterprise.background.api.model.ClientExternalUser;
import com.mytlogos.enterprise.background.api.model.ClientMediaList;
import com.mytlogos.enterprise.background.api.model.ClientMedium;
import com.mytlogos.enterprise.background.api.model.ClientMediumInWait;
import com.mytlogos.enterprise.background.api.model.ClientMinList;
import com.mytlogos.enterprise.background.api.model.ClientMultiListQuery;
import com.mytlogos.enterprise.background.api.model.ClientNews;
import com.mytlogos.enterprise.background.api.model.ClientPart;
import com.mytlogos.enterprise.background.api.model.ClientSimpleMedium;
import com.mytlogos.enterprise.background.api.model.ClientSimpleUser;
import com.mytlogos.enterprise.background.api.model.ClientStat;
import com.mytlogos.enterprise.background.api.model.ClientUser;
import com.mytlogos.enterprise.background.api.model.InvalidatedData;
import com.mytlogos.enterprise.background.resourceLoader.BlockingLoadWorker;
import com.mytlogos.enterprise.background.resourceLoader.LoadWorkGenerator;
import com.mytlogos.enterprise.background.resourceLoader.LoadWorker;
import com.mytlogos.enterprise.background.room.RoomStorage;
import com.mytlogos.enterprise.model.DisplayEpisode;
import com.mytlogos.enterprise.model.DisplayRelease;
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
import com.mytlogos.enterprise.model.UpdateUser;
import com.mytlogos.enterprise.model.User;
import com.mytlogos.enterprise.preferences.UserPreferences;
import com.mytlogos.enterprise.tools.ContentTool;
import com.mytlogos.enterprise.tools.FileTools;
import com.mytlogos.enterprise.tools.Sortings;
import com.mytlogos.enterprise.tools.Utils;
import com.mytlogos.enterprise.viewmodel.EpisodeViewModel;
import com.mytlogos.enterprise.worker.DownloadWorker;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import okhttp3.ResponseBody;
import retrofit2.Response;

public class RepositoryImpl implements Repository {
    private static RepositoryImpl INSTANCE;
    private final ClientModelPersister persister;
    private final LiveData<User> storageUserLiveData;
    private final Client client;
    private final DatabaseStorage storage;
    private final LoadData loadedData;
    private final LoadWorker loadWorker;
    private final EditService editService;

    private RepositoryImpl(Application application) {
        this.storage = new RoomStorage(application);
        this.loadedData = new LoadData();
        this.storageUserLiveData = Transformations.map(this.storage.getUser(), value -> {
            if (value == null) {
                INSTANCE.client.clearAuthentication();
            } else {
                INSTANCE.client.setAuthentication(value.getUuid(), value.getSession());
            }
            return value;
        });
        this.persister = this.storage.getPersister(this, this.loadedData);
        NetworkIdentificator identificator = new AndroidNetworkIdentificator(application.getApplicationContext());
        this.client = new Client(identificator);

        DependantGenerator dependantGenerator = this.storage.getDependantGenerator(this.loadedData);
        this.loadWorker = new BlockingLoadWorker(
                this.loadedData,
                this,
                this.persister,
                dependantGenerator
        );
        this.editService = new EditService(this.client, this.storage, this.persister);
    }

    /**
     * Return the Repository Singleton Instance.
     *
     * @return returns the singleton
     */
    public static Repository getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("Repository not yet initialized");
        }
        return INSTANCE;
    }

    public static Repository getInstance(Application application) {
        if (INSTANCE == null) {
            synchronized (RepositoryImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RepositoryImpl(application);
                    INSTANCE.storage.setLoading(true);
                    System.out.println("querying");

                    // storage.getHomeStats() does nothing, but storageHomeStatsLiveData invalidates instantly?
                    //  storageHomeStatsLiveData does nothing?
//                    new Handler(Looper.getMainLooper()).post(() -> );
                    // check first login
                    TaskManager.runTask(() -> {
                        try {
                            // ask the database what data it has, to check if it needs to be loaded from the server
                            INSTANCE.loadLoadedData();

                            Response<ClientSimpleUser> call = INSTANCE.client.checkLogin();

                            ClientSimpleUser clientUser = call.body();

                            if (clientUser != null) {
                                INSTANCE.client.setAuthentication(clientUser.getUuid(), clientUser.getSession());
                            }
                            INSTANCE.persister.persist(clientUser).finish();
                            Log.i(RepositoryImpl.class.getSimpleName(), "successful query");
                        } catch (IOException e) {
                            Log.e(RepositoryImpl.class.getSimpleName(), "failed query", e);
                        }
                    });
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public boolean isClientOnline() {
        return this.client.isOnline();
    }

    @Override
    public boolean isClientAuthenticated() {
        return this.client.isAuthenticated();
    }

    @Override
    public LoadWorker getLoadWorker() {
        return loadWorker;
    }

    private void loadLoadedData() {
        LoadData loadData = this.storage.getLoadData();

        this.loadedData.getMedia().addAll(loadData.getMedia());
        this.loadedData.getPart().addAll(loadData.getPart());
        this.loadedData.getEpisodes().addAll(loadData.getEpisodes());
        this.loadedData.getNews().addAll(loadData.getNews());
        this.loadedData.getMediaList().addAll(loadData.getMediaList());
        this.loadedData.getExternalUser().addAll(loadData.getExternalUser());
        this.loadedData.getExternalMediaList().addAll(loadData.getExternalMediaList());
    }

    @Override
    public LiveData<HomeStats> getHomeStats() {
        return this.storage.getHomeStats();
    }

    @Override
    public LiveData<User> getUser() {
        return this.storageUserLiveData;
    }

    @Override
    public void updateUser(UpdateUser updateUser) {
        this.editService.updateUser(updateUser);
    }

    @Override
    public void deleteAllUser() {
        TaskManager.runTask(storage::deleteAllUser);
    }

    /**
     * Synchronous Login.
     *
     * @param email    email or name of the user
     * @param password password of the user
     * @throws IOException if an connection problem arose
     */
    @Override
    public void login(String email, String password) throws IOException {
        Response<ClientUser> response = this.client.login(email, password);
        ClientUser user = response.body();

        if (user != null) {
            // set authentication in client before persisting user,
            // as it may load data which requires authentication
            INSTANCE.client.setAuthentication(user.getUuid(), user.getSession());
        }
        persister.persist(user);
    }

    /**
     * Synchronous Registration.
     *
     * @param email    email or name of the user
     * @param password password of the user
     */
    @Override
    public void register(String email, String password) throws IOException {
        Response<ClientUser> response = this.client.register(email, password);
        ClientUser user = response.body();

        if (user != null) {
            // set authentication in client before persisting user,
            // as it may load data which requires authentication
            INSTANCE.client.setAuthentication(user.getUuid(), user.getSession());
        }
        persister.persist(user).finish();
    }

    @Override
    public void logout() {
        TaskManager.runTask(() -> {
            try {
                Response<Boolean> response = this.client.logout();
                if (!response.isSuccessful()) {
                    System.out.println("Log out was not successful: " + response.message());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            storage.deleteAllUser();
        });
    }


    @Override
    public void loadAllMedia() {
        try {
            List<Integer> mediaIds = Utils.checkAndGetBody(this.client.getAllMedia());

            if (mediaIds == null) {
                return;
            }
            for (Integer mediumId : mediaIds) {
                if (this.loadedData.getMedia().contains(mediumId) || this.loadWorker.isMediumLoading(mediumId)) {
                    continue;
                }
                loadWorker.addIntegerIdTask(mediumId, null, loadWorker.MEDIUM_LOADER);
            }
            loadWorker.work();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CompletableFuture<List<ClientEpisode>> loadEpisodeAsync(Collection<Integer> episodeIds) {
        return CompletableFuture.supplyAsync(() -> loadEpisodeSync(episodeIds));
    }

    @Override
    public List<ClientEpisode> loadEpisodeSync(Collection<Integer> episodeIds) {
        try {
            System.out.println("loading episodes: " + episodeIds + " on " + Thread.currentThread());
            return this.client.getEpisodes(episodeIds).body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public CompletableFuture<List<ClientMedium>> loadMediaAsync(Collection<Integer> mediaIds) {
        return CompletableFuture.supplyAsync(() -> this.loadMediaSync(mediaIds));
    }

    @Override
    public List<ClientMedium> loadMediaSync(Collection<Integer> mediaIds) {
        try {
            System.out.println("loading media: " + mediaIds + " on " + Thread.currentThread());
            return this.client.getMedia(mediaIds).body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public CompletableFuture<List<ClientPart>> loadPartAsync(Collection<Integer> partIds) {
        return CompletableFuture.supplyAsync(() -> this.loadPartSync(partIds));
    }

    @Override
    public List<ClientPart> loadPartSync(Collection<Integer> partIds) {
        try {
            System.out.println("loading parts: " + partIds + " on " + Thread.currentThread());
            return this.client.getParts(partIds).body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public CompletableFuture<ClientMultiListQuery> loadMediaListAsync(Collection<Integer> listIds) {
        return CompletableFuture.supplyAsync(() -> this.loadMediaListSync(listIds));
    }

    @Override
    public ClientMultiListQuery loadMediaListSync(Collection<Integer> listIds) {
        try {
            System.out.println("loading lists: " + listIds + " on " + Thread.currentThread());
            return this.client.getLists(listIds).body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public CompletableFuture<List<ClientExternalMediaList>> loadExternalMediaListAsync(Collection<Integer> externalListIds) {
        return CompletableFuture.supplyAsync(() -> this.loadExternalMediaListSync(externalListIds));
    }

    @Override
    public List<ClientExternalMediaList> loadExternalMediaListSync(Collection<Integer> externalListIds) {
        System.out.println("loading ExtLists: " + externalListIds + " on " + Thread.currentThread());
//        try {
//                List<ClientEpisode> body = this.client.getExternalUser(episodeIds).execute().body();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        // todo implement loading of externalMediaLists
        return null;
    }

    @Override
    public CompletableFuture<List<ClientExternalUser>> loadExternalUserAsync(Collection<String> externalUuids) {
        return CompletableFuture.supplyAsync(() -> this.loadExternalUserSync(externalUuids));
    }

    @Override
    public List<ClientExternalUser> loadExternalUserSync(Collection<String> externalUuids) {
        try {
            System.out.println("loading ExternalUser: " + externalUuids + " on " + Thread.currentThread());
            return this.client.getExternalUser(externalUuids).body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public CompletableFuture<List<ClientNews>> loadNewsAsync(Collection<Integer> newsIds) {
        return CompletableFuture.supplyAsync(() -> this.loadNewsSync(newsIds));
    }

    @Override
    public List<ClientNews> loadNewsSync(Collection<Integer> newsIds) {
        try {
            System.out.println("loading News: " + newsIds + " on " + Thread.currentThread());
            return this.client.getNews(newsIds).body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public LiveData<PagedList<News>> getNews() {
        return this.storage.getNews();
    }

    @Override
    public void removeOldNews() {
        TaskManager.runTask(storage::deleteOldNews);
    }

    @Override
    public boolean isLoading() {
        return storage.isLoading();
    }

    @Override
    public void refreshNews(DateTime latest) throws IOException {
        List<ClientNews> news = Utils.checkAndGetBody(this.client.getNews(latest, null));

        if (news != null) {
            this.persister.persistNews(news);
        }
    }

    @Override
    public void loadInvalidated() throws IOException {
        List<InvalidatedData> invalidatedData = this.client.getInvalidated().body();

        if (invalidatedData == null || invalidatedData.isEmpty()) {
            return;
        }

        boolean userUpdated = false;
        LoadWorker loadWorker = this.getLoadWorker();

        for (InvalidatedData datum : invalidatedData) {
            if (datum.isUserUuid()) {
                userUpdated = true;

            } else if (datum.getEpisodeId() > 0) {
                loadWorker.addIntegerIdTask(datum.getEpisodeId(), null, loadWorker.EPISODE_LOADER);

            } else if (datum.getPartId() > 0) {
                loadWorker.addIntegerIdTask(datum.getPartId(), null, loadWorker.PART_LOADER);

            } else if (datum.getMediumId() > 0) {
                loadWorker.addIntegerIdTask(datum.getMediumId(), null, loadWorker.MEDIUM_LOADER);

            } else if (datum.getListId() > 0) {
                loadWorker.addIntegerIdTask(datum.getListId(), null, loadWorker.MEDIALIST_LOADER);

            } else if (datum.getExternalListId() > 0) {
                loadWorker.addIntegerIdTask(datum.getExternalListId(), null, loadWorker.EXTERNAL_MEDIALIST_LOADER);

            } else if (datum.getExternalUuid() != null && !datum.getExternalUuid().isEmpty()) {
                loadWorker.addStringIdTask(datum.getExternalUuid(), null, loadWorker.EXTERNAL_USER_LOADER);

            } else if (datum.getNewsId() > 0) {
                loadWorker.addIntegerIdTask(datum.getNewsId(), null, loadWorker.NEWS_LOADER);
            } else {
                System.out.println("unknown invalid data: " + datum);
            }
        }

        if (userUpdated) {
            ClientSimpleUser user = this.client.checkLogin().body();
            this.persister.persist(user);
        }
        loadWorker.work();
    }

    @Override
    public List<Integer> getSavedEpisodes() {
        return this.storage.getSavedEpisodes();
    }

    @Override
    public void updateSaved(int episodeId, boolean saved) {
        this.storage.updateSaved(episodeId, saved);
    }

    @Override
    public void updateSaved(Collection<Integer> episodeIds, boolean saved) {
        try {
            Utils.doPartitionedEx(episodeIds, ids -> {
                this.storage.updateSaved(ids, saved);
                return false;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Integer> getToDeleteEpisodes() {
        return this.storage.getToDeleteEpisodes();
    }

    @Override
    public List<ClientDownloadedEpisode> downloadEpisodes(Collection<Integer> episodeIds) throws IOException {
        return this.client.downloadEpisodes(episodeIds).body();
    }

    @Override
    public List<ToDownload> getToDownload() {
        return this.storage.getAllToDownloads();
    }

    @Override
    public void addToDownload(ToDownload toDownload) {
        this.persister.persist(toDownload).finish();
    }

    @Override
    public void removeToDownloads(Collection<ToDownload> toDownloads) {
        this.storage.removeToDownloads(toDownloads);
    }

    @Override
    public Collection<Integer> getExternalListItems(Integer externalListId) {
        return this.storage.getExternalListItems(externalListId);
    }

    @Override
    public Collection<Integer> getListItems(Integer listId) {
        return this.storage.getListItems(listId);
    }

    @Override
    public List<Integer> getDownloadableEpisodes(Collection<Integer> mediaIds) {
        return this.storage.getDownloadableEpisodes(mediaIds);
    }

    @Override
    public List<Integer> getDownloadableEpisodes(Integer mediumId, int limit) {
        return this.storage.getDownloadableEpisodes(mediumId, limit);
    }

    @Override
    public LiveData<PagedList<DisplayRelease>> getDisplayEpisodes(EpisodeViewModel.Filter filter) {
        return this.storage.getDisplayEpisodes(filter);
    }

    @Override
    public LiveData<PagedList<DisplayEpisode>> getDisplayEpisodesGrouped(int saved, int medium) {
        return this.storage.getDisplayEpisodesGrouped(saved, medium);
    }

    @Override
    public LiveData<List<MediaList>> getLists() {
        return this.storage.getLists();
    }

    @Override
    public LiveData<? extends MediaListSetting> getListSettings(int id, boolean isExternal) {
        return this.storage.getListSetting(id, isExternal);
    }

    @Override
    public CompletableFuture<String> updateListName(MediaListSetting listSetting, String newName) {
        return this.editService.updateListName(listSetting, newName);
    }

    @Override
    public CompletableFuture<String> updateListMedium(MediaListSetting listSetting, int newMediumType) {
        return this.editService.updateListMedium(listSetting, newMediumType);
    }

    @Override
    public void updateToDownload(boolean add, ToDownload toDownload) {
        this.storage.updateToDownload(add, toDownload);
    }

    @Override
    public LiveData<PagedList<MediumItem>> getAllMedia(Sortings sortings, String title, int medium, String author, DateTime lastUpdate, int minCountEpisodes, int minCountReadEpisodes) {
        return this.storage.getAllMedia(sortings, title, medium, author, lastUpdate, minCountEpisodes, minCountReadEpisodes);
    }

    @Override
    public LiveData<MediumSetting> getMediumSettings(int mediumId) {
        return this.storage.getMediumSettings(mediumId);
    }

    @Override
    public CompletableFuture<String> updateMedium(MediumSetting mediumSettings) {
        return this.editService.updateMedium(mediumSettings);
    }

    @Override
    public LiveData<PagedList<TocEpisode>> getToc(int mediumId, Sortings sortings, byte read, byte saved) {
        return this.storage.getToc(mediumId, sortings, read, saved);
    }

    @Override
    public LiveData<List<MediumItem>> getMediumItems(int listId, boolean isExternal) {
        return this.storage.getMediumItems(listId, isExternal);
    }

    @Override
    public void loadMediaInWaitSync() throws IOException {
        List<ClientMediumInWait> medium = Utils.checkAndGetBody(this.client.getMediumInWait());

        if (medium != null && !medium.isEmpty()) {
            this.storage.clearMediaInWait();
            this.persister.persistMediaInWait(medium);
        }
    }

    @Override
    public void addList(MediaList list, boolean autoDownload) throws IOException {
        User value = this.storageUserLiveData.getValue();

        if (value == null || value.getUuid().isEmpty()) {
            throw new IllegalStateException("user is not authenticated");
        }
        ClientMinList mediaList = new ClientMinList(
                list.getName(),
                list.getMedium()
        );
        ClientMediaList clientMediaList = this.client.addList(mediaList).body();

        if (clientMediaList == null) {
            throw new IllegalArgumentException("adding list failed");
        }

        this.persister.persist(clientMediaList);
        ToDownload toDownload = new ToDownload(
                false,
                null,
                clientMediaList.getId(),
                null
        );
        this.storage.updateToDownload(true, toDownload);
    }

    @Override
    public boolean listExists(String listName) {
        return this.storage.listExists(listName);
    }

    @Override
    public int countSavedUnreadEpisodes(Integer mediumId) {
        return this.storage.countSavedEpisodes(mediumId);
    }

    @Override
    public List<Integer> getSavedEpisodes(int mediumId) {
        return this.storage.getSavedEpisodes(mediumId);
    }

    @Override
    public Episode getEpisode(int episodeId) {
        return this.storage.getEpisode(episodeId);
    }

    @Override
    public List<SimpleEpisode> getSimpleEpisodes(Collection<Integer> ids) {
        return this.storage.getSimpleEpisodes(ids);
    }

    @Override
    public void addProgressListener(Consumer<Integer> consumer) {
        this.loadWorker.addProgressListener(consumer);
    }

    @Override
    public void removeProgressListener(Consumer<Integer> consumer) {
        this.loadWorker.removeProgressListener(consumer);
    }

    @Override
    public void addTotalWorkListener(Consumer<Integer> consumer) {
        this.loadWorker.addTotalWorkListener(consumer);
    }

    @Override
    public void removeTotalWorkListener(Consumer<Integer> consumer) {
        this.loadWorker.removeTotalWorkListener(consumer);
    }

    @Override
    public int getLoadWorkerProgress() {
        return this.loadWorker.getProgress();
    }

    @Override
    public int getLoadWorkerTotalWork() {
        return this.loadWorker.getTotalWork();
    }

    @Override
    public void syncProgress() {
        this.storage.syncProgress();
    }

    @Override
    public void updateDataStructure(List<Integer> mediaIds, List<Integer> partIds) {
        this.storage.updateDataStructure(mediaIds, partIds);
    }

    @Override
    public void reloadLowerIndex(double combiIndex, int mediumId) throws Exception {
        List<Integer> episodeIds = this.storage.getEpisodeIdsWithLowerIndex(combiIndex, mediumId);
        this.reloadEpisodes(episodeIds);
    }

    @Override
    public void reloadHigherIndex(double combiIndex, int mediumId) throws Exception {
        List<Integer> episodeIds = this.storage.getEpisodeIdsWithHigherIndex(combiIndex, mediumId);
        this.reloadEpisodes(episodeIds);
    }

    @Override
    public void reload(Set<Integer> episodeIds) throws Exception {
        this.reloadEpisodes(episodeIds);
    }

    @Override
    public void reloadAll(int mediumId) throws IOException {
        ClientMedium medium = this.client.getMedium(mediumId).body();

        if (medium == null) {
            System.err.println("missing medium: " + mediumId);
            return;
        }
        int[] parts = medium.getParts();
        Collection<Integer> partIds = new ArrayList<>(parts.length);

        for (int part : parts) {
            partIds.add(part);
        }
        List<ClientPart> partBody = this.client.getParts(partIds).body();

        if (partBody == null) {
            return;
        }
        List<Integer> loadedPartIds = new ArrayList<>();

        for (ClientPart part : partBody) {
            loadedPartIds.add(part.getId());
        }
        LoadWorkGenerator generator = new LoadWorkGenerator(this.loadedData);
        LoadWorkGenerator.FilteredParts filteredParts = generator.filterParts(partBody);
        this.persister.persist(filteredParts);

        partIds.removeAll(loadedPartIds);
        this.storage.removeParts(partIds);
    }

    private void reloadEpisodes(Collection<Integer> episodeIds) throws Exception {
        Utils.doPartitionedEx(episodeIds, integers -> {
            List<ClientEpisode> episodes = this.client.getEpisodes(integers).body();

            if (episodes == null) {
                return true;
            }
            LoadWorkGenerator generator = new LoadWorkGenerator(this.loadedData);
            LoadWorkGenerator.FilteredEpisodes filteredEpisodes = generator.filterEpisodes(episodes);
            this.persister.persist(filteredEpisodes);

            List<Integer> loadedIds = new ArrayList<>();

            for (ClientEpisode episode : episodes) {
                loadedIds.add(episode.getId());
            }

            integers.removeAll(loadedIds);
            this.storage.removeEpisodes(integers);
            return true;
        });
    }

    @Override
    public void downloadLowerIndex(double combiIndex, int mediumId, Context context) {
        List<Integer> episodeIds = this.storage.getEpisodeIdsWithLowerIndex(combiIndex, mediumId);
        DownloadWorker.enqueueDownloadTask(context, mediumId, episodeIds);
    }

    @Override
    public void downloadHigherIndex(double combiIndex, int mediumId, Context context) {
        List<Integer> episodeIds = this.storage.getEpisodeIdsWithHigherIndex(combiIndex, mediumId);
        DownloadWorker.enqueueDownloadTask(context, mediumId, episodeIds);
    }

    @Override
    public void download(Set<Integer> episodeIds, int mediumId, Context context) {
        DownloadWorker.enqueueDownloadTask(context, mediumId, episodeIds);
    }

    @Override
    public void downloadAll(int mediumId, Context context) {
        Collection<Integer> episodeIds = this.storage.getAllEpisodes(mediumId);
        DownloadWorker.enqueueDownloadTask(context, mediumId, episodeIds);
    }

    @Override
    public void updateProgress(int episodeId, float progress) {
        TaskManager.runTask(() -> this.storage.updateProgress(Collections.singleton(episodeId), progress));
    }

    @Override
    public Client getClient(Worker worker) {
        if (worker == null || worker.isStopped()) {
            throw new IllegalArgumentException("not an active Worker");
        }
        return this.client;
    }

    @Override
    public ClientModelPersister getPersister(Worker worker) {
        if (worker == null || worker.isStopped()) {
            throw new IllegalArgumentException("not an active Worker");
        }
        return this.persister;
    }

    @Override
    public boolean isMediumLoaded(int mediumId) {
        return this.loadedData.getMedia().contains(mediumId);
    }

    @Override
    public boolean isPartLoaded(int partId) {
        return this.loadedData.getPart().contains(partId);
    }

    @Override
    public boolean isEpisodeLoaded(int episodeId) {
        return this.loadedData.getEpisodes().contains(episodeId);
    }

    @Override
    public boolean isExternalUserLoaded(String uuid) {
        return this.loadedData.getExternalUser().contains(uuid);
    }

    @Override
    public ReloadStat checkReload(ClientStat.ParsedStat stat) {
        return this.storage.checkReload(stat);
    }

    @Override
    public void deleteLocalEpisodesWithLowerIndex(double combiIndex, int mediumId, Application application) throws IOException {
        Collection<Integer> episodeIds = this.storage.getSavedEpisodeIdsWithLowerIndex(combiIndex, mediumId);
        this.deleteLocalEpisodes(new HashSet<>(episodeIds), mediumId, application);
    }

    @Override
    public void deleteLocalEpisodesWithHigherIndex(double combiIndex, int mediumId, Application application) throws IOException {
        Collection<Integer> episodeIds = this.storage.getSavedEpisodeIdsWithHigherIndex(combiIndex, mediumId);
        this.deleteLocalEpisodes(new HashSet<>(episodeIds), mediumId, application);
    }

    @Override
    public void deleteAllLocalEpisodes(int mediumId, Application application) throws IOException {
        Collection<Integer> episodes = this.storage.getSavedEpisodes(mediumId);
        this.deleteLocalEpisodes(new HashSet<>(episodes), mediumId, application);
    }

    @Override
    public void deleteLocalEpisodes(Set<Integer> episodeIds, int mediumId, Application application) throws IOException {
        int medium = this.getMediumType(mediumId);

        ContentTool contentTool = FileTools.getContentTool(medium, application);

        if (!contentTool.isSupported()) {
            throw new IOException("medium type: " + medium + " is not supported");
        }

        contentTool.removeMediaEpisodes(mediumId, episodeIds);
        this.updateSaved(episodeIds, false);
    }

    @Override
    public void updateReadWithHigherIndex(double combiIndex, boolean read, int mediumId) throws Exception {
        List<Integer> episodeIds = this.storage.getEpisodeIdsWithHigherIndex(combiIndex, mediumId, read);
        this.updateRead(episodeIds, read);
    }

    @Override
    public void updateAllRead(int mediumId, boolean read) throws Exception {
        Collection<Integer> episodeIds = this.storage.getAllEpisodes(mediumId);
        this.updateRead(episodeIds, read);
    }

    @Override
    public void updateRead(int episodeId, boolean read) throws Exception {
        this.editService.updateRead(Collections.singletonList(episodeId), read);
    }

    @Override
    public void updateRead(Collection<Integer> episodeIds, boolean read) throws Exception {
        this.editService.updateRead(episodeIds, read);
    }

    @Override
    public LiveData<PagedList<ReadEpisode>> getReadTodayEpisodes() {
        return this.storage.getReadTodayEpisodes();
    }

    @Override
    public LiveData<PagedList<MediumInWait>> getMediaInWaitBy(String filter, int mediumFilter, String hostFilter, Sortings sortings) {
        return this.storage.getMediaInWaitBy(filter, mediumFilter, hostFilter, sortings);
    }

    @Override
    public LiveData<List<MediaList>> getInternLists() {
        return this.storage.getInternLists();
    }

    @Override
    public LiveData<List<MediumInWait>> getSimilarMediaInWait(MediumInWait mediumInWait) {
        return this.storage.getSimilarMediaInWait(mediumInWait);
    }

    @Override
    public LiveData<List<SimpleMedium>> getMediaSuggestions(String title, int medium) {
        return this.storage.getMediaSuggestions(title, medium);
    }

    @Override
    public LiveData<List<MediumInWait>> getMediaInWaitSuggestions(String title, int medium) {
        return this.storage.getMediaInWaitSuggestions(title, medium);
    }

    @Override
    public CompletableFuture<Boolean> consumeMediumInWait(SimpleMedium selectedMedium, List<MediumInWait> mediumInWaits) {
        return TaskManager.runCompletableTask(() -> {
            Collection<ClientMediumInWait> others = new HashSet<>();

            if (mediumInWaits != null) {
                for (MediumInWait inWait : mediumInWaits) {
                    others.add(new ClientMediumInWait(
                            inWait.getTitle(),
                            inWait.getMedium(),
                            inWait.getLink()
                    ));
                }
            }
            try {
                Boolean success = this.client.consumeMediumInWait(selectedMedium.getMediumId(), others).body();

                if (success != null && success) {
                    this.storage.deleteMediaInWait(mediumInWaits);
                    return true;
                } else {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> createMedium(MediumInWait mediumInWait, List<MediumInWait> mediumInWaits, MediaList list) {
        return TaskManager.runCompletableTask(() -> {
            ClientMediumInWait medium = new ClientMediumInWait(
                    mediumInWait.getTitle(),
                    mediumInWait.getMedium(),
                    mediumInWait.getLink()
            );
            Collection<ClientMediumInWait> others = new HashSet<>();

            if (mediumInWaits != null) {
                for (MediumInWait inWait : mediumInWaits) {
                    others.add(new ClientMediumInWait(
                            inWait.getTitle(),
                            inWait.getMedium(),
                            inWait.getLink()
                    ));
                }
            }
            Integer listId = list == null ? null : list.getListId();
            try {
                ClientMedium clientMedium = this.client.createFromMediumInWait(medium, others, listId).body();

                if (clientMedium == null) {
                    return false;
                }
                this.persister.persist(new ClientSimpleMedium(clientMedium));

                Collection<MediumInWait> toDelete = new HashSet<>();
                toDelete.add(mediumInWait);

                if (mediumInWaits != null) {
                    toDelete.addAll(mediumInWaits);
                }
                this.storage.deleteMediaInWait(toDelete);

                if (listId != null && listId > 0) {
                    this.storage.addItemsToList(listId, Collections.singleton(clientMedium.getId()));
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> moveMediaToList(int oldListId, int listId, Collection<Integer> ids) {
        return this.editService.moveMediaToList(oldListId, listId, ids);
    }

    @Override
    public CompletableFuture<Boolean> removeItemFromList(int listId, int mediumId) {
        return this.editService.removeItemFromList(listId, Collections.singleton(mediumId));
    }

    @Override
    public CompletableFuture<Boolean> removeItemFromList(int listId, Collection<Integer> mediumId) {
        return this.editService.removeItemFromList(listId, mediumId);
    }

    @Override
    public CompletableFuture<Boolean> addMediumToList(int listId, Collection<Integer> ids) {
        return this.editService.addMediumToList(listId, ids);
    }

    @Override
    public CompletableFuture<Boolean> moveItemFromList(int oldListId, int newListId, int mediumId) {
        return this.editService.moveItemFromList(oldListId, newListId, mediumId);
    }

    @Override
    public LiveData<PagedList<ExternalUser>> getExternalUser() {
        return this.storage.getExternalUser();
    }

    @Override
    public SpaceMedium getSpaceMedium(int mediumId) {
        return this.storage.getSpaceMedium(mediumId);
    }

    @Override
    public int getMediumType(Integer mediumId) {
        return this.storage.getMediumType(mediumId);
    }

    @Override
    public List<String> getReleaseLinks(int episodeId) {
        return this.storage.getReleaseLinks(episodeId);
    }

    @Override
    public void syncUser() throws IOException {
        Response<ClientUser> user = this.client.getUser();
        ClientUser body = user.body();

        if (!user.isSuccessful()) {
            try (ResponseBody responseBody = user.errorBody()) {
                if (responseBody != null) {
                    System.out.println(responseBody.string());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.persister.persist(body);
    }

    @Override
    public void updateReadWithLowerIndex(double combiIndex, boolean read, int mediumId) throws Exception {
        List<Integer> episodeIds = this.storage.getEpisodeIdsWithLowerIndex(combiIndex, mediumId, read);
        this.updateRead(episodeIds, read);
    }

    @Override
    public void clearLocalMediaData(Context context) {
        UserPreferences.setLastSync(new DateTime(0));
        TaskManager.runTask(() -> {
            this.loadedData.getPart().clear();
            this.loadedData.getEpisodes().clear();
            this.storage.clearLocalMediaData();
        });
    }

    @Override
    public LiveData<PagedList<NotificationItem>> getNotifications() {
        return this.storage.getNotifications();
    }

    @Override
    public void updateFailedDownloads(int episodeId) {
        this.storage.updateFailedDownload(episodeId);
    }

    @Override
    public List<FailedEpisode> getFailedEpisodes(Collection<Integer> episodeIds) {
        return this.storage.getFailedEpisodes(episodeIds);
    }

    @Override
    public void addNotification(NotificationItem notification) {
        this.storage.addNotification(notification);
    }

    @Override
    public SimpleEpisode getSimpleEpisode(int episodeId) {
        return this.storage.getSimpleEpisode(episodeId);
    }

    @Override
    public SimpleMedium getSimpleMedium(Integer mediumId) {
        return this.storage.getSimpleMedium(mediumId);
    }

    @Override
    public void clearNotifications() {
        this.storage.clearNotifications();
    }

    @Override
    public void clearFailEpisodes() {
        TaskManager.runAsyncTask(this.storage::clearFailEpisodes);
    }

    @Override
    public LiveData<List<MediaList>> getListSuggestion(String name) {
        return this.storage.getListSuggestion(name);
    }

    @Override
    public LiveData<Boolean> onDownloadable() {
        return this.storage.onDownloadAble();
    }

    @Override
    public void removeDanglingMedia(Collection<Integer> mediaIds) {
        this.storage.removeDanglingMedia(mediaIds);
    }

    @Override
    public LiveData<List<MediumItem>> getAllDanglingMedia() {
        return this.storage.getAllDanglingMedia();
    }
}