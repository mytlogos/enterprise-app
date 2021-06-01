package com.mytlogos.enterprise.background.resourceLoader;

public class DependencyTask<T> {
    final T idValue;
    final DependantValue dependantValue;
    final boolean optional;
    final NetworkLoader<T> loader;

    public DependencyTask(T idValue, DependantValue dependantValue, NetworkLoader<T> loader, boolean optional) {
        this.idValue = idValue;
        this.dependantValue = dependantValue;
        this.optional = optional;
        this.loader = loader;
    }

    public DependencyTask(T idValue, DependantValue dependantValue, NetworkLoader<T> loader) {
        this(idValue, dependantValue, loader, false);
    }
}
