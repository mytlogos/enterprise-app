package com.mytlogos.enterprise;

import com.mytlogos.enterprise.background.api.model.ClientEpisode;
import com.mytlogos.enterprise.background.api.model.ClientExternalMediaList;
import com.mytlogos.enterprise.background.api.model.ClientExternalUser;
import com.mytlogos.enterprise.background.api.model.ClientMediaList;
import com.mytlogos.enterprise.background.api.model.ClientMedium;
import com.mytlogos.enterprise.background.api.model.ClientNews;
import com.mytlogos.enterprise.background.api.model.ClientPart;
import com.mytlogos.enterprise.background.api.model.ClientReadEpisode;
import com.mytlogos.enterprise.background.api.model.ClientRelease;
import com.mytlogos.enterprise.background.api.model.ClientUser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataGeneratorTest {

    private DataGenerator generator;

    @BeforeEach
    void setUp() {
        this.generator = new DataGenerator();
        this.generator.generateCompleteTestData();
    }

    @Test
    void getClientUser() {
        ClientUser user = this.generator.getClientUser();
        Assertions.assertNotNull(user);
        Assertions.assertNotNull(user.getUnreadNews());
        Assertions.assertNotNull(user.getLists());
        Assertions.assertNotNull(user.getReadToday());
        Assertions.assertNotNull(user.getUnreadChapter());
        Assertions.assertNotNull(user.getUnreadNews());
        Assertions.assertNotNull(user.getUuid());

        for (ClientNews news : user.getUnreadNews()) {
            Assertions.assertNotNull(news);
            Assertions.assertNotNull(Utils.getNews(this.generator.getNews(), news.getId()));
        }

        for (int id : user.getUnreadChapter()) {
            Assertions.assertTrue(id > 0);
            Assertions.assertNotNull(Utils.getEpisode(this.generator.getEpisodes(), id));
        }

        for (ClientReadEpisode readEpisode : user.getReadToday()) {
            Assertions.assertTrue(readEpisode.getEpisodeId() > 0);
            Assertions.assertNotNull(Utils.getEpisode(this.generator.getEpisodes(), readEpisode.getEpisodeId()));
        }

        for (ClientMediaList list : user.getLists()) {
            assertMediaList(list);
        }
    }

    private void assertMediaList(ClientMediaList list) {
        Assertions.assertNotNull(list);
        Assertions.assertNotNull(Utils.getMediaList(this.generator.getMediaLists(), list.getId()));

        for (int item : list.getItems()) {
            assertMedium(item);
        }
    }

    private void assertMedium(int item) {
        Assertions.assertTrue(item > 0);

        ClientMedium medium = Utils.getMedium(this.generator.getMedia(), item);

        Assertions.assertNotNull(medium);
        Assertions.assertNotNull(medium.getParts());
        Assertions.assertNotNull(medium.getLatestReleased());
        Assertions.assertNotNull(medium.getUnreadEpisodes());

        if (medium.getCurrentRead() > 0) {
            assertEpisode(item, medium.getCurrentRead());
        }

        for (int episode : medium.getLatestReleased()) {
            assertEpisode(item, episode);
        }

        for (int episode : medium.getUnreadEpisodes()) {
            assertEpisode(item, episode);
        }

        for (int part : medium.getParts()) {
            Assertions.assertTrue(part > 0);

            ClientPart actual = Utils.getPart(this.generator.getParts(), part);
            Assertions.assertNotNull(actual);
            Assertions.assertNotNull(actual.getEpisodes());
            Assertions.assertEquals(actual.getMediumId(), item);

            for (ClientEpisode episode : actual.getEpisodes()) {
                this.assertEpisode(item, episode.getId());
            }
        }
    }

    private void assertEpisode(int mediumId, int episodeId) {
        Assertions.assertTrue(mediumId > 0);
        Assertions.assertTrue(episodeId > 0);

        ClientEpisode clientEpisode = Utils.getEpisode(this.generator.getEpisodes(), episodeId);
        Assertions.assertNotNull(clientEpisode);
        Assertions.assertNotNull(clientEpisode.getReleases());
        Assertions.assertTrue(clientEpisode.getPartId() > 0);

        ClientPart part = Utils.getPart(this.generator.getParts(), clientEpisode.getPartId());
        Assertions.assertNotNull(part);
        Assertions.assertEquals(part.getMediumId(), mediumId);

        for (ClientRelease release : clientEpisode.getReleases()) {
            Assertions.assertEquals(release.getEpisodeId(), episodeId);
        }
    }

    @Test
    void getParts() {
        for (ClientPart part : this.generator.getParts()) {
            Assertions.assertNotNull(part);
            Assertions.assertTrue(part.getId() > 0);
            Assertions.assertNotNull(part.getEpisodes());

            for (ClientEpisode episode : part.getEpisodes()) {
                this.assertEpisode(part.getMediumId(), episode.getId());
            }
        }
    }

    @Test
    void getEpisodes() {
        for (ClientEpisode episode : this.generator.getEpisodes()) {
            Assertions.assertNotNull(episode);
            Assertions.assertNotNull(episode.getReleases());
            Assertions.assertTrue(episode.getId() > 0);
            Assertions.assertTrue(episode.getPartId() > 0);
            Assertions.assertTrue(episode.getProgress() >= 0);

            ClientPart part = Utils.getPart(this.generator.getParts(), episode.getPartId());
            Assertions.assertNotNull(part);

            for (ClientRelease release : episode.getReleases()) {
                Assertions.assertEquals(release.getEpisodeId(), episode.getId());
            }
        }
    }

    @Test
    void getMediaLists() {
        for (ClientMediaList list : this.generator.getMediaLists()) {
            this.assertMediaList(list);
        }
    }

    @Test
    void getExternalUser() {
        for (ClientExternalUser user : this.generator.getExternalUser()) {
            Assertions.assertNotNull(user);
            Assertions.assertNotNull(user.getLists());
            Assertions.assertNotNull(user.getLocalUuid());
            Assertions.assertNotNull(user.getUuid());
            Assertions.assertEquals(user.getLocalUuid(), this.generator.getClientUser().getUuid());

            for (ClientExternalMediaList list : user.getLists()) {
                Assertions.assertNotNull(list);
                Assertions.assertEquals(list.getUuid(), user.getUuid());
                Assertions.assertTrue(list.getId() > 0);
                Assertions.assertNotNull(Utils.getExtMediaList(this.generator.getExtMediaLists(), list.getId()));

                for (int item : list.getItems()) {
                    assertMedium(item);
                }
            }
        }
    }

    @Test
    void getMedia() {
        for (ClientMedium media : this.generator.getMedia()) {
            Assertions.assertNotNull(media);
            this.assertMedium(media.getId());
        }
    }

    @Test
    void getNews() {
        for (ClientNews news : this.generator.getNews()) {
            Assertions.assertTrue(news.getId() > 0);
        }
    }

    @Test
    void getReadEpisodes() {
        for (ClientReadEpisode episode : this.generator.getReadEpisodes()) {
            Assertions.assertNotNull(episode);
            Assertions.assertTrue(episode.getEpisodeId() > 0);
            Assertions.assertTrue(episode.getProgress() >= 0);
            Assertions.assertNotNull(Utils.getEpisode(this.generator.getEpisodes(), episode.getEpisodeId()));
        }
    }

    @Test
    void getExtMediaLists() {
        for (ClientExternalMediaList list : this.generator.getExtMediaLists()) {
            Assertions.assertNotNull(list);
            Assertions.assertNotNull(Utils.getExtUser(this.generator.getExternalUser(), list.getUuid()));
            Assertions.assertTrue(list.getId() > 0);

            for (int item : list.getItems()) {
                assertMedium(item);
            }
        }

    }
}