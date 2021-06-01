package com.mytlogos.enterprise.background.resourceLoader

import com.mytlogos.enterprise.background.*
import java.util.*
import java.util.concurrent.*
import java.util.function.Function

class LoadWorkerImpl(
    loadedData: LoadData,
    repository: Repository,
    persister: ClientModelPersister
) : LoadWorker(repository, persister, loadedData, null) {
    private val valueDependants: MutableMap<Any, DependantImpl> = ConcurrentHashMap()
    private val loaderMap: Map<Class<*>, LoaderManagerImpl<*>> = ConcurrentHashMap()
    private fun <T> addLoaderDependant(
        loader: LoaderManagerImpl<T>,
        id: T?,
        dependantValue: Any?,
        runnable: Runnable
    ) {
        requireNotNull(id) { "an id of null is not valid" }
        require(!(id === 0)) { "an id of zero is not valid" }
        require(!(id is String && (id as String).isEmpty())) { "an empty id string is not valid" }
        var dependantImpl: DependantImpl? = null
        if (dependantValue != null) {
            dependantImpl = valueDependants.computeIfAbsent(dependantValue) { o: Any? ->
                DependantImpl(
                    o,
                    runnable
                )
            }
        }
        loader.addDependant(id, dependantImpl)
    }

    override fun addIntegerIdTask(
        id: Int,
        dependantValue: Any?,
        loader: NetworkLoader<Int>,
        runnable: Runnable?,
        optional: Boolean
    ) {
        require(id != 0) { "an id of null is not valid" }
        var dependantImpl: DependantImpl? = null
        if (dependantValue != null) {
            dependantImpl = valueDependants.computeIfAbsent(dependantValue) { o: Any? ->
                DependantImpl(
                    o,
                    runnable
                )
            }
        }
        //        loader.addDependant(id, dependantImpl);
    }

    override fun addStringIdTask(
        id: String,
        dependantValue: Any?,
        loader: NetworkLoader<String>,
        runnable: Runnable?,
        optional: Boolean
    ) {
        requireNotNull(id) { "an id of null is not valid" }
        require(!id.isEmpty()) { "an empty id string is not valid" }
        var dependantImpl: DependantImpl? = null
        if (dependantValue != null) {
            dependantImpl = valueDependants.computeIfAbsent(dependantValue) { o: Any? ->
                DependantImpl(
                    o,
                    runnable
                )
            }
        }
        //        loader.addDependant(id, dependantImpl);
    }

    override fun isEpisodeLoading(id: Int): Boolean {
        return checkIsLoading(EpisodeLoader::class.java, id)
    }

    override fun isPartLoading(id: Int): Boolean {
        return checkIsLoading(PartLoader::class.java, id)
    }

    override fun isMediumLoading(id: Int): Boolean {
        return checkIsLoading(MediumLoader::class.java, id)
    }

    override fun isMediaListLoading(id: Int): Boolean {
        return checkIsLoading(MediaListLoader::class.java, id)
    }

    override fun isExternalMediaListLoading(id: Int): Boolean {
        return checkIsLoading(ExtMediaListLoader::class.java, id)
    }

    override fun isExternalUserLoading(uuid: String): Boolean {
        return checkIsLoading(ExtUserLoader::class.java, uuid)
    }

    override fun isNewsLoading(id: Int): Boolean {
        return checkIsLoading(NewsLoader::class.java, id)
    }

    private fun <T> checkIsLoading(loaderClass: Class<out NetworkLoader<T>?>, value: T): Boolean {
        for (manager in loaderMap.values) {
            if (manager.loader.javaClass.isAssignableFrom(loaderClass)) {
                val loaderManager = manager as LoaderManagerImpl<T>
                return loaderManager.isLoading(value)
            }
        }
        return false
    }

    public override fun doWork() {
        val loaderFutures: MutableMap<LoaderManagerImpl<*>, CompletableFuture<Void?>?> = HashMap()
        val currentDependantImpls: MutableSet<DependantImpl> = HashSet()
        for ((_, loader) in loaderMap) {
            currentDependantImpls.addAll(loader.currentDependants as Collection<DependantImpl>)
            loaderFutures[loader] = loader.loadAsync()
        }

        // this should be a partition of the values of valueDependants
        val loaderCombinations: MutableMap<Set<LoaderManagerImpl<*>>, MutableSet<DependantImpl>?> =
            HashMap()
        for (dependantImpl in currentDependantImpls) {
            val keySet: Set<LoaderManagerImpl<*>> = dependantImpl.getDependencies().keys
            if (keySet.isEmpty()) {
                continue
            }
            var dependantImpls = loaderCombinations[keySet]
            if (dependantImpls == null) {
                dependantImpls = Collections.synchronizedSet(HashSet())
                val key = Collections.unmodifiableSet(keySet)
                loaderCombinations[key] = dependantImpls
            }
            dependantImpls!!.add(dependantImpl)
        }
        val futureCombinations: MutableMap<CompletableFuture<Void?>, Set<DependantImpl>?> =
            HashMap()
        loaderCombinations.forEach { (loaders: Set<LoaderManagerImpl<*>>, dependants: Set<DependantImpl>?) ->
            var combinedFuture: CompletableFuture<Void?>? = null
            for (loader in loaders) {
                val future = loaderFutures[loader]
                    ?: throw IllegalStateException(
                        String.format(
                            "loader '%s' has no future",
                            loader.javaClass.simpleName
                        )
                    )
                combinedFuture = if (combinedFuture == null) {
                    future
                } else {
                    combinedFuture.thenCompose(Function<Void?, CompletionStage<Void?>> { a: Void? -> future })
                }
            }
            if (combinedFuture != null) {
                futureCombinations[combinedFuture] = dependants
            }
        }
        val futures: MutableList<Future<Void>> = ArrayList()
        futureCombinations.forEach { (future: CompletableFuture<Void?>, dependants: Set<DependantImpl>?) ->
            processFutures(
                futures,
                future,
                dependants
            )
        }

        // fixme async loading leads to deadlocks or sth. similar, debugger does not give thread dump
        //  for now it is loading synchronously
        // wait for all futures to finish before returning
        /*for (Future<Void> future : futures) {
            try {
                // wait at most 10s for one future, everything above 10s should be exceptional
//                future.get(30, TimeUnit.SECONDS);
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }*/
    }

    override fun work() {
        doWork()
    }

    private fun processFutures(
        futures: MutableList<Future<Void>>,
        future: CompletableFuture<Void?>,
        dependantImpls: Set<DependantImpl>?
    ) {
        val consumerMap = mapDependantsToConsumer(dependantImpls)
        for (consumer in consumerMap.keys) {
            val withBeforeRun: MutableList<DependantImpl> = ArrayList()
            val withoutBeforeRun: MutableList<DependantImpl> = ArrayList()
            val dependantImplSet = consumerMap[consumer]
            if (dependantImplSet != null) {
                for (dependantImpl in dependantImplSet) {
                    if (dependantImpl.runBefore != null) {
                        withBeforeRun.add(dependantImpl)
                        continue
                    }
                    withoutBeforeRun.add(dependantImpl)
                }
            }
            // fixme this cast could be a bug
            val clientConsumer = consumer as ClientConsumer<Any>?
            futures.add(future
                .thenRun {
                    println("running after loading in:" + Thread.currentThread())
                    val dependantsValues: MutableCollection<Any> = HashSet()
                    for (dependantImpl in withoutBeforeRun) {
                        // skip dependantImpls which are not ready yet
                        if (!dependantImpl.isReadyToBeConsumed) {
                            println("dependantImpl not yet ready!: $dependantImpl")
                            continue
                        }
                        if (dependantImpl.value is Collection<*>) {
                            dependantsValues.addAll(dependantImpl.value as Collection<Any>)
                            continue
                        }
                        dependantsValues.add(dependantImpl.value)
                    }
                    try {
                        clientConsumer!!.consume(dependantsValues)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return@thenRun
                    }
                    for (dependantImpl in withoutBeforeRun) {
                        valueDependants.remove(dependantImpl.value)
                    }
                })
            for (dependantImpl in withBeforeRun) {
                futures.add(future
                    .thenRun {
                        println("running with runnable after loading in:" + Thread.currentThread())
                        dependantImpl.runBefore!!.run()
                        if (dependantImpl.isReadyToBeConsumed) {
                            try {
                                if (dependantImpl.value is Collection<*>) {
                                    clientConsumer!!.consume(dependantImpl.value as Collection<Any>)
                                } else {
                                    clientConsumer!!.consume(listOf(dependantImpl.value))
                                }
                                valueDependants.remove(dependantImpl.value)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            // todo what todo if it is still not ready?
                            println("dependantImpl is still not ready!: $dependantImpl")
                        }
                    })
            }
        }
    }

    private fun mapDependantsToConsumer(dependantImpls: Set<DependantImpl>?): Map<ClientConsumer<*>?, Set<DependantImpl>> {
        val classValuesMap: MutableMap<Class<*>?, MutableSet<DependantImpl>> = HashMap()
        for (dependantImpl in dependantImpls!!) {
            var clazz: Class<*>? = null
            if (dependantImpl.value is Collection<*>) {
                val collection = dependantImpl.value
                if (collection.isEmpty()) {
                    println("dependantImpl list value is empty")
                    continue
                }
                // check only the first value,
                // on the assumption that every value after it has the same class
                for (o in collection) {
                    if (o != null) {
                        clazz = o.javaClass
                        break
                    }
                }
            } else {
                clazz = dependantImpl.value.javaClass
            }
            classValuesMap.computeIfAbsent(clazz) { c: Class<*>? -> HashSet() }
                .add(dependantImpl)
        }
        val consumerDependantsMap: MutableMap<ClientConsumer<*>?, Set<DependantImpl>> = HashMap()
        val consumer = persister.getConsumer()
        for (clientConsumer in consumer) {
            val dependantImplSet: Set<DependantImpl>? = classValuesMap[clientConsumer.type]
            if (dependantImplSet != null) {
                consumerDependantsMap[clientConsumer] = dependantImplSet
            }
        }
        return consumerDependantsMap
    }

    class DependantImpl internal constructor(value: Any?, runBefore: Runnable?) : Dependant {
        private val dependencies: MutableMap<LoaderManagerImpl<*>, MutableSet<*>> =
            ConcurrentHashMap()
        override val value: Any
        override val runBefore: Runnable?
        fun getDependencies(): Map<LoaderManagerImpl<*>, MutableSet<*>> {
            return dependencies
        }

        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o == null || javaClass != o.javaClass) return false
            val dependantImpl = o as DependantImpl
            return value == dependantImpl.value
        }

        override fun hashCode(): Int {
            return value.hashCode()
        }

        fun <T> addDependency(loader: LoaderManagerImpl<T>, value: T?) {
            synchronized(dependencies) {
                val set = dependencies
                    .computeIfAbsent(loader) { l: LoaderManagerImpl<*>? ->
                        Collections.synchronizedSet(
                            HashSet<Any>()
                        )
                    } as MutableSet<T?>
                set.add(value)
            }
        }

        val isReadyToBeConsumed: Boolean
            get() {
                for (dependencies in dependencies.values) {
                    if (dependencies != null && !dependencies.isEmpty()) {
                        return false
                    }
                }
                return true
            }

        override fun toString(): String {
            return "DependantImpl{" +
                    "dependencies=" + dependencies +
                    ", value=" + value +
                    ", runBefore=" + runBefore +
                    '}'
        }

        init {
            if (value == null) {
                throw NullPointerException()
            }
            this.runBefore = runBefore
            this.value = value
        }
    }

    class LoaderManagerImpl<T> private constructor(val loader: NetworkLoader<T>): LoaderManager<T> {
        private val toLoad = Collections.synchronizedSet(HashSet<T>())
        private val loading = Collections.synchronizedSet(HashSet<T>())
        private val dependantMap: ConcurrentMap<T, MutableSet<DependantImpl>> = ConcurrentHashMap()
        private val loadingFutureMap: ConcurrentMap<T, CompletableFuture<Void?>> =
            ConcurrentHashMap()

        override fun loadAsync(): CompletableFuture<Void?> {
            var toLoad: Set<T>
            var alreadyLoading: MutableSet<T>
            synchronized(this.toLoad) {
                alreadyLoading = HashSet(this.toLoad)
                alreadyLoading.retainAll(loading)
                this.toLoad.removeAll(loading)
                this.toLoad.removeAll(loader.loadedSet)
                toLoad = HashSet(this.toLoad)
                loading.addAll(toLoad)
                this.toLoad.clear()
            }
            var alreadyLoadingFuture: CompletableFuture<Void?>? = null
            val loadingFutures: MutableSet<CompletableFuture<Void?>> = HashSet()
            for (t in alreadyLoading) {
                val loadingFuture = loadingFutureMap[t]
                if (loadingFuture == null) {
                    System.err.println("missed future")
                } else {
                    if (loadingFutures.add(loadingFuture)) {
                        alreadyLoadingFuture = if (alreadyLoadingFuture == null) {
                            loadingFuture
                        } else {
                            alreadyLoadingFuture.thenCompose(
                                Function<Void?, CompletionStage<Void?>> { a: Void? -> loadingFuture })
                        }
                    }
                }
            }
            if (toLoad.isEmpty()) {
                return alreadyLoadingFuture
                    ?: CompletableFuture.completedFuture(null)
            }
            var future = loader.loadItemsAsync(toLoad)
            for (loading in toLoad) {
                check(
                    loadingFutureMap.put(
                        loading,
                        future as CompletableFuture<Void?>
                    ) == null
                ) { "loading an already loading item" }
            }
            if (alreadyLoadingFuture != null) {
                val finalAlreadyLoadingFuture: CompletableFuture<Void?> = alreadyLoadingFuture
                future = future.thenCompose { a: Void? -> finalAlreadyLoadingFuture }
            }
            future = future.thenRun {
                for (loaded in toLoad) {
                    if (!loader.loadedSet.contains(loaded)) {
                        println("could not load id: " + loaded + " of " + this.javaClass.simpleName)
                    }
                    // what should happen if it could not be loaded for whatever non-exceptional reason?
                    // can there even be a non exceptional reason that it cannot be loaded?
                    loadingFutureMap.remove(loaded)
                    loading.remove(loaded)
                    val dependantImpls: Set<DependantImpl>? = dependantMap.remove(loaded)
                    if (dependantImpls == null) {
                        println(
                            "Id '" + loaded + "' loaded with '" +
                                    this.javaClass.simpleName +
                                    "' even though there are no dependantImpls?"
                        )
                    } else {
                        for (dependantImpl in dependantImpls) {
                            // the dependencies of dependantImpl of this loader
                            val dependencies = dependantImpl.getDependencies()[this] as MutableSet<T>?
                                ?: throw IllegalStateException(
                                    String.format(
                                        "DependantImpl listed as DependantImpl even though it does not depend on any value of %s",
                                        this.javaClass.simpleName
                                    )
                                )
                            dependencies.remove(loaded)
                        }
                    }
                }
            }
            return future
        }

        override fun load() {}
        override fun addDependant(value: T, dependant: Dependant?) {
            if (dependant != null && dependant !is DependantImpl) {
                return
            }
            val dependantImpls = dependantMap
                .computeIfAbsent(value, { Collections.synchronizedSet(HashSet()) })
            if (dependant != null) {
                val d = dependant as DependantImpl
                if (dependantImpls.add(d)) {
                    toLoad.add(value)
                    d.addDependency(this, value)
                }
            } else {
                toLoad.add(value)
            }
        }

        override fun isLoaded(set: Set<T>?): Boolean {
            if (set == null) {
                return false
            }
            return loader.loadedSet.containsAll(set)
        }

        override fun removeLoaded(set: MutableSet<T>) {
            set.removeAll(loader.loadedSet)
        }

        override val currentDependants: Collection<Dependant>
            get() {
                val set: MutableSet<DependantImpl> = HashSet()
                for (t in toLoad) {
                    val dependantImpls: Set<DependantImpl>? = dependantMap[t]
                    if (dependantImpls != null) {
                        set.addAll(dependantImpls)
                    }
                }
                return set
            }

        override fun isLoading(value: T): Boolean {
            return loading.contains(value)
        }
    }
}