package com.mytlogos.enterprise.background.resourceLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface NetworkLoader<T> {
    CompletableFuture<Void> loadItemsAsync(Set<T> toLoad);

    Collection<DependencyTask<?>> loadItemsSync(Set<T> toLoad);

    Set<T> getLoadedSet();

    default Collection<DependencyTask<?>> loadItemsSyncIncremental(Set<T> toLoad) {
        int incrementalLimit = this.getIncrementalLimit();

        if (toLoad.size() <= incrementalLimit) {
            return this.loadItemsSync(toLoad);
        }
        List<DependencyTask<?>> tasks = new ArrayList<>();
        Set<T> ids = new HashSet<>();

        int count = 0;

        for (Iterator<T> iterator = toLoad.iterator(); iterator.hasNext(); count++) {
            T episodeId = iterator.next();
            ids.add(episodeId);

            if (count >= incrementalLimit) {
                tasks.addAll(this.loadItemsSync(ids));
                ids.clear();
                count = 0;
            }
        }
        if (!ids.isEmpty()) {
            tasks.addAll(this.loadItemsSync(ids));
        }
        return tasks;
    }

    default int getIncrementalLimit() {
        return 100;
    }
}
