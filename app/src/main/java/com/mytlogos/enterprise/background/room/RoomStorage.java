package com.mytlogos.enterprise.background.room;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;

import com.mytlogos.enterprise.background.ClientConsumer;
import com.mytlogos.enterprise.background.ClientModelPersister;
import com.mytlogos.enterprise.background.DatabaseStorage;
import com.mytlogos.enterprise.background.LoadData;
import com.mytlogos.enterprise.background.LoadWorker;
import com.mytlogos.enterprise.background.Repository;
import com.mytlogos.enterprise.background.TaskManager;
import com.mytlogos.enterprise.background.api.model.ClientEpisode;
import com.mytlogos.enterprise.background.api.model.ClientExternalMediaList;
import com.mytlogos.enterprise.background.api.model.ClientExternalUser;
import com.mytlogos.enterprise.background.api.model.ClientListQuery;
import com.mytlogos.enterprise.background.api.model.ClientMediaList;
import com.mytlogos.enterprise.background.api.model.ClientMedium;
import com.mytlogos.enterprise.background.api.model.ClientMultiListQuery;
import com.mytlogos.enterprise.background.api.model.ClientNews;
import com.mytlogos.enterprise.background.api.model.ClientPart;
import com.mytlogos.enterprise.background.api.model.ClientReadEpisode;
import com.mytlogos.enterprise.background.api.model.ClientUpdateUser;
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
import com.mytlogos.enterprise.model.News;
import com.mytlogos.enterprise.model.User;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    public void deleteAllUser() {
        TaskManager.runTask(userDao::deleteAllUser);
    }

    @Override
    public ClientModelPersister getPersister(Repository repository, LoadData unLoadedData, LoadData loadedData) {
        return new RoomModelPersister(unLoadedData, loadedData, repository);
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

    @Override
    public LiveData<List<News>> getNews() {
        return Transformations.map(
                this.newsDao.getNews(),
                input -> input
                        .stream()
                        .map(roomNews -> new News(roomNews.getTitle(), roomNews.getTimeStamp(), roomNews.getNewsId(), roomNews.isRead(), roomNews.getLink()))
                        .collect(Collectors.toList())
        );
    }

    private class RoomModelPersister implements ClientModelPersister {

        private final Collection<com.mytlogos.enterprise.background.ClientConsumer<?>> consumer = new ArrayList<>();
        private final LoadData unLoadedData;
        private final LoadData loadedData;
        private final Repository repository;


        RoomModelPersister(LoadData unLoadedData, LoadData loadedData, Repository repository) {
            this.unLoadedData = unLoadedData;
            this.loadedData = loadedData;
            this.repository = repository;
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
            consumer.add(new ClientConsumer<RoomUser.UserReadTodayJoin>() {
                @Override
                public Class<RoomUser.UserReadTodayJoin> getType() {
                    return RoomUser.UserReadTodayJoin.class;
                }

                @Override
                public void consume(Collection<RoomUser.UserReadTodayJoin> joins) {
                    RoomStorage.this.userDao.addReadToday(joins);
                }
            });
            consumer.add(new ClientConsumer<RoomUser.UserUnReadChapterJoin>() {
                @Override
                public Class<RoomUser.UserUnReadChapterJoin> getType() {
                    return RoomUser.UserUnReadChapterJoin.class;
                }

                @Override
                public void consume(Collection<RoomUser.UserUnReadChapterJoin> joins) {
                    RoomStorage.this.userDao.addUnReadChapter(joins);
                }
            });
            consumer.add(new ClientConsumer<RoomUser.UserUnReadNewsJoin>() {
                @Override
                public Class<RoomUser.UserUnReadNewsJoin> getType() {
                    return RoomUser.UserUnReadNewsJoin.class;
                }

                @Override
                public void consume(Collection<RoomUser.UserUnReadNewsJoin> joins) {
                    RoomStorage.this.userDao.addUnReadNews(joins);
                }
            });
        }

        @Override
        public Collection<com.mytlogos.enterprise.background.ClientConsumer<?>> getConsumer() {
            return consumer;
        }

        @Override
        public ClientModelPersister persistEpisodes(Collection<ClientEpisode> episodes) {
            List<RoomEpisode> list = new ArrayList<>();
            List<RoomEpisode> update = new ArrayList<>();

            LoadWorker worker = this.repository.getLoadWorker();

            for (ClientEpisode episode : episodes) {
                int partId = episode.getPartId();

                if (!this.isPartLoaded(partId)) {
                    worker.addPartTask(partId, episode);
                    continue;
                }
                RoomEpisode roomEpisode = new RoomEpisode(
                        episode.getId(), episode.getProgress(), episode.getReadDate(), partId,
                        episode.getTitle(), episode.getTotalIndex(), episode.getPartialIndex(),
                        episode.getUrl(), episode.getReleaseDate()
                );
                if (this.isEpisodeLoaded(episode.getId())) {
                    update.add(roomEpisode);
                } else {
                    list.add(roomEpisode);
                }
            }
            RoomStorage.this.episodeDao.insertBulk(list);
            RoomStorage.this.episodeDao.updateBulk(update);

            for (RoomEpisode episode : list) {
                this.loadedData.getEpisodes().add(episode.getEpisodeId());
                this.unLoadedData.getEpisodes().remove(episode.getEpisodeId());
            }
            System.out.println("from " + episodes.size() + " persisted: " + list);

            return this;
        }

        @Override
        public ClientModelPersister persistMediaLists(Collection<ClientMediaList> mediaLists) {
            List<RoomMediaList> list = new ArrayList<>();
            List<RoomMediaList> update = new ArrayList<>();
            List<RoomMediaList.MediaListMediaJoin> joins = new ArrayList<>();
            List<Integer> clearListJoin = new ArrayList<>();

            LoadWorker worker = this.repository.getLoadWorker();

            for (ClientMediaList mediaList : mediaLists) {
                RoomMediaList roomMediaList = new RoomMediaList(
                        mediaList.getId(), mediaList.getUserUuid(), mediaList.getName(), mediaList.getMedium()
                );

                if (this.isMediaListLoaded(mediaList.getId())) {
                    update.add(roomMediaList);
                } else {
                    list.add(roomMediaList);
                }


                Set<Integer> missingMedia = new HashSet<>();
                List<RoomMediaList.MediaListMediaJoin> currentListMediaJoin = new ArrayList<>();

                for (int item : mediaList.getItems()) {
                    RoomMediaList.MediaListMediaJoin join = new RoomMediaList
                            .MediaListMediaJoin(mediaList.getId(), item);

                    if (!this.isMediumLoaded(item)) {
                        missingMedia.add(item);
                    }
                    currentListMediaJoin.add(join);
                }

                // if none medium is missing, just clear and add like normal
                if (missingMedia.isEmpty()) {
                    joins.addAll(currentListMediaJoin);
                    clearListJoin.add(mediaList.getId());
                } else {
                    // else load missing media with worker and clear and add afterwards
                    for (Integer mediumId : missingMedia) {
                        worker.addMediumTask(
                                mediumId,
                                currentListMediaJoin,
                                () -> RoomStorage.this.mediaListDao.clearJoin(mediaList.getId())
                        );
                    }
                }
            }
            RoomStorage.this.mediaListDao.insertBulk(list);
            RoomStorage.this.mediaListDao.updateBulk(update);
            // first clear all possible out-of-date joins
            RoomStorage.this.mediaListDao.clearJoins(clearListJoin);
            // then add all up-to-date joins
            RoomStorage.this.mediaListDao.addJoin(joins);

            for (RoomMediaList mediaList : list) {
                this.loadedData.getMediaList().add(mediaList.listId);
                this.unLoadedData.getMediaList().remove(mediaList.listId);
            }
            System.out.println("from " + mediaLists.size() + " persisted: " + list);

            return this;
        }

        @Override
        public ClientModelPersister persistExternalMediaLists(Collection<ClientExternalMediaList> externalMediaLists) {
            List<RoomExternalMediaList> list = new ArrayList<>();
            List<RoomExternalMediaList> update = new ArrayList<>();

            List<RoomExternalMediaList.ExternalListMediaJoin> joins = new ArrayList<>();
            List<Integer> clearListJoin = new ArrayList<>();

            LoadWorker worker = this.repository.getLoadWorker();

            for (ClientExternalMediaList externalMediaList : externalMediaLists) {
                String externalUuid = externalMediaList.getUuid();

                RoomExternalMediaList roomExternalMediaList = new RoomExternalMediaList(
                        externalUuid, externalMediaList.getId(), externalMediaList.getName(),
                        externalMediaList.getMedium(), externalMediaList.getUrl()
                );

                if (!this.isExternalUserLoaded(externalUuid)) {
                    worker.addExtUserTask(externalUuid, externalMediaList);
                    continue;
                }
                if (this.isExternalMediaListLoaded(externalMediaList.getId())) {
                    update.add(roomExternalMediaList);
                } else {
                    list.add(roomExternalMediaList);
                }


                Set<Integer> missingMedia = new HashSet<>();
                List<RoomExternalMediaList.ExternalListMediaJoin> currentExtListMediaJoin = new ArrayList<>();

                for (int item : externalMediaList.getItems()) {
                    RoomExternalMediaList.ExternalListMediaJoin join = new RoomExternalMediaList
                            .ExternalListMediaJoin(externalMediaList.getId(), item);

                    if (!this.isMediumLoaded(item)) {
                        missingMedia.add(item);
                    }
                    currentExtListMediaJoin.add(join);
                }

                // if none medium is missing, just clear and add like normal
                if (missingMedia.isEmpty()) {
                    joins.addAll(currentExtListMediaJoin);
                    clearListJoin.add(externalMediaList.getId());
                } else {
                    // else load missing media with worker and clear and add afterwards
                    for (Integer mediumId : missingMedia) {
                        worker.addMediumTask(
                                mediumId,
                                currentExtListMediaJoin,
                                () -> RoomStorage.this.externalMediaListDao.clearJoin(externalMediaList.getId())
                        );
                    }
                }

            }
            RoomStorage.this.externalMediaListDao.insertBulk(list);
            RoomStorage.this.externalMediaListDao.updateBulk(update);
            // first clear all possible out-of-date joins
            RoomStorage.this.externalMediaListDao.clearJoins(clearListJoin);
            // then add all up-to-date joins
            RoomStorage.this.externalMediaListDao.addJoin(joins);

            for (RoomExternalMediaList mediaList : list) {
                this.loadedData.getExternalMediaList().add(mediaList.externalListId);
                this.unLoadedData.getExternalMediaList().remove(mediaList.externalListId);
            }

            System.out.println("from " + externalMediaLists.size() + " persisted: " + list);

            return this;
        }

        @Override
        public ClientModelPersister persistExternalUsers(Collection<ClientExternalUser> externalUsers) {
            List<RoomExternalUser> list = new ArrayList<>();
            List<RoomExternalUser> update = new ArrayList<>();

            List<RoomExternalMediaList> externalMediaLists = new ArrayList<>();
            List<RoomExternalMediaList> updateExternalMediaLists = new ArrayList<>();

            List<RoomExternalMediaList.ExternalListMediaJoin> extListMediaJoin = new ArrayList<>();
            List<Integer> clearListJoin = new ArrayList<>();

            LoadWorker worker = this.repository.getLoadWorker();

            for (ClientExternalUser externalUser : externalUsers) {
                RoomExternalUser roomExternalUser = new RoomExternalUser(
                        externalUser.getUuid(), externalUser.getLocalUuid(), externalUser.getIdentifier(),
                        externalUser.getType()
                );

                if (this.isExternalUserLoaded(externalUser.getUuid())) {
                    update.add(roomExternalUser);
                } else {
                    list.add(roomExternalUser);
                }

                for (ClientExternalMediaList userList : externalUser.getLists()) {
                    RoomExternalMediaList externalMediaList = new RoomExternalMediaList(
                            userList.getUuid(), userList.getId(), userList.getName(),
                            userList.getMedium(), userList.getUrl());

                    if (this.isExternalMediaListLoaded(userList.getId())) {
                        updateExternalMediaLists.add(externalMediaList);
                    } else {
                        externalMediaLists.add(externalMediaList);
                    }

                    Set<Integer> missingMedia = new HashSet<>();
                    List<RoomExternalMediaList.ExternalListMediaJoin> currentExtListMediaJoin = new ArrayList<>();

                    for (int item : userList.getItems()) {
                        RoomExternalMediaList.ExternalListMediaJoin join =
                                new RoomExternalMediaList.ExternalListMediaJoin(
                                        userList.getId(), item
                                );

                        if (!this.isMediumLoaded(item)) {
                            missingMedia.add(item);
                        }
                        currentExtListMediaJoin.add(join);
                    }
                    // if none medium is missing, just clear and add like normal
                    if (missingMedia.isEmpty()) {
                        extListMediaJoin.addAll(currentExtListMediaJoin);
                        clearListJoin.add(userList.getId());
                    } else {
                        // else load missing media with worker and clear and add afterwards
                        for (Integer mediumId : missingMedia) {
                            worker.addMediumTask(
                                    mediumId,
                                    currentExtListMediaJoin,
                                    () -> RoomStorage.this.externalMediaListDao.clearJoin(userList.getId())
                            );
                        }
                    }
                }
            }
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
                this.unLoadedData.getExternalUser().remove(user.uuid);
            }

            for (RoomExternalMediaList mediaList : externalMediaLists) {
                this.loadedData.getExternalMediaList().add(mediaList.externalListId);
                this.unLoadedData.getExternalMediaList().remove(mediaList.externalListId);
            }
            System.out.println("from " + externalUsers.size() + " persisted: " + list);
            System.out.println("from " + externalMediaLists.size() + " persisted: " + externalMediaLists);


            return this;
        }

        @Override
        public ClientModelPersister persistMedia(Collection<ClientMedium> media) {
            List<RoomMedium> list = new ArrayList<>();
            List<RoomMedium> update = new ArrayList<>();

            LoadWorker worker = this.repository.getLoadWorker();

            for (ClientMedium medium : media) {
                int currentRead = medium.getCurrentRead();
                Integer currentReadObj = this.isEpisodeLoaded(currentRead) ? currentRead : null;

                RoomMedium roomMedium = new RoomMedium(
                        currentReadObj, medium.getId(), medium.getCountryOfOrigin(),
                        medium.getLanguageOfOrigin(), medium.getAuthor(), medium.getTitle(),
                        medium.getMedium(), medium.getArtist(), medium.getLang(),
                        medium.getStateOrigin(), medium.getStateTL(), medium.getSeries(),
                        medium.getUniverse()
                );
                // id can never be zero
                if (!this.isEpisodeLoaded(currentRead) && currentRead > 0) {
                    worker.addEpisodeTask(currentRead, medium);
                }
                if (currentRead == 0 || this.isEpisodeLoaded(currentRead) || worker.isEpisodeLoading(currentRead)) {
                    if (this.isMediumLoaded(medium.getId())) {
                        update.add(roomMedium);
                    } else {
                        list.add(roomMedium);
                    }
                }
            }
            RoomStorage.this.mediumDao.insertBulk(list);
            RoomStorage.this.mediumDao.updateBulk(update);

            for (RoomMedium medium : list) {
                this.loadedData.getMedia().add(medium.getMediumId());
                this.unLoadedData.getMedia().remove(medium.getMediumId());
            }
            System.out.println("from " + media.size() + " persisted: " + list);

            return this;
        }

        @Override
        public ClientModelPersister persistNews(Collection<ClientNews> news) {
            List<RoomNews> list = new ArrayList<>();

            for (ClientNews clientNews : news) {
                if (this.isNewsLoaded(clientNews.getId())) {
                    continue;
                }
                RoomNews roomNews = new RoomNews(
                        clientNews.getId(), clientNews.isRead(),
                        clientNews.getTitle(), clientNews.getDate(),
                        clientNews.getLink()
                );
                if (!this.isNewsLoaded(clientNews.getId())) {
                    list.add(roomNews);
                }
            }
            RoomStorage.this.newsDao.insertNews(list);

            for (RoomNews roomNews : list) {
                this.loadedData.getNews().add(roomNews.getNewsId());
                this.unLoadedData.getNews().remove(roomNews.getNewsId());
            }
            System.out.println("from " + news.size() + " persisted: " + list);

            return this;
        }

        @Override
        public ClientModelPersister persistParts(Collection<ClientPart> parts) {
            List<RoomPart> list = new ArrayList<>();
            List<RoomPart> update = new ArrayList<>();
            List<ClientEpisode> episodes = new ArrayList<>();

            LoadWorker worker = this.repository.getLoadWorker();

            for (ClientPart part : parts) {
                RoomPart roomPart = new RoomPart(
                        part.getId(), part.getMediumId(), part.getTitle(), part.getTotalIndex(),
                        part.getPartialIndex()
                );
                if (this.isMediumLoaded(part.getMediumId())) {
                    if (this.isPartLoaded(part.getId())) {
                        update.add(roomPart);
                    } else {
                        list.add(roomPart);
                    }
                    episodes.addAll(Arrays.asList(part.getEpisodes()));
                } else {
                    worker.addMediumTask(part.getMediumId(), part);
                }
            }
            RoomStorage.this.partDao.insertBulk(list);
            RoomStorage.this.partDao.updateBulk(update);

            for (RoomPart part : list) {
                this.loadedData.getPart().add(part.getPartId());
                this.unLoadedData.getPart().remove(part.getPartId());
            }
            System.out.println("from " + episodes.size() + " persisted: " + list);
            this.persistEpisodes(episodes);

            return this;
        }

        @Override
        public ClientModelPersister persistReadEpisodes(Collection<ClientReadEpisode> readEpisodes) {
            LoadWorker worker = this.repository.getLoadWorker();

            for (ClientReadEpisode readEpisode : readEpisodes) {
                int episodeId = readEpisode.getEpisodeId();

                if (this.isEpisodeLoaded(episodeId)) {
                    RoomStorage.this.episodeDao.update(
                            episodeId,
                            readEpisode.getProgress(),
                            readEpisode.getReadDate()
                    );
                } else {
                    worker.addEpisodeTask(episodeId, readEpisode);
                }
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
        public ClientModelPersister persist(ClientUpdateUser user) {
            User value = RoomStorage.this.storageUserLiveData.getValue();
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
        public ClientModelPersister persist(ClientUser clientUser) {
            // short cut version
            if (clientUser == null) {
                RoomStorage.this.deleteAllUser();
                return this;
            }

            List<RoomUser.UserUnReadChapterJoin> unreadChapter = new ArrayList<>();

            // fixme maybe deferReadTodayJoin is unnecessary, just query all media read today
            LoadWorker worker = this.repository.getLoadWorker();

            for (int clientReadChapter : clientUser.getUnreadChapter()) {
                RoomUser.UserUnReadChapterJoin userUnReadChapterJoin = new RoomUser.UserUnReadChapterJoin(
                        clientUser.getUuid(),
                        clientReadChapter
                );
                if (this.isEpisodeLoaded(clientReadChapter)) {
                    unreadChapter.add(userUnReadChapterJoin);
                } else {
                    worker.addEpisodeTask(clientReadChapter, userUnReadChapterJoin);
                }
            }

            List<RoomUser.UserUnReadNewsJoin> unreadNews = new ArrayList<>();

            for (ClientNews clientNews : clientUser.getUnreadNews()) {
                int id = clientNews.getId();

                RoomUser.UserUnReadNewsJoin userUnReadNewsJoin = new RoomUser.UserUnReadNewsJoin(
                        clientUser.getUuid(), id
                );
                if (this.isNewsLoaded(id)) {
                    unreadNews.add(userUnReadNewsJoin);
                } else {
                    worker.addNewsTask(id, userUnReadNewsJoin);
                }

            }

            List<RoomUser.UserReadTodayJoin> readToday = new ArrayList<>();

            for (ClientReadEpisode clientReadEpisode : clientUser.getReadToday()) {
                int episodeId = clientReadEpisode.getEpisodeId();

                RoomUser.UserReadTodayJoin userReadTodayJoin = new RoomUser.UserReadTodayJoin(
                        clientUser.getUuid(),
                        episodeId
                );
                if (this.isEpisodeLoaded(episodeId)) {
                    readToday.add(userReadTodayJoin);
                } else {
                    worker.addEpisodeTask(episodeId, userReadTodayJoin);
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

            User value = RoomStorage.this.userDao.getCurrentUser();

            if (value != null && roomUser.getUuid().equals(value.getUuid())) {
                // update user, so previous one wont be deleted
                RoomStorage.this.userDao.update(roomUser);
            } else {
                RoomStorage.this.userDao.deleteAllUser();
                // persist user
                RoomStorage.this.userDao.insert(roomUser);
            }

            // persist lists
            this.persist(clientUser.getLists());
            // persist externalUser
            this.persist(clientUser.getExternalUser());
            // persist loaded unread News
            this.persist(clientUser.getUnreadNews());
            // persist/update media with data
            this.persist(clientUser.getReadToday());
            // persist readToday join
            RoomStorage.this.userDao.addReadToday(readToday);
            // persist unReadChapter join
            RoomStorage.this.userDao.addUnReadChapter(unreadChapter);
            // persist unReadNews join
            RoomStorage.this.userDao.addUnReadNews(unreadNews);

            user.getExternalUser().addAll(extUser);
            user.getMediaList().addAll(lists);
            System.out.println("persisted: " + clientUser);

            return this;
        }

        private boolean isEpisodeLoaded(int id) {
            return this.loadedData.getEpisodes().contains(id);
        }

        private boolean isPartLoaded(int id) {
            return this.loadedData.getPart().contains(id);
        }

        private boolean isMediumLoaded(int id) {
            return this.loadedData.getMedia().contains(id);
        }

        private boolean isMediaListLoaded(int id) {
            return this.loadedData.getMediaList().contains(id);
        }

        private boolean isExternalMediaListLoaded(int id) {
            return this.loadedData.getExternalMediaList().contains(id);
        }

        private boolean isExternalUserLoaded(String uuid) {
            return this.loadedData.getExternalUser().contains(uuid);
        }

        private boolean isNewsLoaded(int id) {
            return this.loadedData.getNews().contains(id);
        }


        @Override
        public void finish() {
            this.repository.getLoadWorker().work();
        }
    }
}
