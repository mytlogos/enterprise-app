package com.mytlogos.enterprise.background.resourceLoader;

import com.mytlogos.enterprise.background.api.model.ClientEpisode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DummyEpisodeLoader extends EpisodeLoader {

    private final Collection<ClientEpisode> episodes;
    private LoadWorker loadWorker;

    public DummyEpisodeLoader(Collection<ClientEpisode> episodes) {
        super(null);
        this.episodes = episodes;
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
        Collection<ClientEpisode> loadedEpisodes = new ArrayList<>(toLoad.size());

        for (Integer integer : toLoad) {
            boolean found = false;
            for (ClientEpisode episode : this.episodes) {
                if (episode.getId() == integer) {
                    loadedEpisodes.add(episode);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalArgumentException("unknown episode id");
            }
        }
        LoadWorkGenerator generator = new LoadWorkGenerator(this.loadWorker.loadedData);
        LoadWorkGenerator.FilteredEpisodes filteredEpisodes = generator.filterEpisodes(loadedEpisodes);
        this.loadWorker.persister.persist(filteredEpisodes);
        return this.loadWorker.generator.generateEpisodesDependant(filteredEpisodes);
    }

    @Override
    public Set<Integer> getLoadedSet() {
        return loadWorker.loadedData.getEpisodes();
    }
}
