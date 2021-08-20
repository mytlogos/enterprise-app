package com.mytlogos.enterprise.worker

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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.joda.time.DateTime
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutionException

@Suppress("BlockingMethodInNonBlockingContext")
class SynchronizeWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var builder: NotificationCompat.Builder
    private val syncNotificationId = 0x200
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

    override suspend fun doWork(): Result {
        if (this.applicationContext !is Application) {
            println("Context not instance of Application")
            return Result.failure()
        }
        val application = this.applicationContext as Application
        UserPreferences.init(application)

        try {
            initNotification()
            val repository = getInstance(application)

            if (!repository.isClientAuthenticated) {
                cleanUp()
                return Result.retry()
            }
            builder
                .setContentTitle("Start Synchronizing")
                .setSmallIcon(R.mipmap.ic_launcher).priority = NotificationCompat.PRIORITY_DEFAULT
            notificationManager.notify(syncNotificationId, builder.build())

            syncWithTime(repository)

            notifyFinish("Synchronization complete", totalAddedOrUpdated)

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
            notifyFinish(contentText, 0)
            cleanUp()
            return Result.failure()
        }
        cleanUp()
        return Result.success()
    }

    private fun initNotification() {
        notificationManager = NotificationManagerCompat.from(this.applicationContext)
        builder = NotificationCompat.Builder(this.applicationContext, CHANNEL_ID)
    }

    private fun notifyFinish(contentText: String, finished: Int) {
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
        notify(contentText, builder.toString(), finished)
    }

    private fun notify(
        title: String,
        contentText: String,
        finished: Int,
        changeContent: Boolean = true,
    ) {
        this.notifyProgress(title, contentText, changeContent, finished, finished)
    }

    private fun notifyProgress(
        title: String,
        contentText: String?,
        changeContent: Boolean,
        current: Int,
        total: Int,
    ) {
        builder
            .setContentTitle(title)
            .setProgress(total, current, false)
        if (changeContent) {
            builder.setContentText(contentText)
        }
        notificationManager.notify(syncNotificationId, builder.build())
    }

    private fun notifyIndeterminate(title: String, removeContent: Boolean) {
        builder
            .setContentTitle(title)
            .setProgress(0, 0, true)

        if (removeContent) {
            builder.setContentText(null)
        }
        notificationManager.notify(syncNotificationId, builder.build())
    }

    @Throws(IOException::class)
    private suspend fun syncWithTime(repository: Repository): Boolean {
        val client = repository.getClient()
        val persister = repository.getPersister()
        val lastSync = UserPreferences.lastSync
        syncChanged(lastSync, client, persister, repository)
        UserPreferences.lastSync = DateTime.now()
        syncDeleted(client, persister, repository)
        return false
    }

    private fun <T> mapStringToInt(map: Map<String, T>): MutableMap<Int, T> {
        val result: MutableMap<Int, T> = HashMap()
        for ((key, value) in map) {
            result[key.toInt()] = value
        }
        return result
    }

    @Throws(IOException::class)
    private suspend fun syncChanged(
        lastSync: DateTime,
        client: Client,
        persister: ClientModelPersister,
        repository: Repository,
    ) {
        this.notifyIndeterminate("Requesting New Data", removeContent = true)
        val changedEntitiesResponse = client.getNew(lastSync)
        val changedEntities = changedEntitiesResponse.checkAndGetBody()
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
        this.notifyProgress("Received New Data", builder.toString(), true, current, total)
        this.notifyProgress("Persisting Media", null, false, current, total)
        // persist all new or updated entities, media to releases needs to be in this order
        persister.persistMedia(changedEntities.media)
        current += mediaSize
        changedEntities.media.clear()
        this.notifyProgress("Persisting Parts", null, false, current, total)
        persistPartsPure(changedEntities.parts, client, persister, repository)
        current += partsSize
        changedEntities.parts.clear()
        this.notifyProgress("Persisting Episodes", null, false, current, total)
        persistEpisodesPure(changedEntities.episodes, client, persister, repository)
        current += episodesSize
        changedEntities.episodes.clear()
        this.notifyProgress("Persisting Releases", null, false, current, total)
        persistReleases(changedEntities.releases, client, persister, repository)
        current += releasesSize
        changedEntities.releases.clear()
        this.notifyProgress("Persisting Lists", null, false, current, total)
        persister.persistUserLists(changedEntities.lists)
        current += listsSize
        changedEntities.lists.clear()
        this.notifyProgress("Persisting ExternalUser", null, false, current, total)
        persister.persistExternalUsersPure(changedEntities.extUser)
        current += extUserSize
        changedEntities.extUser.clear()
        this.notifyProgress("Persisting External Lists", null, false, current, total)
        persistExternalListsPure(changedEntities.extLists, client, persister, repository)
        current += extListSize
        changedEntities.extLists.clear()
        this.notifyProgress("Persisting unused Media", null, false, current, total)
        persister.persistMediaInWait(changedEntities.mediaInWait)
        current += mediaInWaitSize
        changedEntities.media.clear()
        this.notifyProgress("Persisting News", null, false, current, total)
        persister.persistNews(changedEntities.news)
        current += newsSize
        changedEntities.news.clear()
        this.notifyProgress("Saved all Changes", null, false, current, total)
    }

    private fun append(builder: StringBuilder, prefix: String, i: Int) {
        if (i > 0) {
            builder.append(prefix).append(i).append("\n")
        }
    }

    @Throws(IOException::class)
    private suspend fun persistParts(
        parts: MutableList<ClientPart>,
        client: Client,
        persister: ClientModelPersister,
        repository: Repository,
    ) {
        val missingIds: MutableCollection<Int> = HashSet()
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
        coroutineScope {
            missingIds.doPartitionedRethrowSuspend { ids: List<Int> ->
                async {
                    val parents = client.getMedia(ids).checkAndGetBody()
                    val simpleMedia = parents.map { medium: ClientMedium ->
                        ClientSimpleMedium(medium)
                    }
                    persister.persistMedia(simpleMedia)
                    false
                }
            }
        }
        persister.persistParts(loadingParts)
    }

    @Throws(IOException::class)
    private suspend fun persistPartsPure(
        parts: List<ClientPartPure>,
        client: Client,
        persister: ClientModelPersister,
        repository: Repository,
    ) {
        val unPureParts = parts
            .map { part: ClientPartPure ->
                ClientPart(
                    part.mediumId,
                    part.id,
                    part.title,
                    part.totalIndex,
                    part.partialIndex,
                    null
                )
            }.toMutableList()
        persistParts(unPureParts, client, persister, repository)
    }

    @Throws(IOException::class)
    private suspend fun persistEpisodes(
        episodes: MutableList<ClientEpisode>,
        client: Client,
        persister: ClientModelPersister,
        repository: Repository,
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
        coroutineScope {
            missingIds.doPartitionedRethrowSuspend { ids: List<Int> ->
                async {
                    val parents = client.getParts(ids).checkAndGetBody()
                    persistParts(parents, client, persister, repository)
                    false
                }
            }
        }
        persister.persistEpisodes(loading)
    }

    @Throws(IOException::class)
    private suspend fun persistEpisodesPure(
        episodes: List<ClientEpisodePure>,
        client: Client,
        persister: ClientModelPersister,
        repository: Repository,
    ) {
        val unPure = episodes
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
            }.toMutableList()
        persistEpisodes(unPure, client, persister, repository)
    }

    @Throws(IOException::class)
    private suspend fun persistReleases(
        releases: MutableCollection<ClientRelease>,
        client: Client,
        persister: ClientModelPersister,
        repository: Repository,
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
        coroutineScope {
            missingIds.doPartitionedRethrowSuspend { ids: List<Int> ->
                async {
                    val parents = client.getEpisodes(ids).checkAndGetBody()
                    persistEpisodes(parents, client, persister, repository)
                    false
                }
            }
        }
        persister.persistReleases(loading)
    }

    @Throws(IOException::class)
    private suspend fun persistExternalLists(
        externalMediaLists: MutableList<ClientExternalMediaList>,
        client: Client,
        persister: ClientModelPersister,
        repository: Repository,
    ) {
        val missingIds: MutableCollection<String> = HashSet()
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
        coroutineScope {
            missingIds.doPartitionedRethrowSuspend { ids: List<String> ->
                async {
                    val parents = client.getExternalUser(ids).checkAndGetBody()
                    persister.persistExternalUsers(parents)
                    false
                }
            }
        }
        persister.persistExternalMediaLists(loading)
    }

    @Throws(IOException::class)
    private suspend fun persistExternalListsPure(
        externalMediaLists: List<ClientExternalMediaListPure>,
        client: Client,
        persister: ClientModelPersister,
        repository: Repository,
    ) {
        val unPure = externalMediaLists
            .map { part: ClientExternalMediaListPure ->
                ClientExternalMediaList(
                    part.uuid,
                    part.id,
                    part.name,
                    part.medium,
                    part.url, IntArray(0)
                )
            }.toMutableList()
        persistExternalLists(unPure, client, persister, repository)
    }

    @Throws(IOException::class)
    private suspend fun syncDeleted(
        client: Client,
        persister: ClientModelPersister,
        repository: Repository,
    ) {
        notifyIndeterminate("Synchronize Deleted Items", removeContent = false)

        val statBody = client.getStats().checkAndGetBody()
        val parsedStat = statBody.parse()

        reloadRun(client, persister, repository, parsedStat)

        persister.persist(parsedStat)

        var reloadStat = reloadRun(client, persister, repository, parsedStat)

        // as even now some errors crop up, just load all this shit and dump it in 100er steps
        if (!reloadStat.loadPartEpisodes.isEmpty() || !reloadStat.loadPartReleases.isEmpty()) {
            val partsToLoad: MutableCollection<Int> = HashSet()
            partsToLoad.addAll(reloadStat.loadPartEpisodes)
            partsToLoad.addAll(reloadStat.loadPartReleases)
            coroutineScope {
                partsToLoad.doPartitionedRethrowSuspend { ids: List<Int> ->
                    async {
                        val parts = client.getParts(ids).checkAndGetBody()
                        persistParts(parts, client, persister, repository)
                        false
                    }
                }
            }
            reloadStat = repository.checkReload(parsedStat)
        }

        if (!reloadStat.loadPartEpisodes.isEmpty()) {
            val size = reloadStat.loadPartEpisodes.size
            val msg = "Episodes of $size Parts to load even after running once"
            println(msg)
            Log.e("Repository", msg)
        }

        if (!reloadStat.loadPartReleases.isEmpty()) {
            val size = reloadStat.loadPartReleases.size
            val msg = "Releases of $size Parts to load even after running once"
            println(msg)
            Log.e("Repository", msg)
        }
    }

    private suspend fun reloadRun(
        client: Client,
        persister: ClientModelPersister,
        repository: Repository,
        parsedStat: ClientStat.ParsedStat,
    ): ReloadStat {
        var reloadStat = repository.checkReload(parsedStat)

        if (!reloadStat.loadMedium.isEmpty()) {
            reloadStat = reloadMedium(client, reloadStat, persister, repository, parsedStat)
        }

        if (!reloadStat.loadExUser.isEmpty()) {
            reloadStat = reloadExternalUser(client, reloadStat, persister, repository, parsedStat)
        }

        if (!reloadStat.loadLists.isEmpty()) {
            reloadStat = reloadMediaList(client, reloadStat, persister, repository, parsedStat)
        }

        if (!reloadStat.loadPart.isEmpty()) {
            reloadStat = reloadParts(reloadStat, client, persister, repository, parsedStat)
        }

        if (!reloadStat.loadPartEpisodes.isEmpty()) {
            reloadStat = reloadPartEpisodes(client, reloadStat, repository, persister, parsedStat)
        }

        if (!reloadStat.loadPartReleases.isEmpty()) {
            reloadStat = reloadPartReleases(client, reloadStat, repository, persister, parsedStat)
        }

        if (!reloadStat.loadMediumTocs.isEmpty()) {
            reloadMediumTocs(client, reloadStat, persister)
        }
        return reloadStat
    }

    private suspend fun reloadMediumTocs(
        client: Client,
        reloadStat: ReloadStat,
        persister: ClientModelPersister,
    ) {
        val mediumTocsResponse = client.getMediumTocs(reloadStat.loadMediumTocs)
        val mediumTocs = mediumTocsResponse.checkAndGetBody()
        val mediaTocs: MutableMap<Int, MutableList<String>> = HashMap()
        for (mediumToc in mediumTocs) {
            mediaTocs.computeIfAbsent(mediumToc.mediumId) { ArrayList() }
                .add(mediumToc.link)
        }
        coroutineScope {
            mediaTocs.keys.doPartitionedRethrowSuspend { mediaIds: List<Int> ->
                async {
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
        }
    }

    private suspend fun reloadPartReleases(
        client: Client,
        reloadStat: ReloadStat,
        repository: Repository,
        persister: ClientModelPersister,
        parsedStat: ClientStat.ParsedStat,
    ): ReloadStat {
        val partStringReleases = client.getPartReleases(
            reloadStat.loadPartReleases
        ).checkAndGetBody()

        val partReleases = mapStringToInt(partStringReleases)
        val missingEpisodes: MutableCollection<Int> = HashSet()

        for ((_, value) in partReleases) {
            for (release in value) {
                if (!repository.isEpisodeLoaded(release.episodeId)) {
                    missingEpisodes.add(release.episodeId)
                }
            }
        }

        if (!missingEpisodes.isEmpty()) {
            val episodes = client.getEpisodes(missingEpisodes).checkAndGetBody()
            persistEpisodes(episodes, client, persister, repository)
        }

        val episodesToLoad: Collection<Int> = persister.deleteLeftoverReleases(partReleases)

        if (!episodesToLoad.isEmpty()) {
            coroutineScope {
                episodesToLoad.doPartitionedRethrowSuspend { ids: List<Int> ->
                    async {
                        val episodes = client.getEpisodes(ids).checkAndGetBody()
                        persistEpisodes(episodes, client, persister, repository)
                        false
                    }
                }
            }
        }
        return repository.checkReload(parsedStat)
    }

    private suspend fun reloadPartEpisodes(
        client: Client,
        reloadStat: ReloadStat,
        repository: Repository,
        persister: ClientModelPersister,
        parsedStat: ClientStat.ParsedStat,
    ): ReloadStat {
        val partStringEpisodes = client.getPartEpisodes(
            reloadStat.loadPartEpisodes
        ).checkAndGetBody()

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
            val episodes = client.getEpisodes(missingEpisodes).checkAndGetBody()
            persistEpisodes(episodes, client, persister, repository)
        }

        coroutineScope {
            partEpisodes.keys.doPartitionedRethrowSuspend { ids: List<Int> ->
                async {
                    val currentEpisodes: MutableMap<Int, List<Int>> = HashMap()
                    for (id in ids) {
                        currentEpisodes[id] =
                            partEpisodes[id]
                                ?: throw IllegalStateException("Expected List not found")
                    }
                    persister.deleteLeftoverEpisodes(currentEpisodes)
                    false
                }
            }
        }
        return repository.checkReload(parsedStat)
    }

    private suspend fun reloadParts(
        reloadStat: ReloadStat,
        client: Client,
        persister: ClientModelPersister,
        repository: Repository,
        parsedStat: ClientStat.ParsedStat,
    ): ReloadStat {
        coroutineScope {
            reloadStat.loadPart.doPartitionedRethrowSuspend { partIds: List<Int> ->
                async {
                    val parts = client.getParts(partIds).checkAndGetBody()
                    persister.persistParts(parts)
                    false
                }
            }
        }
        return repository.checkReload(parsedStat)
    }

    private suspend fun reloadMediaList(
        client: Client,
        reloadStat: ReloadStat,
        persister: ClientModelPersister,
        repository: Repository,
        parsedStat: ClientStat.ParsedStat,
    ): ReloadStat {
        val listQuery = client.getLists(reloadStat.loadLists).checkAndGetBody()
        persister.persist(listQuery)
        return repository.checkReload(parsedStat)
    }

    private suspend fun reloadExternalUser(
        client: Client,
        reloadStat: ReloadStat,
        persister: ClientModelPersister,
        repository: Repository,
        parsedStat: ClientStat.ParsedStat,
    ): ReloadStat {
        val users = client.getExternalUser(reloadStat.loadExUser).checkAndGetBody()
        persister.persistExternalUsers(users)
        return repository.checkReload(parsedStat)
    }

    private suspend fun reloadMedium(
        client: Client,
        reloadStat: ReloadStat,
        persister: ClientModelPersister,
        repository: Repository,
        parsedStat: ClientStat.ParsedStat,
    ): ReloadStat {
        val media = client.getMedia(reloadStat.loadMedium).checkAndGetBody()
        val simpleMedia = media.map(::ClientSimpleMedium)
        persister.persistMedia(simpleMedia)
        return repository.checkReload(parsedStat)
    }

    private suspend fun persistTocs(
        mediaTocs: Map<Int, List<String>>,
        persister: ClientModelPersister,
    ) {
        val inserts: MutableList<Toc> = LinkedList()
        for ((mediumId, value) in mediaTocs) {
            for (tocLink in value) {
                inserts.add(SimpleToc(mediumId, tocLink))
            }
        }
        persister.persistTocs(inserts)
    }

    private suspend fun cleanUp() {
        val repository = instance
        repository.syncProgress()
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        notificationManager.cancel(syncNotificationId)
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

        fun isRunning(application: Application): Boolean {
            val uuid = this.uuid ?: return false

            val infoFuture = WorkManager.getInstance(
                application
            ).getWorkInfoById(uuid)

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