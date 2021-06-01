package com.mytlogos.enterprise.background.resourceLoader

import com.mytlogos.enterprise.background.api.model.ClientMultiListQuery
import java.util.concurrent.CompletableFuture

open class MediaListLoader(private val loadWorker: LoadWorker) : NetworkLoader<Int> {
    override fun loadItemsAsync(toLoad: Set<Int>): CompletableFuture<Void> {
        return loadWorker.repository.loadMediaListAsync(toLoad)
            .thenAccept { listQuery: ClientMultiListQuery? -> process(listQuery) }
    }

    override fun loadItemsSync(toLoad: Set<Int>): Collection<DependencyTask<*>> {
        val listQuery = loadWorker.repository.loadMediaListSync(toLoad)
        if (listQuery != null) {
            val generator = LoadWorkGenerator(loadWorker.loadedData)
            val filteredMedia = generator.filterMedia(listOf(*listQuery.media))
            loadWorker.persister.persist(filteredMedia)
            val tasks = loadWorker.generator!!.generateMediaDependant(filteredMedia)
            val filteredMediaList = generator.filterMediaLists(listOf(*listQuery.list))
            loadWorker.persister.persist(filteredMediaList)
            tasks.addAll(loadWorker.generator.generateMediaListsDependant(filteredMediaList))
            return tasks
        }
        return emptyList()
    }

    private fun process(listQuery: ClientMultiListQuery?) {
        if (listQuery != null) {
            loadWorker.persister.persist(listQuery)
        }
    }

    override val loadedSet: Set<Int>
        get() = loadWorker.loadedData.mediaList
}