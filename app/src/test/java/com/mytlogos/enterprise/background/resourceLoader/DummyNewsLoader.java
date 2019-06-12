package com.mytlogos.enterprise.background.resourceLoader;

import com.mytlogos.enterprise.background.api.model.ClientNews;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DummyNewsLoader extends NewsLoader {

    private final Collection<ClientNews> news;
    private LoadWorker loadWorker;

    public DummyNewsLoader(Collection<ClientNews> news) {
        super(null);
        this.news = news;
    }

    public void setLoadWorker(LoadWorker loadWorker) {
        this.loadWorker = loadWorker;
    }

    @Override
    public CompletableFuture<Void> loadItemsAsync(Set<Integer> toLoad) {
        return CompletableFuture.runAsync(() -> this.loadItemsSync(toLoad));
    }

    @Override
    public Collection<DependencyTask<?>> loadItemsSync(Set<Integer> toLoad) {
        Collection<ClientNews> loadedNews = new ArrayList<>(toLoad.size());

        for (Integer integer : toLoad) {
            for (ClientNews news : this.news) {
                if (news.getId() == integer) {
                    loadedNews.add(news);
                    break;
                }
            }
        }
        this.loadWorker.persister.persistNews(loadedNews);
        return Collections.emptyList();
    }

    @Override
    public Set<Integer> getLoadedSet() {
        return this.loadWorker.loadedData.getNews();
    }
}
