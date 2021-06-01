package com.mytlogos.enterprise.background.resourceLoader;

import com.mytlogos.enterprise.background.api.model.ClientMultiListQuery;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

class MediaListLoader implements NetworkLoader<Integer> {

    private LoadWorker loadWorker;

    MediaListLoader(LoadWorker loadWorker) {
        this.loadWorker = loadWorker;
    }

    @Override
    public CompletableFuture<Void> loadItemsAsync(Set<Integer> toLoad) {
        return loadWorker.repository.loadMediaListAsync(toLoad).thenAccept(this::process);
    }

    @Override
    public Collection<DependencyTask<?>> loadItemsSync(Set<Integer> toLoad) {
        ClientMultiListQuery listQuery = this.loadWorker.repository.loadMediaListSync(toLoad);

        if (listQuery != null) {
            LoadWorkGenerator generator = new LoadWorkGenerator(this.loadWorker.loadedData);

            LoadWorkGenerator.FilteredMedia filteredMedia = generator.filterMedia(Arrays.asList(listQuery.getMedia()));
            this.loadWorker.persister.persist(filteredMedia);
            Collection<DependencyTask<?>> tasks = this.loadWorker.generator.generateMediaDependant(filteredMedia);

            LoadWorkGenerator.FilteredMediaList filteredMediaList = generator.filterMediaLists(Arrays.asList(listQuery.getList()));
            this.loadWorker.persister.persist(filteredMediaList);
            tasks.addAll(this.loadWorker.generator.generateMediaListsDependant(filteredMediaList));
            return tasks;
        }
        return Collections.emptyList();
    }

    private void process(ClientMultiListQuery listQuery) {
        if (listQuery != null) {
            loadWorker.persister.persist(listQuery);
        }
    }

    @Override
    public Set<Integer> getLoadedSet() {
        return loadWorker.loadedData.getMediaList();
    }
}
