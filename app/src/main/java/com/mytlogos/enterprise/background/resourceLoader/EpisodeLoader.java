package com.mytlogos.enterprise.background.resourceLoader;

import com.mytlogos.enterprise.background.api.model.ClientEpisode;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

class EpisodeLoader implements NetworkLoader<Integer> {

    private LoadWorker loadWorker;

    public EpisodeLoader(LoadWorker loadWorker) {
        this.loadWorker = loadWorker;
    }

    @Override
    public CompletableFuture<Void> loadItemsAsync(Set<Integer> toLoad) {
        return loadWorker.repository.loadEpisodeAsync(toLoad).thenAccept(this::process);
    }

    @Override
    public Collection<DependencyTask<?>> loadItemsSync(Set<Integer> toLoad) {
        List<ClientEpisode> episodes = this.loadWorker.repository.loadEpisodeSync(toLoad);

        if (episodes != null) {
            LoadWorkGenerator generator = new LoadWorkGenerator(this.loadWorker.loadedData);
            LoadWorkGenerator.FilteredEpisodes filteredEpisodes = generator.filterEpisodes(episodes);
            this.loadWorker.persister.persist(filteredEpisodes);
            return this.loadWorker.generator.generateEpisodesDependant(filteredEpisodes);
        }

        return Collections.emptyList();
    }

    private void process(List<ClientEpisode> episodes) {
        if (episodes != null) {
            loadWorker.persister.persistEpisodes(episodes);
        }
    }

    @Override
    public Set<Integer> getLoadedSet() {
        return loadWorker.loadedData.getEpisodes();
    }
}
