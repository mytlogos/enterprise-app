package com.mytlogos.enterprise.background;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.util.Log;

import com.mytlogos.enterprise.background.api.Client;
import com.mytlogos.enterprise.background.api.model.ClientEpisode;
import com.mytlogos.enterprise.background.api.model.ClientExternalMediaList;
import com.mytlogos.enterprise.background.api.model.ClientExternalUser;
import com.mytlogos.enterprise.background.api.model.ClientMedium;
import com.mytlogos.enterprise.background.api.model.ClientMultiListQuery;
import com.mytlogos.enterprise.background.api.model.ClientNews;
import com.mytlogos.enterprise.background.api.model.ClientPart;
import com.mytlogos.enterprise.background.api.model.ClientUpdateUser;
import com.mytlogos.enterprise.background.api.model.ClientUser;
import com.mytlogos.enterprise.background.room.RoomStorage;
import com.mytlogos.enterprise.model.News;
import com.mytlogos.enterprise.model.UpdateUser;
import com.mytlogos.enterprise.model.User;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import retrofit2.Response;

public class Repository {

    private static Repository INSTANCE;
    private final ClientModelPersister persister;
    private final LiveData<? extends User> storageUserLiveData;
    private final MediatorLiveData<User> userLiveData;
    private final Client client;
    private final DatabaseStorage storage;
    private final LoadData loadedData;
    private final LoadData unLoadedData;
    private final LoadWorker loadWorker;

    private Repository(Application application) {
        this.storage = new RoomStorage(application);
        this.storageUserLiveData = storage.getUser();
        this.client = new Client();
        this.unLoadedData = new LoadData();
        this.loadedData = new LoadData();
        this.userLiveData = new MediatorLiveData<>();
        this.persister = this.storage.getPersister(this, this.unLoadedData, this.loadedData);
        this.loadWorker = new LoadWorker(this.loadedData, this, this.persister);
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


                            Log.i(Repository.class.getSimpleName(), "successful query");
                        } catch (InterruptedException | ExecutionException e) {
                            Log.e(Repository.class.getSimpleName(), "failed query", e);
                        } finally {
                            // storage.getUser() does nothing, but storageUserLiveData invalidates instantly?
                            INSTANCE.userLiveData.addSource(
                                    INSTANCE.storageUserLiveData,
                                    value -> INSTANCE.userLiveData.postValue(value)
                            );
                        }
                    });
                }
            }
        }
        return INSTANCE;
    }

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

    public LiveData<User> getUser() {
        return userLiveData;
    }

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

    CompletableFuture<List<ClientEpisode>> loadEpisode(Collection<Integer> episodeIds) {
        try {
            return CompletableFuture.completedFuture(((Callable<List<ClientEpisode>>) () -> {
                try {
                    System.out.println("loading episodes: " + episodeIds + " on " + Thread.currentThread());
                    return this.client.getEpisodes(episodeIds).execute().body();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }).call());
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    CompletableFuture<List<ClientMedium>> loadMedia(Collection<Integer> mediaIds) {
        try {
            return CompletableFuture.completedFuture(((Callable<List<ClientMedium>>) () -> {
                try {
                    System.out.println("loading media: " + mediaIds + " on " + Thread.currentThread());
                    return this.client.getMedia(mediaIds).execute().body();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }).call());
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    CompletableFuture<List<ClientPart>> loadPart(Collection<Integer> partIds) {
        try {
            return CompletableFuture.completedFuture(((Callable<List<ClientPart>>) () -> {
                try {
                    System.out.println("loading parts: " + partIds + " on " + Thread.currentThread());
                    return this.client.getParts(partIds).execute().body();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }).call());
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    CompletableFuture<ClientMultiListQuery> loadMediaList(Collection<Integer> listIds) {
        try {
            return CompletableFuture.completedFuture(((Callable<ClientMultiListQuery>) () -> {
                try {
                    System.out.println("loading lists: " + listIds + " on " + Thread.currentThread());
                    return this.client.getLists(listIds).execute().body();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }).call());
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    CompletableFuture<List<ClientExternalMediaList>> loadExternalMediaList(Collection<Integer> externalListIds) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("loading ExtLists: " + externalListIds + " on " + Thread.currentThread());

            // todo implement loading of externalMediaLists
//            try {
//                List<ClientEpisode> body = this.client.getExternalUser(episodeIds).execute().body();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            return null;
        });
    }

    CompletableFuture<List<ClientExternalUser>> loadExternalUser(Collection<String> externalUuids) {
        try {
            return CompletableFuture.completedFuture(((Callable<List<ClientExternalUser>>) () -> {
                try {
                    System.out.println("loading ExternalUser: " + externalUuids + " on " + Thread.currentThread());
                    return this.client.getExternalUser(externalUuids).execute().body();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }).call());
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    CompletableFuture<List<ClientNews>> loadNews(Collection<Integer> newsIds) {
        try {
            return CompletableFuture.completedFuture(((Callable<List<ClientNews>>) () -> {
                try {
                    System.out.println("loading News: " + newsIds + " on " + Thread.currentThread());
                    return this.client.getNews(newsIds).execute().body();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }).call());
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    public LiveData<List<News>> getNews() {
        return this.storage.getNews();
    }

    public void setNewsInterval(DateTime from, DateTime to) {
        storage.setNewsInterval(from, to);
    }

    public void removeOldNews() {
        TaskManager.runTask(storage::deleteOldNews);
    }

    public boolean isLoading() {
        return storage.isLoading();
    }

    public void refreshNews(DateTime latest) throws IOException {
        List<ClientNews> news = this.client.getNews(latest, null).execute().body();
        if (news != null) {
            this.persister.persistNews(news);
        }
    }
}

