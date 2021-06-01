package com.mytlogos.enterprise.background.resourceLoader

import java.util.concurrent.CompletableFuture

internal interface LoaderManager<T> {
    fun loadAsync(): CompletableFuture<Void?>?
    fun load()
    fun addDependant(value: T, dependant: Dependant?)
    fun isLoaded(set: Set<T>?): Boolean
    fun removeLoaded(set: MutableSet<T>)
    val currentDependants: Collection<Dependant>
    fun isLoading(value: T): Boolean
}