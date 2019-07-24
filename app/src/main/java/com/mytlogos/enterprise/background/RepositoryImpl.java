package com.mytlogos.enterprise.background;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.mytlogos.enterprise.background.api.Client;
import com.mytlogos.enterprise.background.api.model.ClientDownloadedEpisode;
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
import com.mytlogos.enterprise.background.api.model.ClientUpdateUser;
import com.mytlogos.enterprise.background.api.model.ClientUser;
import com.mytlogos.enterprise.background.api.model.InvalidatedData;
import com.mytlogos.enterprise.background.resourceLoader.BlockingLoadWorker;
import com.mytlogos.enterprise.background.resourceLoader.LoadWorker;
import com.mytlogos.enterprise.background.room.RoomStorage;
import com.mytlogos.enterprise.model.DisplayUnreadEpisode;
import com.mytlogos.enterprise.model.MediaList;
import com.mytlogos.enterprise.model.MediaListSetting;
import com.mytlogos.enterprise.model.MediumInWait;
import com.mytlogos.enterprise.model.MediumItem;
import com.mytlogos.enterprise.model.MediumSetting;
import com.mytlogos.enterprise.model.News;
import com.mytlogos.enterprise.model.ToDownload;
import com.mytlogos.enterprise.model.TocPart;
import com.mytlogos.enterprise.model.UpdateUser;
import com.mytlogos.enterprise.model.User;
import com.mytlogos.enterprise.service.DownloadWorker;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import retrofit2.Call;
import retrofit2.Response;

public class RepositoryImpl implements Repository {
    private static RepositoryImpl INSTANCE;
    private final ClientModelPersister persister;
    private final LiveData<? extends User> storageUserLiveData;
    private final MediatorLiveData<User> userLiveData;
    private final Client client;
    private final DatabaseStorage storage;
    private final LoadData loadedData;
    private final LoadWorker loadWorker;
    private boolean clientOnline;

    private RepositoryImpl(Application application) {
        this.storage = new RoomStorage(application);
        this.storageUserLiveData = storage.getUser();
        this.client = new Client();
        this.loadedData = new LoadData();
        this.userLiveData = new MediatorLiveData<>();
        this.persister = this.storage.getPersister(this, this.loadedData);

        DependantGenerator dependantGenerator = this.storage.getDependantGenerator(this.loadedData);
        this.loadWorker = new BlockingLoadWorker(
                this.loadedData,
                this,
                this.persister,
                dependantGenerator
        );
    }

    /**
     * Return the Repository Singleton Instance.
     *
     * @return returns the singleton
     * @throws IllegalStateException if repository is not yet initialized
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

                    // check first login
                    TaskManager.runTask(() -> {
                        try {
                            Future<Response<ClientUser>> future = TaskManager.runAsyncTask(
                                    () -> INSTANCE.client.checkLogin().execute()
                            );

                            // ask the database what data it has, to check if it needs to be loaded from the server
                            INSTANCE.getLoadedData();

                            // wait for the query to finish
                            ClientUser clientUser = future.get().body();

                            if (clientUser != null) {
                                // set authentication in client before persisting user,
                                // as it may load data which requires authentication
                                INSTANCE.client.setAuthentication(clientUser.getUuid(), clientUser.getSession());
                            }

                            INSTANCE.persister
                                    .persist(clientUser)
                                    .finish();


                            Log.i(RepositoryImpl.class.getSimpleName(), "successful query");
                        } catch (InterruptedException | ExecutionException e) {
                            Log.e(RepositoryImpl.class.getSimpleName(), "failed query", e);
                        } finally {
                            // storage.getUser() does nothing, but storageUserLiveData invalidates instantly?
                            new Handler(Looper.getMainLooper()).post(() -> INSTANCE.userLiveData.addSource(
                                    INSTANCE.storageUserLiveData,
                                    value -> {
                                        if (value == null) {
                                            INSTANCE.client.clearAuthentication();
                                        } else {
                                            INSTANCE.client.setAuthentication(value.getUuid(), value.getSession());
                                        }
                                        INSTANCE.userLiveData.postValue(value);
                                    }
                            ));
                        }
                    });
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public boolean isClientOnline() {
        return this.clientOnline;
    }

    @Override
    public boolean isClientAuthenticated() {
        return this.client.isAuthenticated();
    }

    @Override
    public LoadWorker getLoadWorker() {
        return loadWorker;
    }

    private void getLoadedData() {
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
    public LiveData<User> getUser() {
        return userLiveData;
    }

    @Override
    public void updateUser(UpdateUser updateUser) {
        TaskManager.runTask(() -> {
            User value = userLiveData.getValue();

            if (value == null) {
                throw new IllegalArgumentException("cannot change user when none is logged in");
            }
            ClientUpdateUser user = new ClientUpdateUser(
                    value.getUuid(), updateUser.getName(),
                    updateUser.getPassword(),
                    updateUser.getNewPassword()
            );
            try {
                Boolean body = this.client.updateUser(user).execute().body();

                if (body != null && body) {
                    this.persister.persist(user);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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
        Response<ClientUser> response = this.client.login(email, password).execute();
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
        Response<ClientUser> response = this.client.register(email, password).execute();
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
                Response<Boolean> response = client.logout().execute();
                if (!response.isSuccessful()) {
                    System.out.println("Log out was not successful: " + response.message());
                }
                storage.deleteAllUser();
            } catch (IOException e) {
                storage.deleteAllUser();
                e.printStackTrace();
            }
        });
    }

    @Override
    public CompletableFuture<List<ClientEpisode>> loadEpisodeAsync(Collection<Integer> episodeIds) {
        return CompletableFuture.supplyAsync(() -> loadEpisodeSync(episodeIds));
    }

    @Override
    public List<ClientEpisode> loadEpisodeSync(Collection<Integer> episodeIds) {
        try {
            System.out.println("loading episodes: " + episodeIds + " on " + Thread.currentThread());
            return this.client.getEpisodes(episodeIds).execute().body();
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
            return this.client.getMedia(mediaIds).execute().body();
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
            return this.client.getParts(partIds).execute().body();
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
            return this.client.getLists(listIds).execute().body();
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
            return this.client.getExternalUser(externalUuids).execute().body();
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
            return this.client.getNews(newsIds).execute().body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public LiveData<List<News>> getNews() {
        return this.storage.getNews();
    }

    @Override
    public void setNewsInterval(DateTime from, DateTime to) {
        storage.setNewsInterval(from, to);
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
        List<ClientNews> news = this.client.getNews(latest, null).execute().body();
        if (news != null) {
            this.persister.persistNews(news);
        }
    }

    @Override
    public void loadInvalidated() throws IOException {
        List<InvalidatedData> invalidatedData = this.client.getInvalidated().execute().body();

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
            ClientUser user = this.client.checkLogin().execute().body();
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
        this.storage.updateSaved(episodeIds, saved);
    }

    @Override
    public List<Integer> getToDeleteEpisodes() {
        return this.storage.getToDeleteEpisodes();
    }

    @Override
    public List<ClientDownloadedEpisode> downloadedEpisodes(Collection<Integer> episodeIds) throws IOException {
        return this.client.downloadEpisodes(episodeIds).execute().body();
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
    public List<Integer> getDownloadableEpisodes(Integer mediumId) {
        return this.storage.getDownloadableEpisodes(mediumId);
    }

    @Override
    public LiveData<List<DisplayUnreadEpisode>> getUnReadEpisodes() {
        return this.storage.getUnreadEpisodes();
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
    public CompletableFuture<String> updateListName(MediaListSetting listSetting, String text) {
        return TaskManager.runCompletableTask(() -> {
            try {
                this.executeCall(this.client.updateList(new ClientMediaList(
                        listSetting.getUuid(),
                        listSetting.getListId(),
                        text,
                        listSetting.getMedium(),
                        new int[0]
                )));
                ClientListQuery query = this.executeCall(this.client.getList(listSetting.getListId())).body();
                this.persister.persist(query).finish();
            } catch (IOException e) {
                e.printStackTrace();
                return "Could not update List";
            }
            return "";
        });
    }

    @Override
    public CompletableFuture<String> updateListMedium(MediaListSetting listSetting, int newMediumType) {
        return TaskManager.runCompletableTask(() -> {
            try {
                this.client.updateList(new ClientMediaList(
                        listSetting.getUuid(),
                        listSetting.getListId(),
                        listSetting.getName(),
                        newMediumType,
                        new int[0]
                ));
                ClientListQuery query = this.executeCall(this.client.getList(listSetting.getListId())).body();
                this.persister.persist(query).finish();
            } catch (IOException e) {
                e.printStackTrace();
                return "Could not update List";
            }
            return "";
        });
    }

    private <T> Response<T> executeCall(Call<T> call) throws IOException {
        try {
            Response<T> response = call.execute();
            clientOnline = true;
            return response;
        } catch (IOException e) {
            this.clientOnline = false;
            throw new IOException(e);
        }
    }

    @Override
    public void updateToDownload(boolean add, ToDownload toDownload) {
        this.storage.updateToDownload(add, toDownload);
        DownloadWorker.enqueueDownloadTask();
    }

    @Override
    public LiveData<List<MediumInWait>> getAllMediaInWait() {
        return this.storage.getAllMediaInWait();
    }

    @Override
    public LiveData<List<MediumItem>> getAllMedia() {
        return this.storage.getAllMedia();
    }

    @Override
    public LiveData<MediumSetting> getMediumSettings(int mediumId) {
        return this.storage.getMediumSettings(mediumId);
    }

    @Override
    public CompletableFuture<String> updateMediumType(MediumSetting mediumSettings) {
        return TaskManager.runCompletableTask(() -> {
            try {
                this.client.updateMedia(new ClientMedium(
                        new int[0],
                        new int[0],
                        mediumSettings.getCurrentRead(),
                        new int[0],
                        mediumSettings.getMediumId(),
                        mediumSettings.getCountryOfOrigin(),
                        mediumSettings.getLanguageOfOrigin(),
                        mediumSettings.getAuthor(),
                        mediumSettings.getTitle(),
                        mediumSettings.getMedium(),
                        mediumSettings.getArtist(),
                        mediumSettings.getLang(),
                        mediumSettings.getStateOrigin(),
                        mediumSettings.getStateTL(),
                        mediumSettings.getSeries(),
                        mediumSettings.getUniverse()
                ));
                ClientMedium medium = this.executeCall(this.client.getMedium(mediumSettings.getMediumId())).body();
                this.persister.persist(medium).finish();
            } catch (IOException e) {
                e.printStackTrace();
                return "Could not update Medium";
            }
            return "";
        });
    }

    @Override
    public LiveData<List<TocPart>> getToc(int mediumId) {
        return this.storage.getToc(mediumId);
    }

    @Override
    public LiveData<List<MediumItem>> getMediumItems(int listId, boolean isExternal) {
        return this.storage.getMediumItems(listId, isExternal);
    }

    @Override
    public void loadMediaInWaitSync() throws IOException {
        Response<List<ClientMediumInWait>> response = this.client.getMediumInWait().execute();
        List<ClientMediumInWait> medium = response.body();

        if (medium != null && !medium.isEmpty()) {
            this.persister.persistMediaInWait(medium);
        }
    }
}

