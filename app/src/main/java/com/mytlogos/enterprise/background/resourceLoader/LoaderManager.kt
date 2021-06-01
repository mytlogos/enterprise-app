package com.mytlogos.enterprise.background.resourceLoader;

import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

interface LoaderManager<T> {
    CompletableFuture<Void> loadAsync();

    void load();

    void addDependant(T value, @Nullable Dependant dependant);

    boolean isLoaded(Set<T> set);

    void removeLoaded(Set<T> set);

    Collection<? extends Dependant> getCurrentDependants();

    boolean isLoading(T value);
}
