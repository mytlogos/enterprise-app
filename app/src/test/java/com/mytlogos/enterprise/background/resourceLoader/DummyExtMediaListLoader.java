package com.mytlogos.enterprise.background.resourceLoader;

import com.mytlogos.enterprise.background.api.model.ClientExternalMediaList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DummyExtMediaListLoader extends ExtMediaListLoader {

    private final Collection<ClientExternalMediaList> externalMediaLists;
    private LoadWorker loadWorker;

    public DummyExtMediaListLoader(Collection<ClientExternalMediaList> externalMediaLists) {
        super(null);
        this.externalMediaLists = externalMediaLists;
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
        Collection<ClientExternalMediaList> loadedExtLists = new ArrayList<>(toLoad.size());

        for (Integer integer : toLoad) {
            for (ClientExternalMediaList list : this.externalMediaLists) {
                if (list.getId() == integer) {
                    loadedExtLists.add(list);
                    break;
                }
            }
        }
        LoadWorkGenerator generator = new LoadWorkGenerator(this.loadWorker.loadedData);
        LoadWorkGenerator.FilteredExtMediaList filteredExtMediaList = generator.filterExternalMediaLists(loadedExtLists);
        this.loadWorker.persister.persist(filteredExtMediaList);
        return this.loadWorker.generator.generateExternalMediaListsDependant(filteredExtMediaList);

    }

    @Override
    public Set<Integer> getLoadedSet() {
        return loadWorker.loadedData.getExternalMediaList();
    }
}
