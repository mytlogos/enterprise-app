package com.mytlogos.enterprise.background.resourceLoader;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface NetworkLoader<T> {
    CompletableFuture<Void> loadItemsAsync(Set<T> toLoad);

    Collection<DependencyTask<?>> loadItemsSync(Set<T> toLoad);

    Set<T> getLoadedSet();
}
