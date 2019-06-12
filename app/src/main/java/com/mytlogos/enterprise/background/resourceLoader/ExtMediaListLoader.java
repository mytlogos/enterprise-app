package com.mytlogos.enterprise.background.resourceLoader;

import com.mytlogos.enterprise.background.api.model.ClientExternalMediaList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

class ExtMediaListLoader implements NetworkLoader<Integer> {

    private LoadWorker loadWorker;

    ExtMediaListLoader(LoadWorker loadWorker) {
        this.loadWorker = loadWorker;
    }

    @Override
    public CompletableFuture<Void> loadItemsAsync(Set<Integer> toLoad) {
        return loadWorker.repository.loadExternalMediaListAsync(toLoad)
                .thenAccept(this::process);
    }

    @Override
    public Collection<DependencyTask<?>> loadItemsSync(Set<Integer> toLoad) {
        List<ClientExternalMediaList> externalMediaLists = this.loadWorker.repository.loadExternalMediaListSync(toLoad);

        if (externalMediaLists != null) {
            LoadWorkGenerator generator = new LoadWorkGenerator(this.loadWorker.loadedData);
            LoadWorkGenerator.FilteredExtMediaList filteredExtMediaList = generator.filterExternalMediaLists(externalMediaLists);
            this.loadWorker.persister.persist(filteredExtMediaList);
            return this.loadWorker.generator.generateExternalMediaListsDependant(filteredExtMediaList);
        }
        return Collections.emptyList();
    }

    private void process(List<ClientExternalMediaList> externalMediaLists) {
        if (externalMediaLists != null) {
            loadWorker.persister.persistExternalMediaLists(externalMediaLists);
        }
    }


    @Override
    public Set<Integer> getLoadedSet() {
        return loadWorker.loadedData.getExternalMediaList();
    }
}
