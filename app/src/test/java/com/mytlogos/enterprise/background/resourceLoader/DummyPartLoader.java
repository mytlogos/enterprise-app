package com.mytlogos.enterprise.background.resourceLoader;

import com.mytlogos.enterprise.background.api.model.ClientPart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DummyPartLoader extends PartLoader {

    private final Collection<ClientPart> parts;
    private LoadWorker loadWorker;

    public DummyPartLoader(Collection<ClientPart> parts) {
        super(null);
        this.parts = parts;
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
        Collection<ClientPart> loadedParts = new ArrayList<>(toLoad.size());

        for (Integer integer : toLoad) {
            for (ClientPart part : this.parts) {
                if (part.getId() == integer) {
                    loadedParts.add(part);
                    break;
                }
            }
        }
        LoadWorkGenerator generator = new LoadWorkGenerator(this.loadWorker.loadedData);
        LoadWorkGenerator.FilteredParts filteredParts = generator.filterParts(loadedParts);
        this.loadWorker.persister.persist(filteredParts);
        return this.loadWorker.generator.generatePartsDependant(filteredParts);

    }

    @Override
    public Set<Integer> getLoadedSet() {
        return this.loadWorker.loadedData.getPart();
    }
}
