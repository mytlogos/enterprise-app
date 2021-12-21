package com.mytlogos.enterprise.worker

import android.app.Application
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleOwner
import androidx.work.*
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.background.*
import com.mytlogos.enterprise.background.RepositoryImpl.Companion.getInstance
import com.mytlogos.enterprise.background.api.model.ClientDownloadedEpisode
import com.mytlogos.enterprise.background.repository.EpisodeRepository
import com.mytlogos.enterprise.background.repository.MediaListRepository
import com.mytlogos.enterprise.background.repository.NotificationRepository
import com.mytlogos.enterprise.background.repository.ToDownloadRepository
import com.mytlogos.enterprise.model.NotificationItem
import com.mytlogos.enterprise.model.isType
import com.mytlogos.enterprise.preferences.DownloadPreference
import com.mytlogos.enterprise.preferences.UserPreferences
import com.mytlogos.enterprise.tools.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.atomic.AtomicBoolean

class DownloadWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
): CoroutineWorker(context, workerParams) {
    private val downloadNotificationId = 0x100
    private val notificationManager: NotificationManagerCompat
    private val builder: NotificationCompat.Builder
    private lateinit var contentTools: Map<Int, ContentTool>
    private lateinit var repository: Repository
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var episodeRepository: EpisodeRepository
    private lateinit var toDownloadRepository: ToDownloadRepository
    private lateinit var mediaListRepository: MediaListRepository
    private var downloadCount = 0
    private var successFull = 0
    private var notSuccessFull = 0

    init {
        uuids.add(this.id)
        notificationManager = NotificationManagerCompat.from(this.applicationContext)
        builder = NotificationCompat.Builder(this.applicationContext, CHANNEL_ID)
        builder
            .setContentTitle("Download in Progress...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .priority = NotificationCompat.PRIORITY_DEFAULT
    }

    override suspend fun doWork(): Result {
        // check if a DownloadWorker is already running
        // if yes, do not try to run too and just fail
        if (!running.compareAndSet(false, true)) {
            return Result.failure()
        }
        CoroutineExceptionHandler { _, throwable ->
            run {
                if (throwable is CancellationException) {
                    this.stopDownload()
                }
            }
        }
        currentCoroutineContext()[Job]?.invokeOnCompletion {
            if (it != null && it is CancellationException) {
                this.stopDownload()
            }
        }
        return try {
            this.download()
        } catch (e: Exception) {
            e.printStackTrace()
            builder.setContentTitle("Download failed").setContentText(null).notify()
            Result.failure()
        } finally {
            if (!running.compareAndSet(true, false)) {
                println("Expected to be the only running DownloadWorker in this process, but found none")
            }
            this.cleanUp()
        }
    }

    /**
     * Shortcut Function to display a Notification.
     * Does not modify the Builder.
     */
    private fun NotificationCompat.Builder.notify() {
        notificationManager.notify(
            downloadNotificationId,
            this.build()
        )
    }

    suspend fun download(): Result {
        if (this.applicationContext !is Application) {
            println("Context not instance of Application")
            return Result.failure()
        }
        val application = this.applicationContext as Application

        if (SynchronizeWorker.isRunning(application)) {
            return Result.retry()
        }
        UserPreferences.init(application)
        // init lateinit fields
        repository = getInstance(application)
        notificationRepository = NotificationRepository.getInstance(application)
        episodeRepository = EpisodeRepository.getInstance(application)
        toDownloadRepository = ToDownloadRepository.getInstance(application)
        mediaListRepository = MediaListRepository.getInstance(application)
        contentTools = getSupportedContentTools(application).associateBy { it.medium }

        for (tool in contentTools.values) {
            tool.mergeIfNecessary()
        }

        if (!repository.isClientAuthenticated) {
            return Result.retry()
        }
        if (!repository.isClientOnline) {
            builder.setContentTitle("Server not in reach").notify()
            return Result.failure()
        }
        if (!writable(application)) {
            builder.setContentTitle("Not enough free space").notify()
            return Result.failure()
        }
        builder
            .setContentTitle("Getting Data for Download")
            .setProgress(successFull, successFull, true)
            .notify()

        if (this.inputData == Data.EMPTY) {
            // download all possible episodes
            downloadAll()
        } else {
            // download only specific possible episodes
            downloadData()
        }
        return Result.success()
    }

    private fun filterMediumConstraints(mediumDownload: MediumDownload) {
        val downloadPreference = UserPreferences.get().downloadPreference
        val type = mediumDownload.mediumType
        val id = mediumDownload.id

        val mediumDownloadSizeLimit = downloadPreference.getMediumDownloadLimitSize(id)
        val sizeLimitMB = downloadPreference.getDownloadLimitSize(type).coerceAtMost(mediumDownloadSizeLimit)

        if (sizeLimitMB < successFull) {
            return
        }
        val maxSize = sizeLimitMB * 1024.0 * 1024.0

        for (tool in contentTools.values) {
            if (isType(type, tool.medium)) {
                val averageEpisodeSize = tool.getAverageEpisodeSize(id)
                val path = tool.getItemPath(id)
                if (path == null || path.isEmpty()) {
                    break
                }
                val size = tool.getEpisodePaths(path).keys.size
                var totalSize = averageEpisodeSize * size
                val iterator = mediumDownload.toDownloadEpisodes.iterator()
                while (iterator.hasNext()) {
                    totalSize += averageEpisodeSize
                    if (totalSize > maxSize) {
                        iterator.remove()
                    }
                }
                break
            }
        }
    }

    private suspend fun downloadAll() {
        val toDownloadMediaIds: MutableSet<Int> = getToDownloadMedia()
        val downloads = getMediumDownloads(toDownloadMediaIds)
        val mediumDownloads: MutableSet<MediumDownload> = downloads.toMutableSet()
        // calculate the total number of episodes to be downloaded
        downloadCount = mediumDownloads.fold(successFull) {
            count, download ->
                count + download.toDownloadEpisodes.size
        }

        if (mediumDownloads.isNotEmpty()) {
            downloadEpisodes(mediumDownloads)
        } else {
            builder
                .setContentTitle("Nothing to Download")
                .setContentText(null)
                .setProgress(successFull, successFull, false)
                .notify()
        }
    }

    private suspend fun getToDownloadMedia(): MutableSet<Int> {
        val toDownloadList = toDownloadRepository.getToDownloads()
        val prohibitedMedia: MutableList<Int> = ArrayList()
        val toDownloadMedia: MutableSet<Int> = HashSet()

        for (toDownload in toDownloadList) {
            val affectedMedia = mutableSetOf<Int>()

            if (toDownload.mediumId != null) {
                affectedMedia.add(toDownload.mediumId)
            }
            if (toDownload.externalListId != null) {
                affectedMedia.addAll(repository.getExternalListItems(toDownload.externalListId))
            }
            if (toDownload.listId != null) {
                affectedMedia.addAll(mediaListRepository.getListItems(toDownload.listId))
            }
            if (toDownload.isProhibited) {
                prohibitedMedia.addAll(affectedMedia)
            } else {
                toDownloadMedia.addAll(affectedMedia)
            }
        }
        toDownloadMedia.removeAll(prohibitedMedia)
        return toDownloadMedia
    }

    private suspend fun getMediumDownloads(toDownloadMedia: MutableSet<Int>) = coroutineScope {
        val downloadPreference = UserPreferences.get().downloadPreference

        val jobs = toDownloadMedia.map { mediumId ->
            async {
                val medium = repository.getSimpleMedium(mediumId)
                val count = downloadPreference.getDownloadLimitCount(medium.medium)
                val episodeIds = episodeRepository.getDownloadableEpisodes(mediumId, count)
                val uniqueEpisodes: MutableSet<Int> = LinkedHashSet(episodeIds)

                if (uniqueEpisodes.isEmpty()) {
                    return@async null
                }

                val failedEpisodes = episodeRepository.getFailedEpisodes(uniqueEpisodes)

                for (failedEpisode in failedEpisodes) {
                    // if it failed 3 times or more, don't try anymore for now
                    if (failedEpisode.failCount < 3) {
                        continue
                    }
                    uniqueEpisodes.remove(failedEpisode.episodeId)
                }

                val download = MediumDownload(
                    uniqueEpisodes,
                    mediumId,
                    medium.medium,
                    medium.title
                )

                filterMediumConstraints(download)

                if (download.toDownloadEpisodes.isEmpty()) {
                    return@async null
                }
                download
            }
        }
        return@coroutineScope jobs.awaitAll().filterNotNull()
    }

    /**
     * Download Episodes of a single Medium defined by [getInputData].
     */
    private suspend fun downloadData() {
        val data = this.inputData
        val mediumId = data.getInt(mediumId, successFull)
        val episodeIds = data.getIntArray(episodeIds)

        if (mediumId == successFull || episodeIds == null || episodeIds.isEmpty()) {
            return
        }

        val episodes = episodeIds.toMutableSet()
        val medium = repository.getSimpleMedium(mediumId)
        val download = MediumDownload(episodes, mediumId, medium.medium, medium.title)
        downloadCount = episodeIds.size

        downloadEpisodes(setOf(download))
    }

    /**
     * Download episodes for each medium id,
     * up to an episode limit defined in [DownloadPreference].
     */
    private suspend fun downloadEpisodes(episodeIds: Set<MediumDownload>) {
        builder
            .setContentTitle("Download in Progress [0/$downloadCount]")
            .setProgress(downloadCount, successFull, true)
            .notify()

        createDownloadPackagesFlow(episodeIds).mapNotNull {
            return@mapNotNull try {
                downloadPackage(it)
            } catch (e: IOException) {
                e.printStackTrace()
                onFailed(it.episodeIds, it.mediumTitle)
                null
            }
        }.collect { savePackage ->
            try {
                saveEpisodes(savePackage)
            } catch (e: NotEnoughSpaceException) {
                onFailed(
                    savePackage.toSave.map { it.episodeId },
                    savePackage.mediumTitle,
                    true
                )
            } catch (e: IOException) {
                e.printStackTrace()
                onFailed(savePackage.toSave.map { it.episodeId }, savePackage.mediumTitle)
            }
        }
    }

    private suspend fun downloadPackage(episodePackage: DownloadPackage): SavePackage? {
        val contentTool = contentTools[episodePackage.mediumType]

        if (contentTool == null) {
            notSuccessFull += episodePackage.episodeIds.size
            updateProgress()
            return null
        }

        val downloadedEpisodes = episodeRepository.downloadEpisodes(episodePackage.episodeIds)

        if (downloadedEpisodes == null) {
            onFailed(episodePackage.episodeIds, episodePackage.mediumTitle)
            return null
        }
        val contentEpisodes = mutableListOf<ClientDownloadedEpisode>()

        for (downloadedEpisode in downloadedEpisodes) {
            val episodeId = downloadedEpisode.episodeId

            if (downloadedEpisode.getContent().isNotEmpty()) {
                contentEpisodes.add(downloadedEpisode)
            } else {
                notSuccessFull++
                updateProgress()
                val episode = episodeRepository.getSimpleEpisode(episodeId)
                episodeRepository.updateFailedDownload(episodeId)
                notificationRepository.addNotification(
                    NotificationItem.createNow(
                        "Could not save Episode ${episode.formattedTitle} of ${episodePackage.mediumTitle}",
                    )
                )
            }
        }
        return SavePackage(
            episodePackage.mediumId,
            episodePackage.mediumTitle,
            contentEpisodes,
            contentTool
        )
    }

    private suspend fun saveEpisodes(data: SavePackage) = withContext(ioDispatcher) {
        @Suppress("BlockingMethodInNonBlockingContext")
        data.contentTool.saveContent(data.toSave, data.mediumId)
        val currentlySavedEpisodes = data.toSave.map { it.episodeId }

        episodeRepository.updateSaved(currentlySavedEpisodes, true)

        val episodes = episodeRepository.getSimpleEpisodes(currentlySavedEpisodes)

        for (episode in episodes) {
            successFull++
            updateProgress()
            notificationRepository.addNotification(
                NotificationItem.createNow(
                    "Episode ${episode.formattedTitle} of ${data.mediumTitle} saved",
                )
            )
        }
    }

    private fun stopDownload() {
        builder
            .setContentTitle("Download stopped")
            .setContentText(null)
            .notify()
    }

    private fun cleanUp() {
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        notificationManager.cancel(downloadNotificationId)
        uuids.remove(this.id)
    }

    private suspend fun onFailed(
        episodeIds: Collection<Int>,
        mediumTitle: String,
        notEnoughSpace: Boolean = false
    ) {
        for (episodeId in episodeIds) {
            episodeRepository.updateFailedDownload(episodeId)
            val episode = episodeRepository.getSimpleEpisode(episodeId)
            val format = if (notEnoughSpace) {
                "Not enough Space for Episode %s of %s"
            } else {
                "Could not save Episode %s of %s"
            }
            notificationRepository.addNotification(
                NotificationItem.createNow(
                    String.format(format, episode.formattedTitle, mediumTitle),
                )
            )
        }
        notSuccessFull += episodeIds.size
        updateProgress()
    }

    /**
     * Update the Progress Notification.
     */
    private fun updateProgress() {
        val progress = successFull + notSuccessFull
        builder.setContentTitle(
            "Download in Progress [$progress/$downloadCount]",
        )
        builder.setContentText("Failed: $notSuccessFull")
        builder.setProgress(downloadCount, progress, false)
        builder.notify()
    }

    /**
     * Represents the Episodes of an Medium which are to be downloaded.
     */
    private data class MediumDownload(
        val toDownloadEpisodes: MutableSet<Int>,
        val id: Int,
        val mediumType: Int,
        val title: String
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val that = other as MediumDownload
            return id == that.id
        }

        override fun hashCode(): Int {
            return id
        }
    }

    /**
     * Container for downloaded Episodes which need to be saved locally.
     */
    private data class SavePackage(
        val mediumId: Int,
        val mediumTitle: String,
        val toSave: Collection<ClientDownloadedEpisode>,
        val contentTool: ContentTool,
    )

    /**
     * Represents a Package for a single Download Request.
     */
    private data class DownloadPackage(
        val mediumId: Int,
        val mediumType: Int,
        val mediumTitle: String,
        val episodeIds: MutableSet<Int> = HashSet(),
    )

    /**
     * Create a Flow of DownloadPackages.
     * Each Package contains at most [maxPackageSize] Episode Ids.
     * Ignores Episodes which are already marked as saved.
     */
    private fun createDownloadPackagesFlow(episodeIds: Set<MediumDownload>): Flow<DownloadPackage> = flow {
            val savedEpisodes = episodeRepository.getAllSavedEpisodes()
            val savedIds = savedEpisodes.toSet()

            for (mediumDownload in episodeIds) {
                var downloadPackage = DownloadPackage(
                    mediumDownload.id,
                    mediumDownload.mediumType,
                    mediumDownload.title
                )
                for (episodeId in mediumDownload.toDownloadEpisodes) {
                    if (savedIds.contains(episodeId)) {
                        continue
                    }
                    if (downloadPackage.episodeIds.size == maxPackageSize) {
                        emit(downloadPackage)
                        downloadPackage = DownloadPackage(
                            mediumDownload.id,
                            mediumDownload.mediumType,
                            mediumDownload.title
                        )
                    }
                    downloadPackage.episodeIds.add(episodeId)
                }
                if (downloadPackage.episodeIds.isNotEmpty()) {
                    emit(downloadPackage)
                }
            }
        }

    companion object {
        private const val UNIQUE = "DOWNLOAD_WORKER"
        private val running = AtomicBoolean()

        // TODO: 08.08.2019 use this for sdk >= 28
        private const val CHANNEL_ID = "DOWNLOAD_CHANNEL"
        private const val maxPackageSize = 1
        private const val mediumId = "mediumId"
        private const val episodeIds = "episodeIds"
        private val uuids = Collections.synchronizedSet(HashSet<UUID>())

        fun enqueueDownloadTask(context: Context) {
            val oneTimeWorkRequest = getWorkRequest()
            WorkManager.getInstance(context)
                .beginUniqueWork(UNIQUE, ExistingWorkPolicy.REPLACE, oneTimeWorkRequest)
                .then(CheckSavedWorker.workRequest)
                .enqueue()
        }

        private fun getWorkRequest(data: Data = Data.EMPTY): OneTimeWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()
            return OneTimeWorkRequest.Builder(DownloadWorker::class.java)
                .setInputData(data)
                .setConstraints(constraints)
                .build()
        }

        @JvmStatic
        fun watchDatabase(application: Application, owner: LifecycleOwner) {
            val repository = getInstance(application)
            val doDownload = repository.onDownloadable()
            doDownload.observe(owner, { aBoolean: Boolean? ->
                if (aBoolean != null && aBoolean) {
                    enqueueDownloadTask(application)
                }
            })
            enqueueDownloadTask(application)
        }

        @JvmStatic
        fun stopWorker(application: Application) {
            WorkManager.getInstance(application).cancelUniqueWork(UNIQUE)
        }

        fun isRunning(application: Application): Boolean {
            for (uuid in HashSet(uuids)) {
                val infoFuture = WorkManager.getInstance(
                    application
                ).getWorkInfoById(uuid)
                try {
                    val info = infoFuture.get() ?: continue
                    if (info.state == WorkInfo.State.RUNNING) {
                        return true
                    }
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            return false
        }

        fun enqueueDownloadTask(context: Context, mediumId: Int, episodeIds: Collection<Int>) {
            val data = Data.Builder()
                .putInt(Companion.mediumId, mediumId)
                .putIntArray(Companion.episodeIds, episodeIds.toIntArray())
                .build()

            val oneTimeWorkRequest = getWorkRequest(data)
            val uniqueWorkName = "$UNIQUE-$mediumId"

            WorkManager.getInstance(context)
                .beginUniqueWork(uniqueWorkName, ExistingWorkPolicy.REPLACE, oneTimeWorkRequest)
                .then(CheckSavedWorker.workRequest)
                .enqueue()
        }
    }
}