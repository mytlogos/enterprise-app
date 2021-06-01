package com.mytlogos.enterprise.background.resourceLoader

import com.mytlogos.enterprise.background.api.model.ClientNews
import java.util.concurrent.CompletableFuture

open class NewsLoader(private val loadWorker: LoadWorker) : NetworkLoader<Int> {
    override fun loadItemsAsync(toLoad: Set<Int>): CompletableFuture<Void> {
        return loadWorker.repository.loadNewsAsync(toLoad)
            .thenAccept { news: List<ClientNews>? -> process(news) }
    }

    private fun process(news: List<ClientNews>?) {
        if (news != null) {
            loadWorker.persister.persistNews(news)
        }
    }

    override fun loadItemsSync(toLoad: Set<Int>): Collection<DependencyTask<*>> {
        val news = loadWorker.repository.loadNewsSync(toLoad)
        process(news)
        return emptyList()
    }

    override val loadedSet: Set<Int>
        get() = loadWorker.loadedData.news
}