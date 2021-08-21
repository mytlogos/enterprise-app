package com.mytlogos.enterprise.background.resourceLoader

import com.mytlogos.enterprise.DataGenerator
import com.mytlogos.enterprise.Utils
import com.mytlogos.enterprise.background.LoadData
import com.mytlogos.enterprise.background.api.model.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class LoadWorkGeneratorTest {
    private var generator: LoadWorkGenerator? = null
    private var loadedData: LoadData? = null
    private val clientUser: ClientUser? = null
    private var parts: List<ClientPart>? = null
    private var episodes: List<ClientEpisode>? = null
    private var mediaLists: List<ClientMediaList>? = null
    private var externalUser: List<ClientExternalUser>? = null
    private var media: List<ClientMedium>? = null
    private var news: List<ClientNews>? = null
    private var readEpisodes: List<ClientReadEpisode>? = null
    private var extMediaLists: List<ClientExternalMediaList>? = null

    @BeforeEach
    fun setUp() {
        loadedData = LoadData()
        generator = LoadWorkGenerator(loadedData!!)
        val dataGenerator = DataGenerator()
        dataGenerator.generateCompleteTestData()
        externalUser = dataGenerator.externalUser
        mediaLists = dataGenerator.mediaLists
        extMediaLists = dataGenerator.extMediaLists
        episodes = dataGenerator.episodes
        parts = dataGenerator.parts
        media = dataGenerator.media
        news = dataGenerator.news
        readEpisodes = dataGenerator.readEpisodes
    }

    @Test
    fun filterReadEpisodes() {
        val readEpisodes = generator!!.filterReadEpisodes(readEpisodes!!)
        for (episode in this.readEpisodes!!) {
            if (loadedData!!.episodes.contains(episode.episodeId)) {
                Assertions.assertTrue(readEpisodes.episodeList.contains(episode))
            } else {
                var found = false
                for (dependency in readEpisodes.dependencies) {
                    if (dependency.id == episode.episodeId) {
                        found = true
                        break
                    }
                }
                Assertions.assertTrue(found)
            }
        }
    }

    @Test
    fun filterParts() {
        val parts = generator!!.filterParts(parts!!)
        for (dependency in parts.mediumDependencies) {
            Assertions.assertTrue(Utils.containsMediumId(
                media, dependency.id))
            Assertions.assertEquals(dependency.dependency.mediumId, dependency.id)
        }
        for (part in parts.newParts) {
            Assertions.assertFalse(loadedData!!.part.contains(part.id))
        }
        for (part in parts.updateParts) {
            Assertions.assertTrue(loadedData!!.part.contains(part.id))
        }
    }

    @Test
    fun filterEpisodes() {
        Assertions.fail<Any>("not yet implemented")
    }

    @Test
    fun filterMedia() {
        Assertions.fail<Any>("not yet implemented")
    }

    @Test
    fun filterMediaLists() {
        Assertions.fail<Any>("not yet implemented")
    }

    @Test
    fun filterExternalMediaLists() {
        Assertions.fail<Any>("not yet implemented")
    }

    @Test
    fun filterExternalUsers() {
        Assertions.fail<Any>("not yet implemented")
    }
}