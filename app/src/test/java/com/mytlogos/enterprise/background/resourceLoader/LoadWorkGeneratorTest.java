package com.mytlogos.enterprise.background.resourceLoader;

import com.mytlogos.enterprise.DataGenerator;
import com.mytlogos.enterprise.Utils;
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

import java.util.List;

class LoadWorkGeneratorTest {
    private LoadWorkGenerator generator;
    private LoadData loadedData;
    private ClientUser clientUser;
    private List<ClientPart> parts;
    private List<ClientEpisode> episodes;
    private List<ClientMediaList> mediaLists;
    private List<ClientExternalUser> externalUser;
    private List<ClientMedium> media;
    private List<ClientNews> news;
    private List<ClientReadEpisode> readEpisodes;
    private List<ClientExternalMediaList> extMediaLists;

    @BeforeEach
    void setUp() {
        this.loadedData = new LoadData();
        this.generator = new LoadWorkGenerator(this.loadedData);
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
    }

    @Test
    void filterReadEpisodes() {
        LoadWorkGenerator.FilteredReadEpisodes readEpisodes = this.generator.filterReadEpisodes(this.readEpisodes);

        for (ClientReadEpisode episode : this.readEpisodes) {
            if (this.loadedData.getEpisodes().contains(episode.getEpisodeId())) {
                Assertions.assertTrue(readEpisodes.episodeList.contains(episode));
            } else {
                boolean found = false;
                for (LoadWorkGenerator.IntDependency<ClientReadEpisode> dependency : readEpisodes.dependencies) {
                    if (dependency.id == episode.getEpisodeId()) {
                        found = true;
                        break;
                    }
                }
                Assertions.assertTrue(found);
            }
        }
    }

    @Test
    void filterParts() {
        LoadWorkGenerator.FilteredParts parts = this.generator.filterParts(this.parts);

        for (LoadWorkGenerator.IntDependency<ClientPart> dependency : parts.mediumDependencies) {
            Assertions.assertTrue(Utils.containsMediumId(this.media, dependency.id));
            Assertions.assertEquals(dependency.dependency.getMediumId(), dependency.id);
        }
        for (ClientPart part : parts.newParts) {
            Assertions.assertFalse(this.loadedData.getPart().contains(part.getId()));
        }
        for (ClientPart part : parts.updateParts) {
            Assertions.assertTrue(this.loadedData.getPart().contains(part.getId()));
        }
    }

    @Test
    void filterEpisodes() {
        Assertions.fail("not yet implemented");
    }

    @Test
    void filterMedia() {
        Assertions.fail("not yet implemented");
    }

    @Test
    void filterMediaLists() {
        Assertions.fail("not yet implemented");
    }

    @Test
    void filterExternalMediaLists() {
        Assertions.fail("not yet implemented");
    }

    @Test
    void filterExternalUsers() {
        Assertions.fail("not yet implemented");
    }
}