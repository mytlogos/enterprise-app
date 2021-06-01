package com.mytlogos.enterprise.worker

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.background.*
import com.mytlogos.enterprise.background.RepositoryImpl.Companion.getInstance
import com.mytlogos.enterprise.background.RepositoryImpl.Companion.instance
import com.mytlogos.enterprise.background.api.Client
import com.mytlogos.enterprise.background.api.NotConnectedException
import com.mytlogos.enterprise.background.api.ServerException
import com.mytlogos.enterprise.background.api.model.*
import com.mytlogos.enterprise.model.Toc
import com.mytlogos.enterprise.preferences.UserPreferences
import com.mytlogos.enterprise.tools.*
import org.joda.time.DateTime
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.function.Consumer
import java.util.stream.Collectors

class SynchronizeWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    private var notificationManager: NotificationManagerCompat? = null
    private var builder: NotificationCompat.Builder? = null
    private val syncNotificationId = 0x200
    private val progressListener = Consumer { progress: Int ->
        if (builder == null || notificationManager == null) {
            return@Consumer
        }
        val totalWork = instance.loadWorkerTotalWork
        builder!!.setProgress(totalWork, progress, totalWork < 0)
        notificationManager!!.notify(syncNotificationId, builder!!.build())
    }
    private val totalWorkListener = Consumer { totalWork: Int ->
        if (builder == null || notificationManager == null) {
            return@Consumer
        }
        val progress = instance.loadWorkerProgress
        builder!!.setProgress(totalWork, progress, totalWork < 0)
        notificationManager!!.notify(syncNotificationId, builder!!.build())
    }
    private var mediaAddedOrUpdated = 0
    private var partAddedOrUpdated = 0
    private var episodesAddedOrUpdated = 0
    private var releasesAddedOrUpdated = 0
    private var listsAddedOrUpdated = 0
    private var externalUserAddedOrUpdated = 0
    private var externalListAddedOrUpdated = 0
    private var newsAddedOrUpdated = 0
    private var mediaInWaitAddedOrUpdated = 0
    private var totalAddedOrUpdated = 0

    override fun doWork(): Result {
        if (this.applicationContext !is Application) {
            println("Context not instance of Application")
            return Result.failure()
        }
        val application = this.applicationContext as Application
        UserPreferences.init(application)
        try {
            val repository = getInstance(application)
            if (!repository.isClientAuthenticated) {
                cleanUp()
                return Result.retry()
            }
            notificationManager = NotificationManagerCompat.from(this.applicationContext)
            builder = NotificationCompat.Builder(this.applicationContext, CHANNEL_ID)
            builder!!
                .setContentTitle("Start Synchronizing")
                .setSmallIcon(R.mipmap.ic_launcher).priority = NotificationCompat.PRIORITY_DEFAULT
            notificationManager!!.notify(syncNotificationId, builder!!.build())
            //            if (syncWithInvalidation(repository)) return this.stopSynchronize();
            syncWithTime(repository)
            val builder = StringBuilder("Added or Updated:\n")
            append(builder, "Media: ", mediaAddedOrUpdated)
            append(builder, "Parts: ", partAddedOrUpdated)
            append(builder, "Episodes: ", episodesAddedOrUpdated)
            append(builder, "Releases: ", releasesAddedOrUpdated)
            append(builder, "MediaLists: ", listsAddedOrUpdated)
            append(builder, "ExternalUser: ", externalUserAddedOrUpdated)
            append(builder, "ExternalLists: ", externalListAddedOrUpdated)
            append(builder, "News: ", newsAddedOrUpdated)
            append(builder, "MediaInWait: ", mediaInWaitAddedOrUpdated)
            notify("Synchronization complete", builder.toString(), totalAddedOrUpdated, true)
        } catch (e: Exception) {
            val contentText: String = when (e) {
                is IOException -> {
                    when (e) {
                        is NotConnectedException -> {
                            "Not connected with Server"
                        }
                        is ServerException -> {
                            "Response with Error Message"
                        }
                        else -> {
                            "Error between App and Server"
                        }
                    }
                }
                else -> {
                    "Local Error"
                }
            }
            e.printStackTrace()
            val builder = StringBuilder("Added or Updated:\n")
            append(builder, "Media: ", mediaAddedOrUpdated)
            append(builder, "Parts: ", partAddedOrUpdated)
            append(builder, "Episodes: ", episodesAddedOrUpdated)
            append(builder, "Releases: ", releasesAddedOrUpdated)
            append(builder, "MediaLists: ", listsAddedOrUpdated)
            append(builder, "ExternalUser: ", externalUserAddedOrUpdated)
            append(builder, "ExternalLists: ", externalListAddedOrUpdated)
            append(builder, "News: ", newsAddedOrUpdated)
            append(builder, "MediaInWait: ", mediaInWaitAddedOrUpdated)
            notify(contentText, builder.toString(), 0, true)
            cleanUp()
            return Result.failure()
        }
        cleanUp()
        return Result.success()
    }

    private fun notify(title: String, contentText: String, finished: Int, changeContent: Boolean) {
        this.notify(title, contentText, changeContent, finished, finished)
    }

    private fun notify(
        title: String,
        contentText: String?,
        changeContent: Boolean,
        current: Int,
        total: Int
    ) {
        builder!!
            .setContentTitle(title)
            .setProgress(total, current, false)
        if (changeContent) {
            builder!!.setContentText(contentText)
        }
        notificationManager!!.notify(syncNotificationId, builder!!.build())
    }

    private fun notify(
        title: String,
        contentText: String?,
        indeterminate: Boolean,
        changeContent: Boolean
    ) {
        builder!!
            .setContentTitle(title)
            .setProgress(0, 0, indeterminate)
        if (changeContent) {
            builder!!.setContentText(contentText)
        }
        notificationManager!!.notify(syncNotificationId, builder!!.build())
    }

    @Throws(IOException::class)
    private fun syncWithTime(repository: Repository): Boolean {
        val client = repository.getClient(this)
        val persister = repository.getPersister(this)
        val lastSync = UserPreferences.lastSync
        syncChanged(lastSync, client, persister, repository)
        UserPreferences.lastSync = DateTime.now()
        syncDeleted(client, persister, repository)
        return false
    }

    private fun <T> mapStringToInt(map: Map<String, T>): MutableMap<Int, T> {
        @SuppressLint("UseSparseArrays") val result: MutableMap<Int, T> = HashMap()
        for ((key, value) in map) {
            result[key.toInt()] = value
        }
        return result
    }

    @Throws(IOException::class)
    private fun syncChanged(
        lastSync: DateTime,
        client: Client,
        persister: ClientModelPersister,
        repository: Repository
    ) {
        this.notify("Requesting New Data", null, indeterminate = true, changeContent = true)
        val changedEntitiesResponse = client.getNew(lastSync)
        val changedEntities = Utils.checkAndGetBody(changedEntitiesResponse)
        mediaAddedOrUpdated = changedEntities.media.size
        val mediaSize = mediaAddedOrUpdated
        partAddedOrUpdated = changedEntities.parts.size
        val partsSize = partAddedOrUpdated
        episodesAddedOrUpdated = changedEntities.episodes.size
        val episodesSize = episodesAddedOrUpdated
        releasesAddedOrUpdated = changedEntities.releases.size
        val releasesSize = releasesAddedOrUpdated
        listsAddedOrUpdated = changedEntities.lists.size
        val listsSize = listsAddedOrUpdated
        externalListAddedOrUpdated = changedEntities.extLists.size
        val extListSize = externalListAddedOrUpdated
        externalUserAddedOrUpdated = changedEntities.extUser.size
        val extUserSize = externalUserAddedOrUpdated
        newsAddedOrUpdated = changedEntities.news.size
        val newsSize = newsAddedOrUpdated
        mediaInWaitAddedOrUpdated = changedEntities.mediaInWait.size
        val mediaInWaitSize = mediaInWaitAddedOrUpdated
        totalAddedOrUpdated = (mediaSize + partsSize + episodesSize + releasesSize + listsSize
                + extListSize + extUserSize + newsSize + mediaInWaitSize)
        val total = totalAddedOrUpdated
        val builder = StringBuilder()
        append(builder, "Media: ", mediaSize)
        append(builder, "Parts: ", partsSize)
        append(builder, "Episodes: ", episodesSize)
        append(builder, "Releases: ", releasesSize)
        append(builder, "MediaLists: ", listsSize)
        append(builder, "ExternalUser: ", extUserSize)
        append(builder, "ExternalLists: ", extListSize)
        append(builder, "News: ", newsSize)
        append(builder, "MediaInWait: ", mediaInWaitSize)
        var current = 0
        this.notify("Received New Data", builder.toString(), true, current, total)
        this.notify("Persisting Media", null, false, current, total)
        // persist all new or updated entities, media to releases needs to be in this order
        persister.persistMedia(changedEntities.media)
        current += mediaSize
        changedEntities.media.clear()
        this.notify("Persisting Parts", null, false, current, total)
        persistPartsPure(changedEntities.parts, client, persister, repository)
        current += partsSize
        changedEntities.parts.clear()
        this.notify("Persisting Episodes", null, false, current, total)
        persistEpisodesPure(changedEntities.episodes, client, persister, repository)
        current += episodesSize
        changedEntities.episodes.clear()
        this.notify("Persisting Releases", null, false, current, total)
        persistReleases(changedEntities.releases, client, persister, repository)
        current += releasesSize
        changedEntities.releases.clear()
        this.notify("Persisting Lists", null, false, current, total)
        persister.persistUserLists(changedEntities.lists)
        current += listsSize
        changedEntities.lists.clear()
        this.notify("Persisting ExternalUser", null, false, current, total)
        persister.persistExternalUsersPure(changedEntities.extUser)
        current += extUserSize
        changedEntities.extUser.clear()
        this.notify("Persisting External Lists", null, false, current, total)
        persistExternalListsPure(changedEntities.extLists, client, persister, repository)
        current += extListSize
        changedEntities.extLists.clear()
        this.notify("Persisting unused Media", null, false, current, total)
        persister.persistMediaInWait(changedEntities.mediaInWait)
        current += mediaInWaitSize
        changedEntities.media.clear()
        this.notify("Persisting News", null, false, current, total)
        persister.persistNews(changedEntities.news)
        current += newsSize
        changedEntities.news.clear()
        this.notify("Saved all Changes", null, false, current, total)
    }

    private fun append(builder: StringBuilder, prefix: String, i: Int) {
        if (i > 0) {
            builder.append(prefix).append(i).append("\n")
        }
    }

    @Throws(IOException::class)
    private fun persistParts(
        parts: MutableList<ClientPart>,
        client: Client,
        persister: ClientModelPersister,
        repository: Repository
    ) {
        val missingIds: MutableCollection<Int?> = HashSet()
        val loadingParts: MutableCollection<ClientPart> = HashSet()
        parts.removeIf { part: ClientPart ->
            if (!repository.isMediumLoaded(part.mediumId)) {
                missingIds.add(part.mediumId)
                loadingParts.add(part)
                return@removeIf true
            }
            false
        }
        persister.persistParts(parts)
        if (missingIds.isEmpty()) {
            return
        }
        Utils.doPartitionedRethrow(missingIds) { ids: List<Int?>? ->
            val parents = Utils.checkAndGetBody(client.getMedia(ids))
                ?: throw NullPointerException("missing Media")
            val simpleMedia = parents.stream().map { medium: ClientMedium? ->
                ClientSimpleMedium(
                    medium!!
                )
            }.collect(Collectors.toList())
            persister.persistMedia(simpleMedia)
            false
        }
        persister.persistParts(loadingParts)
    }

    @Throws(IOException::class)
    private fun persistPartsPure(
        parts: List<ClientPartPure>,
        client: Client,
        persister: ClientModelPersister,
        repository: Repository
    ) {
        val unPureParts = parts
            .stream()
            .map { part: ClientPartPure ->
                ClientPart(
                    part.mediumId,
                    part.id,
                    part.title,
                    part.totalIndex,
                    part.partialIndex,
                    null
                )
            }
            .collect(Collectors.toList())
        persistParts(unPureParts, client, persister, repository)
    }

    @Throws(IOException::class)
    private fun persistEpisodes(
        episodes: MutableList<ClientEpisode>,
        client: Client,
        persister: ClientModelPersister,
        repository: Repository
    ) {
        val missingIds: MutableCollection<Int> = HashSet()
        val loading: MutableCollection<ClientEpisode> = HashSet()
        episodes.removeIf { value: ClientEpisode ->
            if (!repository.isPartLoaded(value.partId)) {
                missingIds.add(value.partId)
                loading.add(value)
                return@removeIf true
            }
            false
        }
        persister.persistEpisodes(episodes)
        if (missingIds.isEmpty()) {
            return
        }
        Utils.doPartitionedRethrow(missingIds) { ids: MutableList<Int> ->
            val parents = Utils.checkAndGetBody(client.getParts(ids))
                ?: throw NullPointerException("missing Parts")
            persistParts(parents, client, persister, repository)
            false
        }
        persister.persistEpisodes(loading)
    }

    @Throws(IOException::class)
    private fun persistEpisodesPure(
        episodes: List<ClientEpisodePure>,
        client: Client,
        persister: ClientModelPersister,
        repository: Repository
    ) {
        val unPure = episodes
            .stream()
            .map { part: ClientEpisodePure ->
                ClientEpisode(
                    part.id,
                    part.progress,
                    part.partId,
                    part.totalIndex,
                    part.partialIndex,
                    part.combiIndex,
                    part.readDate,
                    arrayOf()
                )
            }
            .collect(Collectors.toList())
        persistEpisodes(unPure, client, persister, repository)
    }

    @Throws(IOException::class)
    private fun persistReleases(
        releases: MutableCollection<ClientRelease>,
        client: Client,
        persister: ClientModelPersister,
        repository: Repository
    ) {
        val missingIds: MutableCollection<Int> = HashSet()
        val loading: MutableCollection<ClientRelease> = HashSet()
        releases.removeIf { value: ClientRelease ->
            if (!repository.isEpisodeLoaded(value.episodeId)) {
                missingIds.add(value.episodeId)
                loading.add(value)
                return@removeIf true
            }
            false
        }
        persister.persistReleases(releases)
        if (missingIds.isEmpty()) {
            return
        }
        Utils.doPartitionedRethrow(missingIds) { ids: List<Int> ->
            val parents = Utils.checkAndGetBody(client.getEpisodes(ids))
                ?: throw NullPointerException("missing Episodes")
            persistEpisodes(parents, client, persister, repository)
            false
        }
        persister.persistReleases(loading)
    }

    @Throws(IOException::class)
    private fun persistExternalLists(
        externalMediaLists: MutableList<ClientExternalMediaList>,
        client: Client,
        persister: ClientModelPersister,
        repository: Repository
    ) {
        val missingIds: MutableCollection<String?> = HashSet()
        val loading: MutableCollection<ClientExternalMediaList> = HashSet()
        externalMediaLists.removeIf { value: ClientExternalMediaList ->
            if (!repository.isExternalUserLoaded(value.uuid)) {
                missingIds.add(value.uuid)
                loading.add(value)
                return@removeIf true
            }
            false
        }
        persister.persistExternalMediaLists(externalMediaLists)
        if (missingIds.isEmpty()) {
            return
        }
        Utils.doPartitionedRethrow(missingIds) { ids: List<String?>? ->
            val parents = Utils.checkAndGetBody(client.getExternalUser(ids))
                ?: throw NullPointerException("missing ExternalUser")
            persister.persistExternalUsers(parents)
            false
        }
        persister.persistExternalMediaLists(loading)
    }

    @Throws(IOException::class)
    private fun persistExternalListsPure(
        externalMediaLists: List<ClientExternalMediaListPure>,
        client: Client,
        persister: ClientModelPersister,
        repository: Repository
    ) {
        val unPure = externalMediaLists
            .stream()
            .map { part: ClientExternalMediaListPure ->
                ClientExternalMediaList(
                    part.uuid,
                    part.id,
                    part.name,
                    part.medium,
                    part.url, IntArray(0)
                )
            }
            .collect(Collectors.toList())
        persistExternalLists(unPure, client, persister, repository)
    }

    @Throws(IOException::class)
    private fun syncDeleted(
        client: Client,
        persister: ClientModelPersister,
        repository: Repository
    ) {
        notify("Synchronize Deleted Items", null, indeterminate = true, changeContent = false)
        val statBody = Utils.checkAndGetBody(client.stats)
        val parsedStat = statBody.parse()
        persister.persist(parsedStat)
        var reloadStat = repository.checkReload(parsedStat)
        if (!reloadStat.loadMedium.isEmpty()) {
            val media = Utils.checkAndGetBody(client.getMedia(reloadStat.loadMedium))
            val simpleMedia = media.stream().map { medium: ClientMedium? ->
                ClientSimpleMedium(
                    medium!!
                )
            }.collect(Collectors.toList())
            persister.persistMedia(simpleMedia)
            reloadStat = repository.checkReload(parsedStat)
        }
        if (!reloadStat.loadExUser.isEmpty()) {
            val users = Utils.checkAndGetBody(client.getExternalUser(reloadStat.loadExUser))
            persister.persistExternalUsers(users)
            reloadStat = repository.checkReload(parsedStat)
        }
        if (!reloadStat.loadLists.isEmpty()) {
            val listQuery = Utils.checkAndGetBody(client.getLists(reloadStat.loadLists))
            persister.persist(listQuery)
            reloadStat = repository.checkReload(parsedStat)
        }
        if (!reloadStat.loadPart.isEmpty()) {
            Utils.doPartitionedRethrow(reloadStat.loadPart) { partIds: List<Int> ->
                val parts = Utils.checkAndGetBody(client.getParts(partIds))
                persister.persistParts(parts)
                false
            }
            reloadStat = repository.checkReload(parsedStat)
        }
        if (!reloadStat.loadPartEpisodes.isEmpty()) {
            val partStringEpisodes =
                Utils.checkAndGetBody(client.getPartEpisodes(reloadStat.loadPartEpisodes))
            val partEpisodes: MutableMap<Int, List<Int>> = mapStringToInt(partStringEpisodes)
            val missingEpisodes: MutableCollection<Int> = HashSet()

            for ((_, value) in partEpisodes) {
                for (episodeId in value) {
                    if (!repository.isEpisodeLoaded(episodeId)) {
                        missingEpisodes.add(episodeId)
                    }
                }
            }

            if (!missingEpisodes.isEmpty()) {
                val episodes = Utils.checkAndGetBody(
                    client.getEpisodes(missingEpisodes)
                )
                persistEpisodes(episodes, client, persister, repository)
            }

            Utils.doPartitionedRethrow(partEpisodes.keys) { ids: List<Int> ->
                val currentEpisodes: MutableMap<Int, List<Int>> = HashMap()
                for (id in ids) {
                    currentEpisodes[id] = partEpisodes[id] ?: throw IllegalStateException("Expected List not found")
                }
                persister.deleteLeftoverEpisodes(currentEpisodes)
                false
            }
            reloadStat = repository.checkReload(parsedStat)
        }

        if (!reloadStat.loadPartReleases.isEmpty()) {
            val partReleasesResponse = client.getPartReleases(reloadStat.loadPartReleases)
            val partStringReleases = Utils.checkAndGetBody(partReleasesResponse)
            val partReleases = mapStringToInt(partStringReleases)
            val missingEpisodes: MutableCollection<Int> = HashSet()
            for ((_, value) in partReleases) {
                for (release in value) {
                    if (!repository.isEpisodeLoaded(release.id)) {
                        missingEpisodes.add(release.id)
                    }
                }
            }
            if (!missingEpisodes.isEmpty()) {
                val episodes = Utils.checkAndGetBody(
                    client.getEpisodes(missingEpisodes)
                )
                persistEpisodes(episodes, client, persister, repository)
            }
            val episodesToLoad: Collection<Int> = persister.deleteLeftoverReleases(partReleases)
            if (!episodesToLoad.isEmpty()) {
                Utils.doPartitionedRethrow(episodesToLoad) { ids: List<Int> ->
                    val episodes =
                        Utils.checkAndGetBody(client.getEpisodes(ids))
                    persistEpisodes(episodes, client, persister, repository)
                    false
                }
            }
            reloadStat = repository.checkReload(parsedStat)
        }
        if (!reloadStat.loadMediumTocs.isEmpty()) {
            val mediumTocsResponse = client.getMediumTocs(reloadStat.loadMediumTocs)
            val mediumTocs = Utils.checkAndGetBody(mediumTocsResponse)
            val mediaTocs: MutableMap<Int, MutableList<String>> = HashMap()
            for (mediumToc in mediumTocs) {
                mediaTocs.computeIfAbsent(mediumToc.mediumId) { ArrayList() }
                    .add(mediumToc.link)
            }
            Utils.doPartitionedRethrow(mediaTocs.keys) { mediaIds: List<Int> ->
                val partitionedMediaTocs: MutableMap<Int, List<String>> = HashMap()
                for (mediaId in mediaIds) {
                    partitionedMediaTocs[mediaId] = Objects.requireNonNull<List<String>>(
                        mediaTocs[mediaId]
                    )
                }
                persistTocs(partitionedMediaTocs, persister)
                persister.deleteLeftoverTocs(partitionedMediaTocs)
                false
            }
        }

        // as even now some errors crop up, just load all this shit and dump it in 100er steps
        if (!reloadStat.loadPartEpisodes.isEmpty() || !reloadStat.loadPartReleases.isEmpty()) {
            val partsToLoad: MutableCollection<Int> = HashSet()
            partsToLoad.addAll(reloadStat.loadPartEpisodes)
            partsToLoad.addAll(reloadStat.loadPartReleases)
            Utils.doPartitionedRethrow(partsToLoad) { ids: List<Int> ->
                val parts = Utils.checkAndGetBody(client.getParts(ids))
                persistParts(parts, client, persister, repository)
                false
            }
            reloadStat = repository.checkReload(parsedStat)
        }
        if (!reloadStat.loadPartEpisodes.isEmpty()) {
            @SuppressLint("DefaultLocale") val msg = String.format(
                "Episodes of %d Parts to load even after running once",
                reloadStat.loadPartEpisodes.size
            )
            println(msg)
            Log.e("Repository", msg)
        }
        if (!reloadStat.loadPartReleases.isEmpty()) {
            @SuppressLint("DefaultLocale") val msg = String.format(
                "Releases of %d Parts to load even after running once",
                reloadStat.loadPartReleases.size
            )
            println(msg)
            Log.e("Repository", msg)
        }
    }

    private fun persistTocs(mediaTocs: Map<Int, List<String>>, persister: ClientModelPersister) {
        val inserts: MutableList<Toc> = LinkedList()
        for ((mediumId, value) in mediaTocs) {
            for (tocLink in value) {
                inserts.add(SimpleToc(mediumId, tocLink))
            }
        }
        persister.persistTocs(inserts)
    }

    @Throws(IOException::class)
    fun syncWithInvalidation(repository: Repository): Boolean {
        repository.syncUser()
        if (this.isStopped) {
            return true
        }
        builder!!.setContentText("Updating Content data")
        notificationManager!!.notify(syncNotificationId, builder!!.build())
        repository.loadInvalidated()
        if (this.isStopped) {
            return true
        }
        builder!!.setContentText("Loading new Media")
        notificationManager!!.notify(syncNotificationId, builder!!.build())
        repository.loadAllMedia()
        return false
    }

    private fun stopSynchronize(): Result {
        notificationManager!!.notify(
            syncNotificationId,
            builder!!
                .setContentTitle("Synchronization stopped")
                .setContentText(null)
                .build()
        )
        cleanUp()
        return Result.failure()
    }

    private fun cleanUp() {
        val repository = instance
        repository.removeProgressListener(progressListener)
        repository.removeTotalWorkListener(totalWorkListener)
        repository.syncProgress()
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        if (notificationManager != null) {
            notificationManager!!.cancel(syncNotificationId)
        }
        uuid = null
    }

    companion object {
        const val SYNCHRONIZE_WORKER = "SYNCHRONIZE_WORKER"

        // TODO: 08.08.2019 use this for sdk >= 28
        private const val CHANNEL_ID = "SYNC_CHANNEL"

        @Volatile
        private var uuid: UUID? = null

        @JvmStatic
        fun enqueueOneTime(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()
            val workRequest = OneTimeWorkRequest.Builder(SynchronizeWorker::class.java)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context)
                .beginUniqueWork(SYNCHRONIZE_WORKER, ExistingWorkPolicy.REPLACE, workRequest)
                .enqueue()
        }

        fun stopWorker(application: Application) {
            if (uuid == null) {
                return
            }
            WorkManager.getInstance(application).cancelWorkById(uuid!!)
        }

        fun isRunning(application: Application): Boolean {
            if (uuid == null) {
                return false
            }
            val infoFuture = WorkManager.getInstance(
                application
            ).getWorkInfoById(uuid!!)
            try {
                return infoFuture.get()?.state == WorkInfo.State.RUNNING
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            return false
        }
    }

    init {
        uuid = this.id
    }
}