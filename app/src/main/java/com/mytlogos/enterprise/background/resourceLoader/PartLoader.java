package com.mytlogos.enterprise.background.resourceLoader;

import com.mytlogos.enterprise.background.api.model.ClientPart;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

class PartLoader implements NetworkLoader<Integer> {

    private LoadWorker loadWorker;

    PartLoader(LoadWorker loadWorker) {
        this.loadWorker = loadWorker;
    }

    @Override
    public CompletableFuture<Void> loadItemsAsync(Set<Integer> toLoad) {
        return loadWorker.repository.loadPartAsync(toLoad).thenAccept(this::process);
    }

    private void process(List<ClientPart> parts) {
        if (parts != null) {
            loadWorker.persister.persistParts(parts);
            loadWorker.doWork();
        }
    }

    @Override
    public Collection<DependencyTask<?>> loadItemsSync(Set<Integer> toLoad) {
        List<ClientPart> parts = this.loadWorker.repository.loadPartSync(toLoad);
        if (parts != null) {
            LoadWorkGenerator generator = new LoadWorkGenerator(this.loadWorker.loadedData);
            LoadWorkGenerator.FilteredParts filteredParts = generator.filterParts(parts);

            this.loadWorker.persister.persist(filteredParts);
            return this.loadWorker.generator.generatePartsDependant(filteredParts);
        }
        return Collections.emptyList();
    }

    @Override
    public Set<Integer> getLoadedSet() {
        return loadWorker.loadedData.getPart();
    }
}
