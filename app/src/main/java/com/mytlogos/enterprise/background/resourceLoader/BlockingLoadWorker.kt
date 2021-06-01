package com.mytlogos.enterprise.background.resourceLoader

import com.mytlogos.enterprise.background.*
import java.util.*
import java.util.concurrent.*
import java.util.function.BiFunction
import java.util.function.Function
import java.util.stream.Stream

class BlockingLoadWorker : LoadWorker {
    // TODO: 11.06.2019 starting after a month with this new loader lead to many episodeNodes rejecting
    // FIXME: 11.06.2019 apparently the databaseValidator detected a null id value as parameter and threw an error
    private val valueNodes: ConcurrentMap<DependantValue, DependantNode> = ConcurrentHashMap()
    private val workService = Executors.newSingleThreadExecutor()
    private val loadingService = Executors.newFixedThreadPool(5)
    private val intLoaderManager: MutableMap<NetworkLoader<Int>, IntLoaderManager> = HashMap()
    private val stringLoaderManager: MutableMap<NetworkLoader<String>, StringLoaderManager> =
        HashMap()
    private val enforceMedia: MutableList<Int> = ArrayList()
    private val enforcePart: MutableList<Int> = ArrayList()

    constructor(
        loadedData: LoadData,
        repository: Repository,
        persister: ClientModelPersister,
        generator: DependantGenerator
    ) : super(repository, persister, loadedData, generator) {
    }

    internal constructor(
        repository: Repository,
        persister: ClientModelPersister,
        loadedData: LoadData,
        extUserLoader: ExtUserLoader,
        external_medialist_loader: ExtMediaListLoader,
        medialist_loader: MediaListLoader,
        medium_loader: MediumLoader,
        part_loader: PartLoader,
        episode_loader: EpisodeLoader,
        news_loader: NewsLoader,
        generator: DependantGenerator?
    ) : super(
        repository,
        persister,
        loadedData,
        extUserLoader,
        external_medialist_loader,
        medialist_loader,
        medium_loader,
        part_loader,
        episode_loader,
        news_loader,
        generator
    ) {
    }

    override fun addIntegerIdTask(
        id: Int,
        value: DependantValue?,
        loader: NetworkLoader<Int>,
        optional: Boolean
    ) {
        addIdTask<Int>(id, value, optional, getIntLoaderManager(loader))
    }

    override fun addStringIdTask(
        id: String,
        value: DependantValue?,
        loader: NetworkLoader<String>,
        optional: Boolean
    ) {
        addIdTask(id, value, optional, getStringLoaderManager(loader))
    }

    private fun getIntLoaderManager(loader: NetworkLoader<Int>): IntLoaderManager {
        return intLoaderManager.computeIfAbsent(loader) { loader1: NetworkLoader<Int> ->
            IntLoaderManager(
                loader1,
                loader.loadedSet
            )
        }
    }

    private fun getStringLoaderManager(loader: NetworkLoader<String>): StringLoaderManager {
        return stringLoaderManager.computeIfAbsent(loader) { loader1: NetworkLoader<String> ->
            StringLoaderManager(
                loader1,
                loader.loadedSet
            )
        }
    }

    private fun <T> addIdTask(
        id: T,
        value: DependantValue?,
        optional: Boolean,
        loaderManager: LoaderManagerImpl<T>
    ) {
        var node: DependantNode? = null
        if (value != null) {
            node = valueNodes[value]
            if (value.intId > 0) {
                val manager = getIntLoaderManager(value.integerLoader!!)
                node = manager.valueDependants
                    .compute(
                        value.intId
                    ) { _: Int?, remapNode: DependantNode? ->
                        remapNode(
                            value,
                            remapNode
                        )
                    }
            } else if (value.stringId != null) {
                val manager = getStringLoaderManager(value.stringLoader!!)
                node = manager.valueDependants
                    .compute(
                        value.stringId
                    ) { _: String?, remapNode: DependantNode? ->
                        remapNode(
                            value,
                            remapNode
                        )
                    }
            }
            if (node == null) {
                node = DependantNode(value)
                valueNodes[value] = node
            }
        }
        loaderManager.addDependant(id, node, optional)
    }

    private fun remapNode(value: DependantValue, dependantNode: DependantNode?): DependantNode? {
        if (dependantNode == null || !dependantNode.isRoot()) {
            return null
        }
        val newNode = dependantNode.createNewNode(value)
        valueNodes[value] = newNode
        return newNode
    }

    @Deprecated("")
    override fun addIntegerIdTask(
        id: Int,
        dependantValue: Any?,
        loader: NetworkLoader<Int>,
        runnable: Runnable?,
        optional: Boolean
    ) {
        throw UnsupportedOperationException()
    }

    @Deprecated("")
    override fun addStringIdTask(
        id: String,
        dependantValue: Any?,
        loader: NetworkLoader<String>,
        runnable: Runnable?,
        optional: Boolean
    ) {
        throw UnsupportedOperationException()
    }

    override fun isEpisodeLoading(id: Int): Boolean {
        return isIntegerIdLoading(id, EPISODE_LOADER)
    }

    override fun isPartLoading(id: Int): Boolean {
        return isIntegerIdLoading(id, PART_LOADER)
    }

    override fun isMediumLoading(id: Int): Boolean {
        return isIntegerIdLoading(id, MEDIUM_LOADER)
    }

    override fun isMediaListLoading(id: Int): Boolean {
        return isIntegerIdLoading(id, MEDIALIST_LOADER)
    }

    override fun isExternalMediaListLoading(id: Int): Boolean {
        return isIntegerIdLoading(id, EXTERNAL_MEDIALIST_LOADER)
    }

    override fun isExternalUserLoading(uuid: String): Boolean {
        return isStringIdLoading(uuid, EXTERNAL_USER_LOADER)
    }

    private fun isStringIdLoading(uuid: String, loader: NetworkLoader<String>): Boolean {
        val manager = stringLoaderManager[loader] ?: return false
        return manager.isLoading(uuid)
    }

    override fun isNewsLoading(id: Int): Boolean {
        return isIntegerIdLoading(id, NEWS_LOADER)
    }

    private fun isIntegerIdLoading(id: Int, loader: NetworkLoader<Int>): Boolean {
        val manager = intLoaderManager[loader] ?: return false
        return manager.isLoading(id)
    }

    private fun <T> processTasks(
        manager: LoaderManagerImpl<T>,
        freeIds: MutableSet<T>,
        tasks: Collection<DependencyTask<*>>
    ): Collection<DependantNode> {
        for (task in tasks) {
            if (task.idValue is Int) {
                this.addIntegerIdTask(
                    task.idValue,
                    task.dependantValue,
                    task.loader as NetworkLoader<Int>,
                    task.optional
                )
                removeUnresolvedIds(manager, freeIds, task)
            } else if (task.idValue is String) {
                this.addStringIdTask(
                    task.idValue,
                    task.dependantValue,
                    task.loader as NetworkLoader<String>,
                    task.optional
                )
                removeUnresolvedIds(manager, freeIds, task)
            } else {
                throw IllegalArgumentException("unknown id value type: neither Integer nor String")
            }
        }
        return getResolvedNodes(manager, freeIds)
    }

    private fun <T> removeUnresolvedIds(
        manager: LoaderManagerImpl<T>,
        freeIds: MutableSet<T>,
        task: DependencyTask<*>
    ) {
        if (task.optional || task.dependantValue == null) {
            return
        }
        if (task.dependantValue.integerLoader === manager.loader) {
            freeIds.remove(task.dependantValue.intId as T)
        } else if (task.dependantValue.stringLoader === manager.loader) {
            freeIds.remove(task.dependantValue.stringId as T)
        }
    }

    private fun <T> loadIds(manager: LoaderManagerImpl<T>): Collection<DependantNode> {
        val freeIds = manager.freeIds
        System.out.printf(
            "Loader: %s got Ids: %s\n",
            manager.loader.javaClass.simpleName,
            freeIds
        )
        if (freeIds.isEmpty()) {
            return emptyList()
        }
        if (manager.loaded.containsAll(freeIds)) {
            return getResolvedNodes(manager, freeIds)
        }
        val tasks = manager.loader.loadItemsSyncIncremental(freeIds)
        for (task in tasks) {
            if (task.idValue is Int) {
                this.addIntegerIdTask(
                    (task.idValue as Int),
                    task.dependantValue,
                    task.loader as NetworkLoader<Int>,
                    task.optional
                )
                removeUnresolvedId(manager, freeIds, task)
            } else if (task.idValue is String) {
                this.addStringIdTask(
                    task.idValue as String,
                    task.dependantValue,
                    task.loader as NetworkLoader<String>,
                    task.optional
                )
                removeUnresolvedId(manager, freeIds, task)
            } else {
                throw IllegalArgumentException("unknown id value type: neither Integer nor String")
            }
        }
        return getResolvedNodes(manager, freeIds)
    }

    private fun <T> removeUnresolvedId(
        manager: LoaderManagerImpl<T>,
        freeIds: MutableSet<T>,
        task: DependencyTask<*>
    ) {
        if (!task.optional && task.dependantValue != null) {
            if (task.dependantValue.integerLoader === manager.loader) {
                freeIds.remove(task.dependantValue.intId as T)
            } else if (task.dependantValue.stringLoader === manager.loader) {
                freeIds.remove(task.dependantValue.stringId as T)
            }
        }
    }

    private fun <T> getResolvedNodes(
        manager: LoaderManagerImpl<T>,
        freeIds: Set<T>
    ): Collection<DependantNode> {
        val nodes: MutableCollection<DependantNode> = ArrayList()
        System.out
            .printf(
                "Consumed FreeIds %d for %s",
                freeIds.size,
                manager.loader!!.javaClass.simpleName
            )
            .println()
        for (id in freeIds) {
            val node = manager.getIdDependant(id)
            if (node == null) {
                System.err.println("Node is null even though it just loaded")
                continue
            }
            manager.removeId(id)
            if (!manager.isLoaded(id)) {
                System.out
                    .printf(
                        "Rejecting Dependencies for Id '%s' with loader '%s'",
                        id,
                        manager.loader.javaClass.simpleName
                    )
                    .println()
                node.rejectNode()
                continue
            }
            nodes.addAll(node.removeAsParent())
        }
        return nodes
    }

    private fun consumeDependantValue(values: Collection<DependantValue>) {
        Objects.requireNonNull(values)
        val consumerMap = mapDependantsToConsumer(values)
        for ((key, value1) in consumerMap) {
            val dependantsValues: MutableCollection<Any> = HashSet()
            for (value in value1) {
                if (value.value is Collection<*>) {
                    dependantsValues.addAll((value.value as Collection<Any>))
                    continue
                }
                if (value.value != null) {
                    dependantsValues.add(value.value)
                }
            }
            try {
                val clientConsumer = key as ClientConsumer<Any>
                clientConsumer.consume(dependantsValues)
                for (value in value1) {
                    val node = valueNodes.remove(value)
                    if (node != null && !node.isFree) {
                        println("removed node is not free!")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return
            }
        }
    }

    private fun mapDependantsToConsumer(values: Collection<DependantValue>): Map<ClientConsumer<*>, Set<DependantValue>> {
        val classValuesMap: MutableMap<Class<*>, MutableSet<DependantValue>> = HashMap()

        for (dependantImpl in values) {
            var clazz: Class<*>? = null
            if (dependantImpl.value is Collection<*>) {
                val collection = dependantImpl.value
                if (collection.isEmpty()) {
                    System.err.println("collection is empty")
                    continue
                }
                // check only the first value,
                // on the assumption that every value after it has the same class
                for (o in collection) {
                    if (o !== null) {
                        clazz = o.javaClass
                        break
                    }
                }
            } else if (dependantImpl.value != null) {
                clazz = dependantImpl.value.javaClass
            }
            if (clazz != null) {
                classValuesMap.computeIfAbsent(clazz) { HashSet() }
                    .add(dependantImpl)
            }
        }

        val consumerDependantsMap: MutableMap<ClientConsumer<*>, Set<DependantValue>> = HashMap()
        val consumer = persister.getConsumer()
        for (clientConsumer in consumer) {
            val dependantImplSet: Set<DependantValue>? = classValuesMap[clientConsumer.type]
            if (dependantImplSet != null) {
                consumerDependantsMap[clientConsumer] = dependantImplSet
            }
        }
        return consumerDependantsMap
    }

    private fun hasCircularDependencies(): Boolean {
        for (root in valueNodes.values) {
            val visitedNodes: MutableSet<DependantNode> = HashSet()
            val nodeStack: Deque<DependantNode> = LinkedList()
            nodeStack.push(root)
            while (!nodeStack.isEmpty()) {
                val current = nodeStack.pop()
                if (!visitedNodes.add(current)) {
                    return true
                }
                for (child in current!!.getChildren()) {
                    nodeStack.push(child)
                }
            }
        }
        return false
    }

    override fun doWork() {
        println("do work")
        var totalWork = 0
        for (manager in ArrayList(stringLoaderManager.values)) {
            totalWork += manager.loading()
        }
        for (manager in ArrayList(intLoaderManager.values)) {
            totalWork += manager.loading()
        }
        updateTotalWork(totalWork)
        val nodes: MutableCollection<DependantNode> = ArrayList()
        val futureProcessTasksMap: MutableMap<Future<Collection<DependencyTask<*>>>, ProcessTasks> =
            HashMap()
        for (manager in ArrayList(stringLoaderManager.values)) {
            processLoadingManager(nodes, futureProcessTasksMap, manager)
        }
        for (manager in ArrayList(intLoaderManager.values)) {
            processLoadingManager(nodes, futureProcessTasksMap, manager)
        }
        for ((key, value) in futureProcessTasksMap) {
            try {
                val tasks = key.get()
                //                Collection<DependencyTask<?>> tasks = entry.getKey().get(20, TimeUnit.SECONDS);
                nodes.addAll(value.apply(tasks))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /*for (StringLoaderManager manager : new ArrayList<>(this.stringLoaderManager.values())) {
            if (manager.hasFreeIds()) {
                nodes.addAll(this.loadIds(manager));
            }
        }
        for (LoaderManagerImpl<Integer> manager : new ArrayList<>(this.intLoaderManager.values())) {
            if (manager.hasFreeIds()) {
                nodes.addAll(this.loadIds(manager));
            }
        }*/println("finish work")
        //        print();

        // todo only exit condition, maybe do more
        if (!nodes.isEmpty()) {
            val values: MutableCollection<DependantValue> = ArrayList()
            for (node in nodes) {
                if (node.isFree) {
                    if (node.value.runnable != null) {
                        try {
                            node.value.runnable.run()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            continue
                        }
                    }
                    values.add(node.value)
                }
            }
            consumeDependantValue(values)
        } else if (!hasFreeIds()) {
            println("empty nodes and no free ids")
            return
        }
        doWork()
    }

    private fun print() {
        for ((key, value) in intLoaderManager) {
            printManager(key, value)
        }
        for ((key, value) in stringLoaderManager) {
            printManager(key, value)
        }
        val s = getFormattedLoaded("Media", loadedData.media) +
                getFormattedLoaded("Parts", loadedData.part) +
                getFormattedLoaded("Episodes", loadedData.episodes) +
                getFormattedLoaded("News", loadedData.news) +
                getFormattedLoaded("MediaLists", loadedData.mediaList) +
                getFormattedLoaded("ExtMediaLists", loadedData.externalMediaList) +
                getFormattedLoaded("ExtUser", loadedData.externalUser)
        System.err.println(s)
    }

    private fun <T> getFormattedLoaded(s: String, set: Set<T>?): String {
        return String.format(Locale.getDefault(), "Loaded %s %d: %s\n", s, set!!.size, set)
    }

    private fun <T> printManager(loader: NetworkLoader<T>?, manager: LoaderManagerImpl<T>) {
        val name = loader!!.javaClass.simpleName
        val loading = manager.loading
        val freeIds = manager.freeIds
        for (loadingId in loading) {
            if (!manager.valueDependants.containsKey(loadingId)) {
                System.out.printf("%s contains loading Id '%s' without a node\n", name, loadingId)
            }
        }
        val classFrequencyChildren: MutableMap<String, Int> = HashMap()
        val classFrequencyOptChildren: MutableMap<String, Int> = HashMap()
        for (node in manager.valueDependants.values) {
            for (child in node.getChildren()) {
                val simpleName = child.value.value?.javaClass?.simpleName
                if (simpleName != null) {
                    classFrequencyChildren.merge(simpleName, 1) { a: Int?, b: Int? ->
                        Integer.sum(
                            a!!, b!!
                        )
                    }
                }
            }
            for (child in node.getOptionalChildren()) {
                val simpleName = child.value.value?.javaClass?.simpleName
                if (simpleName != null) {
                    classFrequencyOptChildren.merge(simpleName, 1) { a: Int?, b: Int? ->
                        Integer.sum(
                            a!!, b!!
                        )
                    }
                }
            }
        }
        val format = "%s:\nLoading %d: %s\nFree %d: %s\n"
        val builder = StringBuilder(format + "Children: ")
        val parameterList: MutableList<Any> = ArrayList(
            classFrequencyChildren.size * 2 + classFrequencyOptChildren.size * 2
        )
        for ((key, value) in classFrequencyChildren) {
            builder.append("%s: %d, ")
            parameterList.add(key)
            parameterList.add(value)
        }
        builder.append("\nOptional Children: ")
        for ((key, value) in classFrequencyOptChildren) {
            builder.append("%s: %d, ")
            parameterList.add(key)
            parameterList.add(value)
        }
        builder.append("\n")
        val objects = arrayOf(name, loading.size, loading, freeIds.size, freeIds)
        val parameter = Stream.concat(Arrays.stream(objects), parameterList.stream()).toArray()
        System.err.println(
            String.format(
                Locale.getDefault(),
                builder.toString(),
                *parameter
            )
        )
    }

    private fun <T> processLoadingManager(
        nodes: MutableCollection<DependantNode>,
        futureProcessTasksMap: MutableMap<Future<Collection<DependencyTask<*>>>, ProcessTasks>,
        manager: LoaderManagerImpl<T>
    ) {
        if (manager.hasFreeIds()) {
            val freeIds = manager.freeIds
            System.out.printf(
                "Loader: %s got Ids: %s\n",
                manager.loader.javaClass.simpleName,
                freeIds
            )
            if (manager.loaded.containsAll(freeIds)) {
                nodes.addAll(getResolvedNodes(manager, freeIds))
            }
            val future: Future<Collection<DependencyTask<*>>> = loadingService.submit(
                Callable {
                    manager.loader.loadItemsSyncIncremental(
                        freeIds
                    )
                })
            val processTasks = ProcessTasks { tasks: Collection<DependencyTask<*>> ->
                processTasks(
                    manager,
                    freeIds,
                    tasks
                )
            }
            futureProcessTasksMap[future] = processTasks
        }
    }

    private fun hasFreeIds(): Boolean {
        for (manager in intLoaderManager.values) {
            if (manager.hasFreeIds()) {
                return true
            }
        }
        for (manager in stringLoaderManager.values) {
            if (manager.hasFreeIds()) {
                return true
            }
        }
        return false
    }

    override fun enforceMediumStructure(id: Int) {
        enforceMedia.add(id)
    }

    override fun enforcePartStructure(id: Int) {
        enforcePart.add(id)
    }

    override fun work() {
        try {
            workService.submit { doWork() }.get()
            val mediaIds: List<Int> = ArrayList(enforceMedia)
            val partIds: List<Int> = ArrayList(enforcePart)
            enforcePart.clear()
            enforceMedia.clear()
            repository.updateDataStructure(mediaIds, partIds)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private abstract class LoaderManagerImpl<T>(
        loader: NetworkLoader<T>,
        loaded: Set<T>
    ) {
        val loading = Collections.synchronizedSet(HashSet<T>())
        val loaded: Set<T>
        val valueDependants: ConcurrentMap<T, DependantNode> = ConcurrentHashMap()
        val loader: NetworkLoader<T>

        fun addDependant(value: T, node: DependantNode?, optional: Boolean) {
            loading.add(value)
            addDependantNode(value, node, optional)
        }

        abstract fun addDependantNode(value: T, node: DependantNode?, optional: Boolean)

        fun isLoaded(set: Set<T>): Boolean {
            return loaded.containsAll(set)
        }

        fun isLoaded(value: T): Boolean {
            return loaded.contains(value)
        }

        fun removeId(value: T) {
            valueDependants.remove(value)
            loading.remove(value)
        }

        val freeIds: MutableSet<T>
            get() {
                val set: MutableSet<T> = HashSet()
                for ((key, value) in valueDependants) {
                    if (value.isFree) {
                        set.add(key)
                    }
                }
                return set
            }

        fun hasFreeIds(): Boolean {
            for ((_, value) in valueDependants) {
                if (value.isFree) {
                    return true
                }
            }
            return false
        }

        fun getIdDependant(value: T): DependantNode? {
            return valueDependants[value]
        }

        fun isLoading(value: T): Boolean {
            return loading.contains(value)
        }

        fun loading(): Int {
            return loading.size
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val that = other as LoaderManagerImpl<*>
            return loader == that.loader
        }

        override fun hashCode(): Int {
            return loader.hashCode()
        }

        init {
            Objects.requireNonNull(loader)
            Objects.requireNonNull(loaded)
            this.loader = loader
            this.loaded = loaded
        }
    }

    private class StringLoaderManager(loader: NetworkLoader<String>, loaded: Set<String>) :
        LoaderManagerImpl<String>(loader, loaded) {
        override fun addDependantNode(value: String, node: DependantNode?, optional: Boolean) {
            val parentNode = valueDependants.computeIfAbsent(
                value
            ) { stringId: String -> DependantNode(stringId) }
            if (node != null) {
                parentNode.addChild(node, optional)
            }
        }
    }

    private class IntLoaderManager(loader: NetworkLoader<Int>, loaded: Set<Int>) :
        LoaderManagerImpl<Int>(loader, loaded) {
        override fun addDependantNode(value: Int, node: DependantNode?, optional: Boolean) {
            val parentNode = valueDependants.computeIfAbsent(
                value
            ) { intId: Int -> DependantNode(intId) }
            if (node != null) {
                parentNode.addChild(node, optional)
            }
        }
    }
}

private typealias ProcessTasks = Function<Collection<DependencyTask<*>>, Collection<DependantNode>>