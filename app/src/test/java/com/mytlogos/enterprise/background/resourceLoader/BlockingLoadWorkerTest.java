package com.mytlogos.enterprise.background.resourceLoader;

import com.mytlogos.enterprise.DataGenerator;
import com.mytlogos.enterprise.Utils;
import com.mytlogos.enterprise.background.DummyClientModelPersister;
import com.mytlogos.enterprise.background.DummyDependantGenerator;
import com.mytlogos.enterprise.background.DummyRepository;
import com.mytlogos.enterprise.background.LoadData;
import com.mytlogos.enterprise.background.api.model.ClientEpisode;
import com.mytlogos.enterprise.background.api.model.ClientExternalMediaList;
import com.mytlogos.enterprise.background.api.model.ClientExternalUser;
import com.mytlogos.enterprise.background.api.model.ClientMediaList;
import com.mytlogos.enterprise.background.api.model.ClientMedium;
import com.mytlogos.enterprise.background.api.model.ClientNews;
import com.mytlogos.enterprise.background.api.model.ClientPart;
import com.mytlogos.enterprise.background.api.model.ClientReadEpisode;
import com.mytlogos.enterprise.background.api.model.ClientUser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockingLoadWorkerTest {
    private DummyNewsLoader NEWS_LOADER;
    private DummyEpisodeLoader EPISODE_LOADER;
    private DummyPartLoader PART_LOADER;
    private DummyMediumLoader MEDIUM_LOADER;
    private DummyMediaListLoader MEDIALIST_LOADER;
    private DummyExtMediaListLoader EXTERNAL_MEDIALIST_LOADER;
    private DummyExtUserLoader EXTERNAL_USER_LOADER;
    private BlockingLoadWorker loadWorker;
    private LoadData loadedData;
    private DummyClientModelPersister persister;
    private DummyRepository repository;
    private ClientUser clientUser;
    private List<ClientPart> parts;
    private List<ClientEpisode> episodes;
    private List<ClientMediaList> mediaLists;
    private List<ClientExternalUser> externalUser;
    private List<ClientMedium> media;
    private List<ClientNews> news;
    private List<ClientReadEpisode> readEpisodes;
    private List<ClientExternalMediaList> extMediaLists;
    private DummyDependantGenerator dependantGenerator;

    @BeforeEach
    void setUp() {
        System.out.println("setting up");
        DataGenerator dataGenerator = new DataGenerator();
        dataGenerator.generateCompleteTestData();

        this.externalUser = dataGenerator.getExternalUser();
        this.mediaLists = dataGenerator.getMediaLists();
        this.extMediaLists = dataGenerator.getExtMediaLists();
        this.episodes = dataGenerator.getEpisodes();
        this.parts = dataGenerator.getParts();
        this.media = dataGenerator.getMedia();
        this.news = dataGenerator.getNews();
        this.readEpisodes = dataGenerator.getReadEpisodes();
        this.clientUser = dataGenerator.getClientUser();

        this.loadedData = new LoadData();
        this.repository = new DummyRepository();
        this.persister = new DummyClientModelPersister(this.loadedData, repository);

        this.NEWS_LOADER = new DummyNewsLoader(this.news);
        this.EPISODE_LOADER = new DummyEpisodeLoader(this.episodes);
        this.PART_LOADER = new DummyPartLoader(this.parts);
        this.MEDIUM_LOADER = new DummyMediumLoader(this.media);
        this.MEDIALIST_LOADER = new DummyMediaListLoader(this.mediaLists, this.media);
        this.EXTERNAL_MEDIALIST_LOADER = new DummyExtMediaListLoader(this.extMediaLists);
        this.EXTERNAL_USER_LOADER = new DummyExtUserLoader(this.externalUser);

        this.dependantGenerator = new DummyDependantGenerator(this.loadedData);
        this.loadWorker = new BlockingLoadWorker(
                this.repository,
                this.persister,
                this.loadedData,
                this.EXTERNAL_USER_LOADER,
                this.EXTERNAL_MEDIALIST_LOADER,
                this.MEDIALIST_LOADER,
                this.MEDIUM_LOADER,
                this.PART_LOADER,
                this.EPISODE_LOADER,
                this.NEWS_LOADER,
                this.dependantGenerator
        );

        this.repository.setLoadWorker(this.loadWorker);
        this.NEWS_LOADER.setLoadWorker(this.loadWorker);
        this.EPISODE_LOADER.setLoadWorker(this.loadWorker);
        this.PART_LOADER.setLoadWorker(this.loadWorker);
        this.MEDIUM_LOADER.setLoadWorker(this.loadWorker);
        this.MEDIALIST_LOADER.setLoadWorker(this.loadWorker);
        this.EXTERNAL_MEDIALIST_LOADER.setLoadWorker(this.loadWorker);
        this.EXTERNAL_USER_LOADER.setLoadWorker(this.loadWorker);

        this.generateLocalTestData();
    }

    private void generateLocalTestData() {

    }

    private int[] toIntArray(Collection<Integer> integers) {
        int[] ints = new int[integers.size()];
        int index = 0;
        for (Integer integer : integers) {
            ints[index] = integer;
            index++;
        }
        return ints;
    }

    @Test
    void addIntegerIdTask() {
        this.episodes.forEach(episode -> testInteger(episode.getId(), this.EPISODE_LOADER, LoadWorker::isEpisodeLoading));
        this.media.forEach(episode -> testInteger(episode.getId(), this.MEDIUM_LOADER, LoadWorker::isMediumLoading));
        this.extMediaLists.forEach(episode -> testInteger(episode.getId(), this.EXTERNAL_MEDIALIST_LOADER, LoadWorker::isExternalMediaListLoading));
        this.mediaLists.forEach(episode -> testInteger(episode.getId(), this.MEDIALIST_LOADER, LoadWorker::isMediaListLoading));
        this.news.forEach(episode -> testInteger(episode.getId(), this.NEWS_LOADER, LoadWorker::isNewsLoading));
        this.parts.forEach(episode -> testInteger(episode.getId(), this.PART_LOADER, LoadWorker::isPartLoading));
    }

    private void testInteger(Integer id, NetworkLoader<Integer> loader, BiFunction<LoadWorker, Integer, Boolean> function) {
        this.loadWorker.addIntegerIdTask(id, null, loader);
        assertTrue(function.apply(this.loadWorker, id));
    }

    @Test
    void addStringIdTask() {
        List<String> uuids = this.externalUser.stream().map(ClientExternalUser::getUuid).collect(Collectors.toList());

        for (String uuid : uuids) {
            this.loadWorker.addStringIdTask(uuid, null, this.EXTERNAL_USER_LOADER);
            assertTrue(this.loadWorker.isExternalUserLoading(uuid));
        }
    }

    @Test
    void doWork() {
        assertTrue(this.clientUser.getExternalUser().length > 0);
        assertTrue(this.clientUser.getLists().length > 0);

        this.persister.persist(this.clientUser);
        this.loadWorker.doWork();

        assertTrue(this.persister.isUserInserted());

        for (ClientExternalUser user : this.clientUser.getExternalUser()) {
            assertTrue(this.loadedData.getExternalUser().contains(user.getUuid()));
            assertTrue(!this.loadWorker.isExternalUserLoading(user.getUuid()));

            for (ClientExternalMediaList list : user.getLists()) {
                assertTrue(this.loadedData.getExternalMediaList().contains(list.getId()));
                assertTrue(!this.loadWorker.isExternalMediaListLoading(list.getId()));

                for (int item : list.getItems()) {
                    this.assertMediumDown(item);
                }
            }
        }

        for (ClientMediaList list : this.clientUser.getLists()) {
            assertTrue(this.loadedData.getMediaList().contains(list.getId()));
            assertTrue(!this.loadWorker.isMediaListLoading(list.getId()));

            for (int item : list.getItems()) {
                this.assertMediumDown(item);
            }
        }

        for (ClientReadEpisode episode : this.clientUser.getReadToday()) {
            this.assertEpisodeUp(episode.getEpisodeId());
        }

        for (int id : this.clientUser.getUnreadChapter()) {
            this.assertEpisodeUp(id);
        }

        for (ClientNews clientNews : this.clientUser.getUnreadNews()) {
            assertTrue(this.loadedData.getNews().contains(clientNews.getId()));
            assertTrue(!this.loadWorker.isNewsLoading(clientNews.getId()));
        }
    }

    @Test
    void doWorkPersistParts() {
        int steps = (this.parts.size() / this.media.size()) / 2;
        Collection<ClientPart> parts = new ArrayList<>();

        for (int i = 0; i < this.parts.size(); i += steps) {
            parts.add(this.parts.get(i));
        }
        this.persister.persistParts(parts);

        for (ClientPart part : parts) {
            if (!this.loadedData.getMedia().contains(part.getMediumId())) {
                Assertions.assertTrue(this.loadWorker.isMediumLoading(part.getMediumId()));
            }
        }

        this.loadWorker.doWork();

        for (ClientPart part : parts) {
            this.assertPartUp(part.getId());
            this.assertPartDown(part.getId());
        }
    }

    @Test
    void doWorkPersistMedia() {
        Collection<ClientMedium> media = this
                .media
                .stream()
                .map(clientMedium -> BlockingLoadWorkerTest.cloneObject(clientMedium, Collections.singletonList("currentRead")))
                .collect(Collectors.toList());

        this.persister.persistMedia(media);

        for (ClientMedium medium : media) {
            if (!this.loadedData.getMedia().contains(medium.getId())) {
                Assertions.assertTrue(this.loadWorker.isMediumLoading(medium.getId()));
            }
        }

        this.loadWorker.doWork();

        for (ClientMedium medium : media) {
            this.assertMedium(medium.getId());
        }
    }

    @Test
    void work() {
        assertTrue(this.clientUser.getExternalUser().length > 0);
        assertTrue(this.clientUser.getLists().length > 0);

        this.persister.persist(this.clientUser);
        this.loadWorker.work();

        assertTrue(this.persister.isUserInserted());

        for (ClientExternalUser user : this.clientUser.getExternalUser()) {
            assertTrue(this.loadedData.getExternalUser().contains(user.getUuid()));
            assertTrue(!this.loadWorker.isExternalUserLoading(user.getUuid()));

            for (ClientExternalMediaList list : user.getLists()) {
                assertTrue(this.loadedData.getExternalMediaList().contains(list.getId()));
                assertTrue(!this.loadWorker.isExternalMediaListLoading(list.getId()));

                for (int item : list.getItems()) {
                    this.assertMediumDown(item);
                }
            }
        }

        for (ClientMediaList list : this.clientUser.getLists()) {
            assertTrue(this.loadedData.getMediaList().contains(list.getId()));
            assertTrue(!this.loadWorker.isMediaListLoading(list.getId()));

            for (int item : list.getItems()) {
                this.assertMediumDown(item);
            }
        }

        for (ClientReadEpisode episode : this.clientUser.getReadToday()) {
            this.assertEpisodeUp(episode.getEpisodeId());
        }

        for (int id : this.clientUser.getUnreadChapter()) {
            this.assertEpisodeUp(id);
        }

        for (ClientNews clientNews : this.clientUser.getUnreadNews()) {
            assertTrue(this.loadedData.getNews().contains(clientNews.getId()));
            assertTrue(!this.loadWorker.isNewsLoading(clientNews.getId()));
        }
    }

    private static <T> T cloneObject(T obj, List<String> skipFields) {
        try {
            Object clone = obj.getClass().newInstance();
            for (Field field : obj.getClass().getDeclaredFields()) {
                if (skipFields.contains(field.getName())) {
                    continue;
                }
                field.setAccessible(true);
                field.set(clone, field.get(obj));
            }
            //noinspection unchecked
            return (T) clone;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void assertEpisode(int id) {
        assertTrue(this.loadedData.getEpisodes().contains(id));
        assertTrue(!this.loadWorker.isEpisodeLoading(id));
    }

    private void assertEpisodeUp(int id) {
        this.assertEpisode(id);
        this.assertPartUp(Utils.getEpisode(this.episodes, id).getPartId());
    }

    private void assertPartUp(int id) {
        this.assertPart(id);
        this.assertMedium(Utils.getPart(this.parts, id).getMediumId());
    }

    private void assertPart(int id) {
        assertTrue(!this.loadWorker.isPartLoading(id));
        assertTrue(this.loadedData.getPart().contains(id));
    }

    private void assertMedium(int id) {
        assertTrue(this.loadedData.getMedia().contains(id));
        assertTrue(!this.loadWorker.isMediumLoading(id));
    }

    private void assertMediumDown(int id) {
        this.assertMedium(id);

        for (int part : Utils.getMedium(this.media, id).getParts()) {
            this.assertPartDown(part);
        }
    }

    private void assertPartDown(int id) {
        this.assertPart(id);

        for (ClientEpisode episode : Utils.getPart(this.parts, id).getEpisodes()) {
            assertTrue(this.loadedData.getEpisodes().contains(episode.getId()));
            assertTrue(!this.loadWorker.isEpisodeLoading(episode.getId()));
        }
    }
}