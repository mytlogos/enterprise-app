package com.mytlogos.enterprise.background.resourceLoader

import com.mytlogos.enterprise.background.api.model.ClientPart
import java.util.concurrent.CompletableFuture

open class PartLoader(private val loadWorker: LoadWorker) : NetworkLoader<Int> {
    override fun loadItemsAsync(toLoad: Set<Int>): CompletableFuture<Void> {
        return loadWorker.repository.loadPartAsync(toLoad)
            .thenAccept { parts: List<ClientPart>? -> process(parts) }
    }

    private fun process(parts: List<ClientPart>?) {
        if (parts != null) {
            loadWorker.persister.persistParts(parts)
            loadWorker.doWork()
        }
    }

    override fun loadItemsSync(toLoad: Set<Int>): Collection<DependencyTask<*>> {
        val parts = loadWorker.repository.loadPartSync(toLoad)
        if (parts != null) {
            val generator = LoadWorkGenerator(loadWorker.loadedData)
            val filteredParts = generator.filterParts(parts)
            loadWorker.persister.persist(filteredParts)
            return loadWorker.generator!!.generatePartsDependant(filteredParts)
        }
        return emptyList()
    }

    override val loadedSet: Set<Int>
        get() = loadWorker.loadedData.part
}