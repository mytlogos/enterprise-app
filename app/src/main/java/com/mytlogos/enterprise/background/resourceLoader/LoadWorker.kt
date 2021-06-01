package com.mytlogos.enterprise.background.resourceLoader

import com.mytlogos.enterprise.background.ClientModelPersister
import com.mytlogos.enterprise.background.DependantGenerator
import com.mytlogos.enterprise.background.LoadData
import com.mytlogos.enterprise.background.Repository
import java.util.*
import java.util.function.Consumer

abstract class LoadWorker {
    val NEWS_LOADER: NewsLoader
    @kotlin.jvm.JvmField
    val EPISODE_LOADER: EpisodeLoader
    @kotlin.jvm.JvmField
    val PART_LOADER: PartLoader
    @kotlin.jvm.JvmField
    val MEDIUM_LOADER: MediumLoader
    val MEDIALIST_LOADER: MediaListLoader
    @kotlin.jvm.JvmField
    val EXTERNAL_MEDIALIST_LOADER: ExtMediaListLoader
    @kotlin.jvm.JvmField
    val EXTERNAL_USER_LOADER: ExtUserLoader
    val repository: Repository
    @kotlin.jvm.JvmField
    val persister: ClientModelPersister
    @kotlin.jvm.JvmField
    val loadedData: LoadData
    @kotlin.jvm.JvmField
    val generator: DependantGenerator?
    private val progressListener = Collections.synchronizedSet(HashSet<Consumer<Int>>())
    private val totalWorkListener = Collections.synchronizedSet(HashSet<Consumer<Int>>())
    var progress = 0
        private set
    var totalWork = -1
        private set

    constructor(
        repository: Repository,
        persister: ClientModelPersister,
        loadedData: LoadData,
        generator: DependantGenerator?
    ) {
        this.repository = repository
        this.persister = persister
        this.loadedData = loadedData
        this.generator = generator
        EXTERNAL_USER_LOADER = ExtUserLoader(this)
        EXTERNAL_MEDIALIST_LOADER = ExtMediaListLoader(this)
        MEDIALIST_LOADER = MediaListLoader(this)
        MEDIUM_LOADER = MediumLoader(this)
        PART_LOADER = PartLoader(this)
        EPISODE_LOADER = EpisodeLoader(this)
        NEWS_LOADER = NewsLoader(this)
        worker = this
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
    ) {
        this.repository = repository
        this.persister = persister
        this.loadedData = loadedData
        EXTERNAL_USER_LOADER = extUserLoader
        EXTERNAL_MEDIALIST_LOADER = external_medialist_loader
        MEDIALIST_LOADER = medialist_loader
        MEDIUM_LOADER = medium_loader
        PART_LOADER = part_loader
        EPISODE_LOADER = episode_loader
        NEWS_LOADER = news_loader
        this.generator = generator
        worker = this
    }

    fun addIntegerIdTask(id: Int, value: DependantValue?, loader: NetworkLoader<Int>) {
        this.addIntegerIdTask(id, value, loader, false)
    }

    open fun addIntegerIdTask(
        id: Int,
        value: DependantValue?,
        loader: NetworkLoader<Int>,
        optional: Boolean
    ) {
    }

    fun addStringIdTask(id: String, value: DependantValue?, loader: NetworkLoader<String>) {
        this.addStringIdTask(id, value, loader, false)
    }

    open fun addStringIdTask(
        id: String,
        value: DependantValue?,
        loader: NetworkLoader<String>,
        optional: Boolean
    ) {
    }

    @Deprecated("")
    abstract fun addIntegerIdTask(
        id: Int,
        dependantValue: Any?,
        loader: NetworkLoader<Int>,
        runnable: Runnable?,
        optional: Boolean
    )

    @Deprecated("")
    fun addIntegerIdTask(
        id: Int,
        dependantValue: Any?,
        loader: NetworkLoader<Int>,
        runnable: Runnable?
    ) {
        checkIdLoader(id, loader)
        this.addIntegerIdTask(id, dependantValue, loader, runnable, false)
    }

    @Deprecated("")
    fun addIntegerIdTask(
        id: Int,
        dependantValue: Any?,
        loader: NetworkLoader<Int>,
        optional: Boolean
    ) {
        checkIdLoader(id, loader)
        this.addIntegerIdTask(id, dependantValue, loader, null, optional)
    }

    @Deprecated("")
    fun addIntegerIdTask(id: Int, dependantValue: Any?, loader: NetworkLoader<Int>) {
        Objects.requireNonNull(dependantValue)
        checkIdLoader(id, loader)
        this.addIntegerIdTask(id, dependantValue, loader, null, false)
    }

    @Deprecated("")
    fun addIntegerIdTask(id: Int, loader: NetworkLoader<Int>) {
        this.addIntegerIdTask(id, null, loader, null, false)
    }

    @Deprecated("")
    abstract fun addStringIdTask(
        id: String,
        dependantValue: Any?,
        loader: NetworkLoader<String>,
        runnable: Runnable?,
        optional: Boolean
    )

    @Deprecated("")
    fun addStringIdTask(
        id: String,
        dependantValue: Any?,
        loader: NetworkLoader<String>,
        runnable: Runnable?
    ) {
        checkIdLoader(id, loader)
        this.addStringIdTask(id, dependantValue, loader, runnable, false)
    }

    @Deprecated("")
    fun addStringIdTask(
        id: String,
        dependantValue: Any?,
        loader: NetworkLoader<String>,
        optional: Boolean
    ) {
        checkIdLoader(id, loader)
        this.addStringIdTask(id, dependantValue, loader, null, optional)
    }

    @Deprecated("")
    fun addStringIdTask(id: String, dependantValue: Any?, loader: NetworkLoader<String>) {
        Objects.requireNonNull(dependantValue)
        checkIdLoader(id, loader)
        this.addStringIdTask(id, dependantValue, loader, null, false)
    }

    @Deprecated("")
    fun addStringIdTask(id: String, loader: NetworkLoader<String>) {
        checkIdLoader(id, loader)
        this.addStringIdTask(id, null, loader, null, false)
    }

    private fun checkIdLoader(id: String, loader: NetworkLoader<String>) {
        Objects.requireNonNull(loader)
        Objects.requireNonNull(id)
        require(!id.isEmpty()) { "empty id is invalid" }
    }

    private fun checkIdLoader(id: Int, loader: NetworkLoader<Int>) {
        Objects.requireNonNull(loader)
        require(id > 0) { "invalid id: $id" }
    }

    abstract fun isEpisodeLoading(id: Int): Boolean
    abstract fun isPartLoading(id: Int): Boolean
    abstract fun isMediumLoading(id: Int): Boolean
    abstract fun isMediaListLoading(id: Int): Boolean
    abstract fun isExternalMediaListLoading(id: Int): Boolean
    abstract fun isExternalUserLoading(uuid: String): Boolean
    abstract fun isNewsLoading(id: Int): Boolean
    abstract fun doWork()
    abstract fun work()
    fun addProgressListener(consumer: Consumer<Int>) {
        progressListener.add(consumer)
        consumer.accept(progress)
    }

    fun removeProgressListener(consumer: Consumer<Int>) {
        progressListener.remove(consumer)
    }

    fun addTotalWorkListener(consumer: Consumer<Int>) {
        progressListener.add(consumer)
        consumer.accept(progress)
    }

    fun removeTotalWorkListener(consumer: Consumer<Int>) {
        progressListener.remove(consumer)
    }

    protected fun updateProgress(progress: Int) {
        this.progress = progress
        for (consumer in progressListener) {
            consumer.accept(progress)
        }
    }

    protected fun updateTotalWork(total: Int) {
        totalWork = total
        for (consumer in totalWorkListener) {
            consumer.accept(total)
        }
    }

    open fun enforceMediumStructure(id: Int) {}
    open fun enforcePartStructure(id: Int) {}

    companion object {
        @kotlin.jvm.JvmStatic
        var worker: LoadWorker? = null
            private set
    }
}