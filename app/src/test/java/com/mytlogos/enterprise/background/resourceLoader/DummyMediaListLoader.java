package com.mytlogos.enterprise.background.resourceLoader;

import com.mytlogos.enterprise.background.api.model.ClientMediaList;
import com.mytlogos.enterprise.background.api.model.ClientMedium;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DummyMediaListLoader extends MediaListLoader {

    private final Collection<ClientMediaList> mediaLists;
    private final Collection<ClientMedium> media;
    private LoadWorker loadWorker;

    public DummyMediaListLoader(Collection<ClientMediaList> mediaLists, Collection<ClientMedium> media) {
        super(null);
        this.mediaLists = mediaLists;
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
        Collection<ClientMediaList> loadedLists = new ArrayList<>(toLoad.size());
        Collection<ClientMedium> loadedMedia = new HashSet<>();

        for (Integer integer : toLoad) {
            for (ClientMediaList list : this.mediaLists) {

                if (list.getId() == integer) {
                    loadedLists.add(list);
                    break;
                }
            }
        }
        for (ClientMediaList list : loadedLists) {
            for (int item : list.getItems()) {
                for (ClientMedium medium : this.media) {
                    if (medium.getId() == item) {
                        loadedMedia.add(medium);
                        break;
                    }
                }
            }
        }
        LoadWorkGenerator generator = new LoadWorkGenerator(this.loadWorker.loadedData);

        LoadWorkGenerator.FilteredMedia filteredMedia = generator.filterMedia(loadedMedia);
        this.loadWorker.persister.persist(filteredMedia);
        Collection<DependencyTask<?>> tasks = this.loadWorker.generator.generateMediaDependant(filteredMedia);

        LoadWorkGenerator.FilteredMediaList filteredMediaList = generator.filterMediaLists(loadedLists);
        this.loadWorker.persister.persist(filteredMediaList);
        tasks.addAll(this.loadWorker.generator.generateMediaListsDependant(filteredMediaList));
        return tasks;

    }

    @Override
    public Set<Integer> getLoadedSet() {
        return loadWorker.loadedData.getMediaList();
    }
}
