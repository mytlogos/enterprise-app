package com.mytlogos.enterprise.background.room;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.mytlogos.enterprise.background.ClientModelPersister;
import com.mytlogos.enterprise.background.DatabaseStorage;
import com.mytlogos.enterprise.background.LoadData;
import com.mytlogos.enterprise.background.Repository;
import com.mytlogos.enterprise.background.TaskManager;
import com.mytlogos.enterprise.background.api.model.ClientEpisode;
import com.mytlogos.enterprise.background.api.model.ClientExternalMediaList;
import com.mytlogos.enterprise.background.api.model.ClientExternalUser;
import com.mytlogos.enterprise.background.api.model.ClientListQuery;
import com.mytlogos.enterprise.background.api.model.ClientMediaList;
import com.mytlogos.enterprise.background.api.model.ClientMedium;
import com.mytlogos.enterprise.background.api.model.ClientNews;
import com.mytlogos.enterprise.background.api.model.ClientPart;
import com.mytlogos.enterprise.background.api.model.ClientReadEpisode;
import com.mytlogos.enterprise.background.api.model.ClientUser;
import com.mytlogos.enterprise.background.room.model.RoomEpisode;
import com.mytlogos.enterprise.background.room.model.RoomExternalMediaList;
import com.mytlogos.enterprise.background.room.model.RoomExternalUser;
import com.mytlogos.enterprise.background.room.model.RoomMediaList;
import com.mytlogos.enterprise.background.room.model.RoomMedium;
import com.mytlogos.enterprise.background.room.model.RoomNews;
import com.mytlogos.enterprise.background.room.model.RoomPart;
import com.mytlogos.enterprise.background.room.model.RoomUser;
import com.mytlogos.enterprise.background.room.modelImpl.UserImpl;
import com.mytlogos.enterprise.model.User;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class RoomStorage implements DatabaseStorage {
    private final UserDao userDao;
    private final NewsDao newsDao;
    private final EpisodeDao episodeDao;
    private final PartDao partDao;
    private final MediumDao mediumDao;
    private final MediaListDao mediaListDao;
    private final ExternalMediaListDao externalMediaListDao;
    private final ExternalUserDao externalUserDao;
    private final LiveData<? extends User> storageUserLiveData;
    private boolean loading = false;

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
        storageUserLiveData = userDao.getUser();
    }

    @Override
    public LiveData<? extends User> getUser() {
        return storageUserLiveData;
    }

    @Override
    public void updateUser(@NonNull User user) {
        TaskManager.getInstance().runTask(() -> userDao.update(new RoomUser(user.getName(), user.getUuid(), user.getSession())));
    }

    @Override
    public void insertUser(@NonNull User user) {
        TaskManager.getInstance().runTask(() -> userDao.insert(new RoomUser(user.getName(), user.getUuid(), user.getSession())));
    }

    @Override
    public void deleteAllUser() {
        TaskManager.getInstance().runTask(userDao::deleteAllUser);
    }

    @Override
    public ClientModelPersister getPersister(LoadData unLoadedData, LoadData loadedData) {
        return new RoomModelPersister(unLoadedData, loadedData, this);
    }

    @Override
    public void deleteOldNews() {
        TaskManager.getInstance().runTask(newsDao::deleteOldNews);
    }

    @Override
    public void changeUser(User newUser) {
        System.out.println("change user in background to: " + newUser);
        // todo check if this is synchronous or asynchronous, may lead to race condition,
        // todo one trying to insert and the other to delete

        TaskManager.getInstance().runTask(() -> {
            userDao.deleteAllUser();
            if (newUser != null) {
                userDao.insert((RoomUser) newUser);
            }
        });
    }

    @Override
    public boolean isLoading() {
        return this.loading;
    }

    @Override
    public void setLoading(boolean loading) {
        this.loading = loading;
    }


    @Override
    public void setNewsInterval(DateTime from, DateTime to) {
        // todo
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

    private static class RoomModelPersister implements ClientModelPersister {
        private final LoadData unLoadedData;
        private final LoadData loadedData;
        private final RoomStorage storage;
        private final LoadData toLoad = new LoadData();
        private final List<Runnable> afterEpisodeLoad = new ArrayList<>();
        private final List<Runnable> afterPartLoad = new ArrayList<>();
        private final List<Runnable> afterMediumLoad = new ArrayList<>();
        private final List<Runnable> afterMediaListLoad = new ArrayList<>();
        private final List<Runnable> afterExternalMediaListLoad = new ArrayList<>();
        private final List<Runnable> afterExternalUserLoad = new ArrayList<>();
        private final List<Runnable> afterNewsLoad = new ArrayList<>();

        RoomModelPersister(LoadData unLoadedData, LoadData loadedData, RoomStorage storage) {
            this.unLoadedData = unLoadedData;
            this.loadedData = loadedData;
            this.storage = storage;
        }

        @Override
        public void persist(ClientEpisode... episodes) {
            List<RoomEpisode> list = new ArrayList<>(episodes.length);

            for (ClientEpisode episode : episodes) {
                if (loadedData.getEpisodes().contains(episode.getId())) {
                    continue;
                }
                RoomEpisode roomEpisode = new RoomEpisode(
                        episode.getId(), episode.getProgress(), episode.getReadDate(), episode.getPartId(),
                        episode.getTitle(), episode.getTotalIndex(), episode.getPartialIndex(),
                        episode.getUrl(), episode.getReleaseDate()
                );
                list.add(roomEpisode);
            }
            this.storage.episodeDao.insertBulk(list);

            for (RoomEpisode episode : list) {
                unLoadedData.getEpisodes().remove(episode.getEpisodeId());
            }
        }

        @Override
        public void persist(String uuid, ClientMediaList... mediaLists) {
            List<RoomMediaList> list = new ArrayList<>(mediaLists.length);
            List<RoomMediaList.MediaListMediaJoin> joins = new ArrayList<>();

            for (ClientMediaList mediaList : mediaLists) {
                RoomMediaList roomMediaList = new RoomMediaList(
                        mediaList.getId(), uuid, mediaList.getName(), mediaList.getMedium()
                );
                for (int mediumId : mediaList.getItems()) {
                    joins.add(new RoomMediaList.MediaListMediaJoin(mediaList.getId(), mediumId));
                }
                list.add(roomMediaList);
            }
            this.storage.mediaListDao.insertBulk(list);
            this.storage.mediaListDao.addJoin(joins);
        }

        @Override
        public void persist(String externalUuid, ClientExternalMediaList... externalMediaLists) {
            List<RoomExternalMediaList> list = new ArrayList<>(externalMediaLists.length);
            List<RoomExternalMediaList.ExternalListMediaJoin> joins = new ArrayList<>();

            for (ClientExternalMediaList externalMediaList : externalMediaLists) {
                RoomExternalMediaList roomExternalMediaList = new RoomExternalMediaList(
                        externalUuid, externalMediaList.getId(), externalMediaList.getName(),
                        externalMediaList.getMedium(), externalMediaList.getUrl()
                );
                for (int mediumId : externalMediaList.getItems()) {
                    joins.add(new RoomExternalMediaList.ExternalListMediaJoin(externalMediaList.getId(), mediumId));
                }

                list.add(roomExternalMediaList);
            }
            this.storage.externalMediaListDao.insertBulk(list);
            this.storage.externalMediaListDao.addJoin(joins);
        }

        @Override
        public void persist(String uuid, ClientExternalUser... externalUsers) {
            List<RoomExternalUser> list = new ArrayList<>(externalUsers.length);
            List<RoomExternalMediaList> externalMediaLists = new ArrayList<>();

            for (ClientExternalUser externalUser : externalUsers) {
                RoomExternalUser roomExternalUser = new RoomExternalUser(
                        externalUser.getUuid(), uuid, externalUser.getIdentifier(),
                        externalUser.getType()
                );
                for (ClientExternalMediaList userList : externalUser.getLists()) {
                    externalMediaLists.add(new RoomExternalMediaList(
                            userList.getUuid(), userList.getId(), userList.getName(),
                            userList.getMedium(), userList.getUrl())
                    );
                }
                list.add(roomExternalUser);
            }
            this.storage.externalUserDao.insertBulk(list);
            this.storage.externalMediaListDao.insertBulk(externalMediaLists);
        }

        @Override
        public void persist(String uuid, ClientListQuery... listQueries) {
            List<ClientMediaList> list = new ArrayList<>(listQueries.length);
            List<ClientMedium> media = new ArrayList<>(listQueries.length);
            List<RoomMediaList.MediaListMediaJoin> joins = new ArrayList<>(listQueries.length);

            for (ClientListQuery listQuery : listQueries) {
                ClientMediaList mediaList = listQuery.getList();
                list.add(mediaList);

                for (ClientMedium clientMedium : listQuery.getMedia()) {
                    joins.add(new RoomMediaList.MediaListMediaJoin(mediaList.getId(), clientMedium.getId()));
                    media.add(clientMedium);
                }
            }
            this.persist(media.toArray(new ClientMedium[0]));
            this.persist(uuid, list.toArray(new ClientMediaList[0]));
            this.storage.mediaListDao.addJoin(joins);
        }

        @Override
        public void persist(ClientMedium... media) {
            List<RoomMedium> list = new ArrayList<>(media.length);

            for (ClientMedium medium : media) {
                if (loadedData.getMedia().contains(medium.getId())) {
                    continue;
                }
                RoomMedium roomMedium = new RoomMedium(
                        medium.getCurrentRead(), medium.getId(), medium.getCountryOfOrigin(),
                        medium.getLanguageOfOrigin(), medium.getAuthor(), medium.getTitle(),
                        medium.getMedium(), medium.getArtist(), medium.getLang(),
                        medium.getStateOrigin(), medium.getStateTL(), medium.getSeries(),
                        medium.getUniverse()
                );
                list.add(roomMedium);
            }
            this.storage.mediumDao.insertBulk(list);

            for (RoomMedium medium : list) {
                unLoadedData.getMedia().remove(medium.getMediumId());
            }
        }

        @Override
        public void persist(ClientNews... news) {
            List<RoomNews> list = new ArrayList<>(news.length);

            for (ClientNews clientNews : news) {
                if (loadedData.getNews().contains(clientNews.getId())) {
                    continue;
                }
                RoomNews roomNews = new RoomNews(
                        clientNews.getId(), clientNews.isRead(),
                        clientNews.getTitle(), clientNews.getDate()
                );
                list.add(roomNews);
            }
            this.storage.newsDao.insertNews(list);

            for (RoomNews roomNews : list) {
                unLoadedData.getNews().remove(roomNews.getNewsId());
            }
        }

        @Override
        public void persist(int mediumId, ClientPart... parts) {
            List<RoomPart> list = new ArrayList<>(parts.length);

            // todo handle part episodes as full object or only as number?
            for (ClientPart part : parts) {
                if (loadedData.getPart().contains(part.getId())) {
                    continue;
                }
                RoomPart roomPart = new RoomPart(
                        part.getId(), mediumId, part.getTitle(), part.getTotalIndex(),
                        part.getPartialIndex()
                );
                list.add(roomPart);
            }
            this.storage.partDao.insertBulk(list);

            for (RoomPart part : list) {
                unLoadedData.getPart().remove(part.getPartId());
            }
        }

        @Override
        public void persist(ClientReadEpisode[] readMedia) {

            for (ClientReadEpisode readMedium : readMedia) {
                this.storage.episodeDao.update(
                        readMedium.getEpisodeId(),
                        readMedium.getProgress(),
                        readMedium.getReadDate()
                );
            }
        }

        @Override
        public User persist(ClientUser clientUser) {
            if (clientUser == null) {
                storage.deleteAllUser();
                return null;
            } else {
                List<RoomUser.UserUnReadChapterJoin> unreadChapter = new ArrayList<>();
                List<RoomUser.UserUnReadChapterJoin> deferUnreadChapter = new ArrayList<>();

                for (int clientReadChapter : clientUser.getUnreadChapter()) {
                    RoomUser.UserUnReadChapterJoin userUnReadChapterJoin = new RoomUser.UserUnReadChapterJoin(
                            clientUser.getUuid(),
                            clientReadChapter
                    );
                    if (loadedData.getEpisodes().contains(clientReadChapter)) {
                        unreadChapter.add(userUnReadChapterJoin);
                    } else {
                        deferUnreadChapter.add(userUnReadChapterJoin);
                        this.toLoad.getEpisodes().add(clientReadChapter);
                    }
                }

                List<RoomUser.UserUnReadNewsJoin> unreadNews = new ArrayList<>();
                List<RoomUser.UserUnReadNewsJoin> deferUnreadNews = new ArrayList<>();

                for (ClientNews clientNews : clientUser.getUnreadNews()) {
                    RoomUser.UserUnReadNewsJoin userUnReadNewsJoin = new RoomUser.UserUnReadNewsJoin(
                            clientUser.getUuid(),
                            clientNews.getId()
                    );
                    if (loadedData.getNews().contains(clientNews.getId())) {
                        unreadNews.add(userUnReadNewsJoin);
                    } else {
                        deferUnreadNews.add(userUnReadNewsJoin);
                        this.toLoad.getNews().add(clientNews.getId());
                    }

                }

                List<RoomUser.UserReadTodayJoin> readToday = new ArrayList<>();
                List<RoomUser.UserReadTodayJoin> deferReadToday = new ArrayList<>();

                for (ClientReadEpisode clientReadEpisode : clientUser.getReadToday()) {
                    RoomUser.UserReadTodayJoin userReadTodayJoin = new RoomUser.UserReadTodayJoin(
                            clientUser.getUuid(),
                            clientReadEpisode.getEpisodeId()
                    );
                    if (this.loadedData.getEpisodes().contains(clientReadEpisode.getEpisodeId())) {
                        readToday.add(userReadTodayJoin);
                    } else {
                        this.toLoad.getEpisodes().add(clientReadEpisode.getEpisodeId());
                        deferReadToday.add(userReadTodayJoin);
                    }
                }

                List<String> extUser = Arrays
                        .stream(clientUser.getExternalUser())
                        .map(ClientExternalUser::getUuid)
                        .collect(Collectors.toList());

                List<Integer> lists = Arrays
                        .stream(clientUser.getLists())
                        .map(ClientMediaList::getId)
                        .collect(Collectors.toList());

                UserImpl user = new UserImpl(
                        clientUser.getName(), clientUser.getUuid(), clientUser.getSession()
                );
                user.getUnReadNewsJoins().addAll(unreadNews);
                user.getReadTodayJoins().addAll(readToday);
                user.getUnReadChapterJoins().addAll(unreadChapter);

                RoomUser roomUser = new RoomUser(
                        clientUser.getName(),
                        clientUser.getUuid(),
                        clientUser.getSession()
                );

                // persist user
                this.storage.userDao.insert(roomUser);
                // persist lists
                this.persist(clientUser.getUuid(), clientUser.getLists());
                // persist externalUser
                this.persist(clientUser.getUuid(), clientUser.getExternalUser());
                // persist loaded unread News
                this.persist(clientUser.getUnreadNews());
                // persist/update media with data
                this.persist(clientUser.getReadToday());
                // persist readToday join
                this.storage.userDao.addReadToday(readToday);
                // persist unReadChapter join
                this.storage.userDao.addUnReadChapter(unreadChapter);
                // persist unReadNews join
                this.storage.userDao.addUnReadNews(unreadNews);

                if (!deferReadToday.isEmpty()) {
                    this.afterEpisodeLoad.add(() -> this.storage.userDao.addReadToday(deferReadToday));
                }
                if (!deferUnreadChapter.isEmpty()) {
                    this.afterEpisodeLoad.add(() -> this.storage.userDao.addUnReadChapter(deferUnreadChapter));
                }
                if (!deferUnreadNews.isEmpty()) {
                    this.afterNewsLoad.add(() -> this.storage.userDao.addUnReadNews(deferUnreadNews));
                }

                user.getExternalUser().addAll(extUser);
                user.getMediaList().addAll(lists);

                return user;
            }
        }

        @Override
        public void finish() {
            List<CompletableFuture<?>> completableFutures = new ArrayList<>();

            if (!this.toLoad.getPart().isEmpty()) {

            }
            if (!this.toLoad.getEpisodes().isEmpty()) {
                completableFutures.add(Repository
                        .getInstance()
                        .loadEpisode(this.toLoad.getEpisodes())
                        .thenAcceptAsync(aVoid -> {

                        }));
            }

            if (!this.toLoad.getNews().isEmpty()) {
                completableFutures.add(Repository.getInstance().loadNews(this.toLoad.getNews()));
            }
            if (!this.toLoad.getMedia().isEmpty()) {

            }
            if (!this.toLoad.getMediaList().isEmpty()) {

            }
            if (!this.toLoad.getExternalMediaList().isEmpty()) {

            }
            if (!this.toLoad.getExternalUser().isEmpty()) {

            }
            for (CompletableFuture<?> future : completableFutures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
