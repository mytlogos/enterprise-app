package com.mytlogos.enterprise.background;

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
import com.mytlogos.enterprise.background.api.model.ClientRelease;
import com.mytlogos.enterprise.background.api.model.ClientSimpleRelease;
import com.mytlogos.enterprise.background.api.model.ClientSimpleUser;
import com.mytlogos.enterprise.background.api.model.ClientStat;
import com.mytlogos.enterprise.background.api.model.ClientUpdateUser;
import com.mytlogos.enterprise.background.api.model.ClientUser;
import com.mytlogos.enterprise.background.resourceLoader.DependantValue;
import com.mytlogos.enterprise.background.resourceLoader.LoadWorkGenerator;
import com.mytlogos.enterprise.background.resourceLoader.LoadWorker;
import com.mytlogos.enterprise.background.room.model.RoomEpisode;
import com.mytlogos.enterprise.background.room.model.RoomExternalMediaList;
import com.mytlogos.enterprise.background.room.model.RoomExternalUser;
import com.mytlogos.enterprise.background.room.model.RoomMediaList;
import com.mytlogos.enterprise.background.room.model.RoomMedium;
import com.mytlogos.enterprise.background.room.model.RoomNews;
import com.mytlogos.enterprise.background.room.model.RoomPart;
import com.mytlogos.enterprise.background.room.model.RoomToDownload;
import com.mytlogos.enterprise.model.ToDownload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DummyClientModelPersister implements ClientModelPersister {
    private final Collection<ClientConsumer<?>> consumer = new ArrayList<>();
    private final LoadData loadedData;
    private final LoadData updatedData;
    private final LoadData deletedData;
    private final Repository repository;
    private final LoadWorkGenerator generator;
    private boolean userUpdated = false;
    private boolean userDeleted = false;
    private boolean userInserted = false;
    private List<RoomExternalMediaList.ExternalListMediaJoin> externalListMediaJoins = new ArrayList<>();
    private List<RoomExternalMediaList.ExternalListMediaJoin> clearedExternalListMediaJoins = new ArrayList<>();
    private List<Integer> clearedExternalList = new ArrayList<>();
    private List<RoomMediaList.MediaListMediaJoin> listMediaJoins = new ArrayList<>();
    private List<RoomMediaList.MediaListMediaJoin> clearedListMediaJoins = new ArrayList<>();
    private List<Integer> clearedListMedia = new ArrayList<>();
    private List<ClientRelease> savedReleases = new ArrayList<>();
    private List<RoomToDownload> savedToDownloads = new ArrayList<>();

    public DummyClientModelPersister(LoadData loadedData, Repository repository) {
        this.loadedData = loadedData;
        this.repository = repository;
        this.generator = new LoadWorkGenerator(loadedData);
        this.initConsumer();
        this.updatedData = new LoadData();
        this.deletedData = new LoadData();
    }

    private void initConsumer() {
        consumer.add(new ClientConsumer<ClientReadEpisode>() {
            @Override
            public Class<ClientReadEpisode> getType() {
                return ClientReadEpisode.class;
            }

            @Override
            public void consume(Collection<ClientReadEpisode> clientEpisodes) {
                DummyClientModelPersister.this.persistReadEpisodes(clientEpisodes);
            }
        });
        consumer.add(new ClientConsumer<ClientEpisode>() {
            @Override
            public Class<ClientEpisode> getType() {
                return ClientEpisode.class;
            }

            @Override
            public void consume(Collection<ClientEpisode> clientEpisodes) {
                DummyClientModelPersister.this.persistEpisodes(clientEpisodes);
            }
        });
        consumer.add(new ClientConsumer<ClientPart>() {
            @Override
            public Class<ClientPart> getType() {
                return ClientPart.class;
            }

            @Override
            public void consume(Collection<ClientPart> parts) {
                DummyClientModelPersister.this.persistParts(parts);
            }
        });
        consumer.add(new ClientConsumer<ClientMedium>() {
            @Override
            public Class<ClientMedium> getType() {
                return ClientMedium.class;
            }

            @Override
            public void consume(Collection<ClientMedium> media) {
                DummyClientModelPersister.this.persistMedia(media);
            }
        });
        consumer.add(new ClientConsumer<RoomMediaList.MediaListMediaJoin>() {
            @Override
            public Class<RoomMediaList.MediaListMediaJoin> getType() {
                return RoomMediaList.MediaListMediaJoin.class;
            }

            @Override
            public void consume(Collection<RoomMediaList.MediaListMediaJoin> joins) {
                DummyClientModelPersister.this.listMediaJoins.addAll(joins);
            }
        });
        consumer.add(new ClientConsumer<RoomExternalMediaList.ExternalListMediaJoin>() {
            @Override
            public Class<RoomExternalMediaList.ExternalListMediaJoin> getType() {
                return RoomExternalMediaList.ExternalListMediaJoin.class;
            }

            @Override
            public void consume(Collection<RoomExternalMediaList.ExternalListMediaJoin> joins) {
                DummyClientModelPersister.this.externalListMediaJoins.addAll(joins);
            }
        });
        consumer.add(new ClientConsumer<ClientExternalMediaList>() {
            @Override
            public Class<ClientExternalMediaList> getType() {
                return ClientExternalMediaList.class;
            }

            @Override
            public void consume(Collection<ClientExternalMediaList> extLists) {
                DummyClientModelPersister.this.persistExternalMediaLists(extLists);
            }
        });
        consumer.add(new ClientConsumer<ClientMediaList>() {
            @Override
            public Class<ClientMediaList> getType() {
                return ClientMediaList.class;
            }

            @Override
            public void consume(Collection<ClientMediaList> lists) {
                DummyClientModelPersister.this.persistMediaLists(lists);
            }
        });
        consumer.add(new ClientConsumer<ClientExternalUser>() {
            @Override
            public Class<ClientExternalUser> getType() {
                return ClientExternalUser.class;
            }

            @Override
            public void consume(Collection<ClientExternalUser> extUsers) {
                DummyClientModelPersister.this.persistExternalUsers(extUsers);
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
        return this.persist(filteredEpisodes);
    }

    @Override
    public ClientModelPersister persistReleases(Collection<ClientRelease> releases) {
        return null;
    }

    @Override
    public ClientModelPersister persist(LoadWorkGenerator.FilteredEpisodes filteredEpisodes) {
        RoomConverter converter = new RoomConverter(this.loadedData);

        List<RoomEpisode> list = converter.convertEpisodes(filteredEpisodes.newEpisodes);
        List<RoomEpisode> update = converter.convertEpisodes(filteredEpisodes.updateEpisodes);

        for (RoomEpisode episode : list) {
            this.loadedData.getEpisodes().add(episode.getEpisodeId());
        }
        for (RoomEpisode episode : update) {
            this.updatedData.getEpisodes().add(episode.getEpisodeId());
        }
        this.savedReleases.addAll(filteredEpisodes.releases);
        return this;
    }

    @Override
    public ClientModelPersister persistMediaLists(Collection<ClientMediaList> mediaLists) {
        RoomConverter converter = new RoomConverter(this.loadedData);
        LoadWorkGenerator.FilteredMediaList filteredMediaList = this.generator.filterMediaLists(mediaLists);

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
                            () -> this.clearedListMedia.add(listId)
                    ),
                    worker.MEDIUM_LOADER
            );
        }

        return this.persist(filteredMediaList);
    }

    @Override
    public ClientModelPersister persist(LoadWorkGenerator.FilteredMediaList filteredMediaList) {
        RoomConverter converter = new RoomConverter(this.loadedData);

        List<RoomMediaList> list = converter.convertMediaList(filteredMediaList.newList);
        List<RoomMediaList> update = converter.convertMediaList(filteredMediaList.updateList);
        List<RoomMediaList.MediaListMediaJoin> joins = converter.convertListJoin(filteredMediaList.joins);
        List<Integer> clearListJoin = filteredMediaList.clearJoins;


        for (RoomMediaList mediaList : update) {
            this.updatedData.getMediaList().add(mediaList.listId);
        }
        this.clearedListMedia.addAll(clearListJoin);
        // then add all up-to-date joins
        this.listMediaJoins.addAll(joins);

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
                            () -> this.clearedExternalList.add(listId)
                    ),
                    worker.MEDIUM_LOADER
            );
        }
        return this.persist(filteredExtMediaList);
    }

    @Override
    public ClientModelPersister persist(LoadWorkGenerator.FilteredExtMediaList filteredExtMediaList) {
        RoomConverter converter = new RoomConverter(this.loadedData);

        List<RoomExternalMediaList> list = converter.convertExternalMediaList(filteredExtMediaList.newList);
        List<RoomExternalMediaList> update = converter.convertExternalMediaList(filteredExtMediaList.updateList);

        List<RoomExternalMediaList.ExternalListMediaJoin> joins = converter.convertExListJoin(filteredExtMediaList.joins);
        List<Integer> clearListJoin = filteredExtMediaList.clearJoins;

        this.clearedExternalList.addAll(clearListJoin);
        this.externalListMediaJoins.addAll(joins);

        for (RoomExternalMediaList mediaList : list) {
            this.loadedData.getExternalMediaList().add(mediaList.externalListId);
        }
        for (RoomExternalMediaList mediaList : update) {
            this.updatedData.getExternalMediaList().add(mediaList.externalListId);
        }
        return this;
    }

    @Override
    public ClientModelPersister persistExternalUsers(Collection<ClientExternalUser> externalUsers) {
        LoadWorkGenerator.FilteredExternalUser filteredExternalUser = this.generator.filterExternalUsers(externalUsers);

        RoomConverter converter = new RoomConverter(this.loadedData);
        LoadWorker worker = this.repository.getLoadWorker();

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
                            () -> this.clearedExternalList.add(listId)
                    ),
                    worker.MEDIUM_LOADER
            );
        }
        return this.persist(filteredExternalUser);
    }

    @Override
    public ClientModelPersister persist(LoadWorkGenerator.FilteredExternalUser filteredExternalUser) {
        RoomConverter converter = new RoomConverter(this.loadedData);

        List<RoomExternalUser> list = converter.convertExternalUser(filteredExternalUser.newUser);
        List<RoomExternalUser> update = converter.convertExternalUser(filteredExternalUser.updateUser);

        List<RoomExternalMediaList> externalMediaLists = converter.convertExternalMediaList(filteredExternalUser.newList);
        List<RoomExternalMediaList> updateExternalMediaLists = converter.convertExternalMediaList(filteredExternalUser.updateList);

        List<RoomExternalMediaList.ExternalListMediaJoin> extListMediaJoin = converter.convertExListJoin(filteredExternalUser.joins);
        List<Integer> clearListJoin = filteredExternalUser.clearJoins;

        this.clearedExternalList.addAll(clearListJoin);
        this.externalListMediaJoins.addAll(extListMediaJoin);

        for (RoomExternalUser user : list) {
            this.loadedData.getExternalUser().add(user.uuid);
        }
        for (RoomExternalUser user : update) {
            this.updatedData.getExternalUser().add(user.uuid);
        }

        for (RoomExternalMediaList mediaList : externalMediaLists) {
            this.loadedData.getExternalMediaList().add(mediaList.externalListId);
        }
        for (RoomExternalMediaList mediaList : updateExternalMediaLists) {
            this.updatedData.getExternalMediaList().add(mediaList.externalListId);
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
        return this.persist(filteredMedia);
    }

    @Override
    public ClientModelPersister persist(LoadWorkGenerator.FilteredMedia filteredMedia) {
        RoomConverter converter = new RoomConverter(this.loadedData);

        List<RoomMedium> list = converter.convertMedia(filteredMedia.newMedia);
        List<RoomMedium> update = converter.convertMedia(filteredMedia.updateMedia);

        for (RoomMedium medium : list) {
            this.loadedData.getMedia().add(medium.getMediumId());
        }
        for (RoomMedium medium : update) {
            this.updatedData.getMedia().add(medium.getMediumId());
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
        for (RoomNews roomNews : list) {
            this.loadedData.getNews().add(roomNews.getNewsId());
        }
        for (RoomNews roomNews : update) {
            this.updatedData.getNews().add(roomNews.getNewsId());
        }
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
        return this.persist(filteredParts);
    }

    @Override
    public ClientModelPersister persist(LoadWorkGenerator.FilteredParts filteredParts) {
        RoomConverter converter = new RoomConverter();

        List<RoomPart> list = converter.convertParts(filteredParts.newParts);
        List<RoomPart> update = converter.convertParts(filteredParts.updateParts);

        for (RoomPart part : list) {
            this.loadedData.getPart().add(part.getPartId());
        }
        for (RoomPart part : update) {
            this.updatedData.getPart().add(part.getPartId());
        }
        this.persistEpisodes(filteredParts.episodes);
        return this;
    }

    @Override
    public ClientModelPersister persist(LoadWorkGenerator.FilteredReadEpisodes filteredReadEpisodes) {
        for (ClientReadEpisode readEpisode : filteredReadEpisodes.episodeList) {
            this.updatedData.getEpisodes().add(readEpisode.getEpisodeId());
        }
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
        for (ClientReadEpisode readEpisode : filteredReadEpisodes.episodeList) {
            this.updatedData.getEpisodes().add(readEpisode.getEpisodeId());
        }
        return this;
    }

    @Override
    public ClientModelPersister persist(ClientStat.ParsedStat stat) {
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
        this.savedToDownloads.addAll(roomToDownloads);
        return this;
    }

    @Override
    public ClientModelPersister persist(ClientUpdateUser user) {
        this.userUpdated = true;
        return this;
    }

    @Override
    public ClientModelPersister persist(ToDownload toDownload) {
        this.savedToDownloads.add(new RoomConverter().convert(toDownload));
        return this;
    }

    @Override
    public void persistMediaInWait(List<ClientMediumInWait> medium) {

    }

    @Override
    public ClientModelPersister persist(ClientSimpleUser user) {
        return null;
    }

    @Override
    public void deleteLeftoverEpisodes(Map<Integer, List<Integer>> partEpisodes) {

    }

    @Override
    public void deleteLeftoverReleases(Map<Integer, List<ClientSimpleRelease>> partReleases) {

    }

    @Override
    public ClientModelPersister persist(ClientUser clientUser) {
        // short cut version
        if (clientUser == null) {
            this.userDeleted = true;
            return this;
        }

        LoadWorker worker = this.repository.getLoadWorker();

        for (int clientReadChapter : clientUser.getUnreadChapter()) {
            if (!this.generator.isEpisodeLoaded(clientReadChapter)) {
                worker.addIntegerIdTask(clientReadChapter, null, worker.EPISODE_LOADER);
            }
        }

        this.userInserted = true;

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

    public LoadData getLoadedData() {
        return loadedData;
    }

    public LoadData getUpdatedData() {
        return updatedData;
    }

    public LoadData getDeletedData() {
        return deletedData;
    }

    public Repository getRepository() {
        return repository;
    }

    public LoadWorkGenerator getGenerator() {
        return generator;
    }

    public boolean isUserUpdated() {
        return userUpdated;
    }

    public boolean isUserDeleted() {
        return userDeleted;
    }

    public boolean isUserInserted() {
        return userInserted;
    }

    public List<RoomExternalMediaList.ExternalListMediaJoin> getExternalListMediaJoins() {
        return externalListMediaJoins;
    }

    public List<RoomExternalMediaList.ExternalListMediaJoin> getClearedExternalListMediaJoins() {
        return clearedExternalListMediaJoins;
    }

    public List<Integer> getClearedExternalList() {
        return clearedExternalList;
    }

    public List<RoomMediaList.MediaListMediaJoin> getListMediaJoins() {
        return listMediaJoins;
    }

    public List<RoomMediaList.MediaListMediaJoin> getClearedListMediaJoins() {
        return clearedListMediaJoins;
    }

    public List<Integer> getClearedListMedia() {
        return clearedListMedia;
    }

    public List<ClientRelease> getSavedReleases() {
        return savedReleases;
    }

    public List<RoomToDownload> getSavedToDownloads() {
        return savedToDownloads;
    }
}
