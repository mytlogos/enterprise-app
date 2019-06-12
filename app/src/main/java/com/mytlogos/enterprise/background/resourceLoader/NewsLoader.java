package com.mytlogos.enterprise.background.resourceLoader;

import com.mytlogos.enterprise.background.api.model.ClientNews;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

class NewsLoader implements NetworkLoader<Integer> {

    private LoadWorker loadWorker;

    NewsLoader(LoadWorker loadWorker) {
        this.loadWorker = loadWorker;
    }

    @Override
    public CompletableFuture<Void> loadItemsAsync(Set<Integer> toLoad) {
        return loadWorker.repository.loadNewsAsync(toLoad).thenAccept(this::process);
    }

    private void process(List<ClientNews> news) {
        if (news != null) {
            loadWorker.persister.persistNews(news);
        }
    }

    @Override
    public Collection<DependencyTask<?>> loadItemsSync(Set<Integer> toLoad) {
        List<ClientNews> news = this.loadWorker.repository.loadNewsSync(toLoad);
        this.process(news);
        return Collections.emptyList();
    }

    @Override
    public Set<Integer> getLoadedSet() {
        return loadWorker.loadedData.getNews();
    }
}
