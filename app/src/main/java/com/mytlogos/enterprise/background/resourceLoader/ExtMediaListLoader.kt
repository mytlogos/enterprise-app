package com.mytlogos.enterprise.background.resourceLoader

import com.mytlogos.enterprise.background.api.model.ClientExternalMediaList
import java.util.concurrent.CompletableFuture

open class ExtMediaListLoader(private val loadWorker: LoadWorker) : NetworkLoader<Int> {
    override fun loadItemsAsync(toLoad: Set<Int>): CompletableFuture<Void> {
        return loadWorker.repository.loadExternalMediaListAsync(toLoad)
            .thenAccept { externalMediaLists: List<ClientExternalMediaList>? ->
                process(
                    externalMediaLists
                )
            }
    }

    override fun loadItemsSync(toLoad: Set<Int>): Collection<DependencyTask<*>> {
        val externalMediaLists = loadWorker.repository.loadExternalMediaListSync(toLoad)
        if (externalMediaLists != null) {
            val generator = LoadWorkGenerator(loadWorker.loadedData)
            val filteredExtMediaList = generator.filterExternalMediaLists(externalMediaLists)
            loadWorker.persister.persist(filteredExtMediaList)
            return loadWorker.generator!!.generateExternalMediaListsDependant(filteredExtMediaList)
        }
        return emptyList()
    }

    private fun process(externalMediaLists: List<ClientExternalMediaList>?) {
        if (externalMediaLists != null) {
            loadWorker.persister.persistExternalMediaLists(externalMediaLists)
        }
    }

    override val loadedSet: Set<Int>
        get() = loadWorker.loadedData.externalMediaList
}