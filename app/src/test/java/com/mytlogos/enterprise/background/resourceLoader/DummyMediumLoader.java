package com.mytlogos.enterprise.background.resourceLoader;

import com.mytlogos.enterprise.background.api.model.ClientMedium;
import com.mytlogos.enterprise.background.resourceLoader.LoadWorkGenerator.FilteredMedia;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DummyMediumLoader extends MediumLoader {
    private final Collection<ClientMedium> media;
    private LoadWorker loadWorker;

    public DummyMediumLoader(Collection<ClientMedium> media) {
        super(null);
        this.media = media;
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
        Collection<ClientMedium> loadedMedia = new ArrayList<>(toLoad.size());

        for (Integer integer : toLoad) {
            for (ClientMedium medium : this.media) {
                if (medium.getId() == integer) {
                    loadedMedia.add(medium);
                    break;
                }
            }
        }
        LoadWorkGenerator generator = new LoadWorkGenerator(this.loadWorker.loadedData);
        FilteredMedia filteredMedia = generator.filterMedia(loadedMedia);
        this.loadWorker.persister.persist(filteredMedia);
        return this.loadWorker.generator.generateMediaDependant(filteredMedia);

    }

    @Override
    public Set<Integer> getLoadedSet() {
        return this.loadWorker.loadedData.getMedia();
    }
}
