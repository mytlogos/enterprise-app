package com.mytlogos.enterprise.background.resourceLoader

import java.util.*
import java.util.concurrent.CompletableFuture

interface NetworkLoader<T> {
    fun loadItemsAsync(toLoad: Set<T>): CompletableFuture<Void>
    fun loadItemsSync(toLoad: Set<T>): Collection<DependencyTask<*>>
    val loadedSet: Set<T>
    fun loadItemsSyncIncremental(toLoad: Set<T>): Collection<DependencyTask<*>> {
        val incrementalLimit = incrementalLimit
        if (toLoad.size <= incrementalLimit) {
            return loadItemsSync(toLoad)
        }
        val tasks: MutableList<DependencyTask<*>> = ArrayList()
        val ids: MutableSet<T> = HashSet()
        var count = 0
        val iterator = toLoad.iterator()
        while (iterator.hasNext()) {
            val episodeId = iterator.next()
            ids.add(episodeId)
            if (count >= incrementalLimit) {
                tasks.addAll(loadItemsSync(ids))
                ids.clear()
                count = 0
            }
            count++
        }
        if (ids.isNotEmpty()) {
            tasks.addAll(loadItemsSync(ids))
        }
        return tasks
    }

    val incrementalLimit: Int
        get() = 100
}