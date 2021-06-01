package com.mytlogos.enterprise.background.resourceLoader

import com.mytlogos.enterprise.background.api.model.ClientMedium
import com.mytlogos.enterprise.background.api.model.ClientSimpleMedium
import java.util.concurrent.CompletableFuture
import java.util.stream.Collectors

open class MediumLoader(private val loadWorker: LoadWorker) : NetworkLoader<Int> {
    override fun loadItemsAsync(toLoad: Set<Int>): CompletableFuture<Void> {
        return loadWorker.repository.loadMediaAsync(toLoad)
            .thenAccept { media: List<ClientMedium>? -> process(media) }
    }

    override fun loadItemsSync(toLoad: Set<Int>): Collection<DependencyTask<*>> {
        val media = loadWorker.repository.loadMediaSync(toLoad)
        if (media != null) {
            val generator = LoadWorkGenerator(loadWorker.loadedData)
            val filteredMedia = generator.filterMedia(media)
            loadWorker.persister.persist(filteredMedia)
            return loadWorker.generator!!.generateMediaDependant(filteredMedia)
        }
        return emptyList()
    }

    private fun process(media: List<ClientMedium>?) {
        if (media != null) {
            loadWorker.persister.persistMedia(media.stream().map { medium: ClientMedium ->
                ClientSimpleMedium(
                    medium
                )
            }.collect(Collectors.toList()))
        }
    }

    override val loadedSet: Set<Int>
        get() = loadWorker.loadedData.media
}