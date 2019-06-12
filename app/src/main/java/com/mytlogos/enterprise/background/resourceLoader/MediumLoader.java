package com.mytlogos.enterprise.background.resourceLoader;

import com.mytlogos.enterprise.background.api.model.ClientMedium;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

class MediumLoader implements NetworkLoader<Integer> {
    private LoadWorker loadWorker;

    MediumLoader(LoadWorker loadWorker) {
        this.loadWorker = loadWorker;
    }

    @Override
    public CompletableFuture<Void> loadItemsAsync(Set<Integer> toLoad) {
        return loadWorker.repository.loadMediaAsync(toLoad).thenAccept(this::process);
    }

    @Override
    public Collection<DependencyTask<?>> loadItemsSync(Set<Integer> toLoad) {
        List<ClientMedium> media = this.loadWorker.repository.loadMediaSync(toLoad);

        if (media != null) {
            LoadWorkGenerator generator = new LoadWorkGenerator(this.loadWorker.loadedData);
            LoadWorkGenerator.FilteredMedia filteredMedia = generator.filterMedia(media);

            this.loadWorker.persister.persist(filteredMedia);
            return this.loadWorker.generator.generateMediaDependant(filteredMedia);
        }
        return Collections.emptyList();
    }

    private void process(List<ClientMedium> media) {
        if (media != null) {
            loadWorker.persister.persistMedia(media);
        }
    }

    @Override
    public Set<Integer> getLoadedSet() {
        return loadWorker.loadedData.getMedia();
    }
}
