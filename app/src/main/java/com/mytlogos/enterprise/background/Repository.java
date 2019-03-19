package com.mytlogos.enterprise.background;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.mytlogos.enterprise.background.api.Client;
import com.mytlogos.enterprise.background.api.model.ClientUser;
import com.mytlogos.enterprise.background.room.RoomStorage;
import com.mytlogos.enterprise.model.News;
import com.mytlogos.enterprise.model.User;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import retrofit2.Response;

public class Repository {

    private static Repository INSTANCE;
    private final LiveData<? extends User> storageUserLiveData;
    private final MediatorLiveData<User> userLiveData;
    private final Client client;
    private final DatabaseStorage storage;
    private final LoadData loadedData = new LoadData();
    private final LoadData unLoadedData = new LoadData();

    private Repository(Application application) {
        storage = new RoomStorage(application);
        storageUserLiveData = storage.getUser();
        client = new Client();
        userLiveData = new MediatorLiveData<>();
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
            synchronized (Repository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Repository(application);
                    INSTANCE.storage.setLoading(true);
                    System.out.println("querying");

                    // check first login
                    TaskManager.getInstance().runTask(() -> {
                        try {
                            Future<Response<ClientUser>> future = TaskManager
                                    .getInstance()
                                    .runAsyncTask(() -> INSTANCE.client.checkLogin().execute());

                            // ask the database what data it has, to check if it needs to be loaded from the server
                            INSTANCE.getLoadedData();

                            // wait for the query to finish
                            ClientUser clientUser = future.get().body();

                            User user = INSTANCE
                                    .storage
                                    .getPersister(INSTANCE.unLoadedData, INSTANCE.loadedData)
                                    .persist(clientUser);

                            if (user != null) {
                                INSTANCE.client.setAuthentication(user.getUuid(), user.getSession());
                            }

                            INSTANCE.storage.changeUser(user);
                            Log.i(Repository.class.getSimpleName(), "successful query");
                        } catch (InterruptedException | ExecutionException e) {
                            Log.e(Repository.class.getSimpleName(), "failed query", e);
                        } finally {
                            INSTANCE.userLiveData.addSource(
                                    INSTANCE.getUser(),
                                    value -> INSTANCE.userLiveData.postValue(value)
                            );
                        }

                    });
                }
            }
        }
        return INSTANCE;
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

    public LiveData<User> getUser() {
        return userLiveData;
    }

    public void updateUser(@NonNull User user) {
        TaskManager.getInstance().runTask(() -> storage.updateUser(user));
    }

    public void deleteAllUser() {
        TaskManager.getInstance().runTask(storage::deleteAllUser);
    }

    /**
     * Synchronous Login.
     *
     * @param email    email or name of the user
     * @param password password of the user
     * @throws IOException if an connection problem arose
     */
    public void login(String email, String password) throws IOException {
        Response<ClientUser> response = this.client.login(email, password).execute();
        ClientUser user = response.body();

        if (user != null) {
            INSTANCE.client.setAuthentication(user.getUuid(), user.getSession());
        }
        storage.getPersister(this.unLoadedData, this.loadedData).persist(user);
    }

    public void logout() {
        TaskManager.getInstance().runTask(() -> {
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

    public CompletableFuture<Void> loadEpisode(Collection<Integer> episodeIds) {
        return CompletableFuture.runAsync(() -> this.client.getEpisodes(episodeIds));
    }

    public CompletableFuture<Void> loadMedia(Collection<Integer> mediaIds) {
        return null;
    }

    public CompletableFuture<Void> loadPart(Collection<Integer> partIds) {
        return null;
    }

    public CompletableFuture<Void> loadMediaList(Collection<Integer> listIds) {
        return null;
    }

    public CompletableFuture<Void> loadExternalMediaList(Collection<Integer> externalListIds) {
        return null;
    }

    public CompletableFuture<Void> loadExternalUser(Collection<String> externalUuids) {
        return null;
    }

    public CompletableFuture<Void> loadNews(Collection<Integer> newsIds) {
        return null;
    }

    /**
     * Synchronous Registration.
     *
     * @param email    email or name of the user
     * @param password password of the user
     */
    public void register(String email, String password) throws IOException {
        Response<ClientUser> response = this.client.register(email, password).execute();
        ClientUser user = response.body();
        if (user != null) {
            INSTANCE.client.setAuthentication(user.getUuid(), user.getSession());
        }
        storage.getPersister(this.unLoadedData, this.loadedData).persist(user);
    }

    public LiveData<List<News>> getNews() {
        MutableLiveData<List<News>> data = new MutableLiveData<>();
        data.setValue(new ArrayList<>());
        return data;
    }

    public void setNewsInterval(DateTime from, DateTime to) {
        storage.setNewsInterval(from, to);
    }

    public void removeOldNews() {
        TaskManager.getInstance().runTask(storage::deleteOldNews);
    }

    public boolean isLoading() {
        return storage.isLoading();
    }
}

