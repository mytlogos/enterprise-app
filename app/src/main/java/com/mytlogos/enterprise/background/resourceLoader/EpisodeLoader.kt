package com.mytlogos.enterprise.background.resourceLoader

import com.mytlogos.enterprise.background.api.model.ClientEpisode
import java.util.concurrent.CompletableFuture

open class EpisodeLoader(private val loadWorker: LoadWorker) : NetworkLoader<Int> {
    override fun loadItemsAsync(toLoad: Set<Int>): CompletableFuture<Void> {
        return loadWorker.repository.loadEpisodeAsync(toLoad)
            .thenAccept { episodes: List<ClientEpisode>? -> process(episodes) }
    }

    override fun loadItemsSync(toLoad: Set<Int>): Collection<DependencyTask<*>> {
        val episodes = loadWorker.repository.loadEpisodeSync(toLoad)
        if (episodes != null) {
            val generator = LoadWorkGenerator(loadWorker.loadedData)
            val filteredEpisodes = generator.filterEpisodes(episodes)
            loadWorker.persister.persist(filteredEpisodes)
            return loadWorker.generator!!.generateEpisodesDependant(filteredEpisodes)
        }
        return emptyList()
    }

    private fun process(episodes: List<ClientEpisode>?) {
        if (episodes != null) {
            loadWorker.persister.persistEpisodes(episodes)
        }
    }

    override val loadedSet: Set<Int>
        get() = loadWorker.loadedData.episodes
}