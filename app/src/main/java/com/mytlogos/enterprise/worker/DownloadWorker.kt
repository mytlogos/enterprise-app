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
import com.mytlogos.enterprise.model.MediumType
import com.mytlogos.enterprise.model.NotificationItem
import com.mytlogos.enterprise.preferences.DownloadPreference
import com.mytlogos.enterprise.preferences.UserPreferences
import com.mytlogos.enterprise.tools.*
import java.io.IOException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class DownloadWorker(
    context: Context,
    workerParams: WorkerParameters
): Worker(context, workerParams) {
    private val downloadNotificationId = 0x100
    private val notificationManager: NotificationManagerCompat
    private val builder: NotificationCompat.Builder
    private var contentTools: Set<ContentTool>? = null

    override fun doWork(): Result {
        if (this.applicationContext !is Application) {
            println("Context not instance of Application")
            return Result.failure()
        }
        val application = this.applicationContext as Application
        if (SynchronizeWorker.isRunning(application)) {
            return Result.retry()
        }
        UserPreferences.init(application)
        try {
            synchronized(UNIQUE) {
                val repository = getInstance(application)
                contentTools = FileTools.getSupportedContentTools(application)
                for (tool in contentTools as MutableSet<ContentTool>) {
                    tool.mergeIfNecessary()
                }
                if (!repository.isClientAuthenticated) {
                    return Result.retry()
                }
                if (!repository.isClientOnline) {
                    notificationManager.notify(
                        downloadNotificationId,
                        builder.setContentTitle("Server not in reach").build()
                    )
                    cleanUp()
                    return Result.failure()
                }
                if (!FileTools.writable(application)) {
                    notificationManager.notify(
                        downloadNotificationId,
                        builder.setContentTitle("Not enough free space").build()
                    )
                    cleanUp()
                    return Result.failure()
                }
                notificationManager.notify(
                    downloadNotificationId,
                    builder
                        .setContentTitle("Getting Data for Download")
                        .setProgress(0, 0, true)
                        .build()
                )
                if (this.inputData == Data.EMPTY) {
                    download(repository)
                } else {
                    downloadData(repository)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            notificationManager.notify(
                downloadNotificationId,
                builder.setContentTitle("Download failed").setContentText(null).build()
            )
            cleanUp()
            return Result.failure()
        }
        cleanUp()
        return Result.success()
    }

    private fun filterMediumConstraints(mediumDownload: MediumDownload) {
        val downloadPreference = UserPreferences.get().downloadPreference
        val type = mediumDownload.mediumType
        val id = mediumDownload.id

        var sizeLimitMB = downloadPreference.getDownloadLimitSize(type)
        sizeLimitMB = sizeLimitMB.coerceAtMost(downloadPreference.getMediumDownloadLimitSize(id))

        if (sizeLimitMB < 0) {
            return
        }
        val maxSize = sizeLimitMB * 1024.0 * 1024.0

        for (tool in contentTools!!) {
            if (MediumType.`is`(type, tool.medium)) {
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

    private fun download(repository: Repository) {
        val toDownloadList = repository.toDownload
        val prohibitedMedia: MutableList<Int> = ArrayList()
        val toDownloadMedia: MutableSet<Int> = HashSet()
        for (toDownload in toDownloadList) {
            if (toDownload.mediumId != null) {
                if (toDownload.isProhibited) {
                    prohibitedMedia.add(toDownload.mediumId)
                } else {
                    toDownloadMedia.add(toDownload.mediumId)
                }
            }
            if (toDownload.externalListId != null) {
                toDownloadMedia.addAll(repository.getExternalListItems(toDownload.externalListId))
            }
            if (toDownload.listId != null) {
                toDownloadMedia.addAll(repository.getListItems(toDownload.listId))
            }
        }
        toDownloadMedia.removeAll(prohibitedMedia)
        val downloadPreference = UserPreferences.get().downloadPreference
        val executor: Executor = Executors.newFixedThreadPool(20)
        val futures: MutableList<CompletableFuture<MediumDownload>> = LinkedList()
        for (mediumId in toDownloadMedia) {
            futures.add(CompletableFuture.supplyAsync({
                val medium = repository.getSimpleMedium(mediumId)
                val count = downloadPreference.getDownloadLimitCount(medium.medium)
                val episodeIds = repository.getDownloadableEpisodes(mediumId, count)
                val uniqueEpisodes: MutableSet<Int> = LinkedHashSet(episodeIds)
                if (uniqueEpisodes.isEmpty()) {
                    return@supplyAsync null
                }
                val failedEpisodes = repository.getFailedEpisodes(uniqueEpisodes)
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
                    return@supplyAsync null
                }
                download
            }, executor))
        }
        val downloads: List<MediumDownload> = try {
            Utils.finishAll(futures).get()
        } catch (e: ExecutionException) {
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
        val mediumDownloads: MutableSet<MediumDownload> = HashSet()
        var downloadCount = 0
        for (download in downloads) {
            if (mediumDownloads.add(download)) {
                downloadCount += download.toDownloadEpisodes.size
            }
        }
        if (mediumDownloads.isNotEmpty()) {
            downloadEpisodes(mediumDownloads, repository, downloadCount)
        } else {
            notificationManager.notify(
                downloadNotificationId,
                builder
                    .setContentTitle("Nothing to Download")
                    .setContentText(null)
                    .setProgress(0, 0, false)
                    .build()
            )
        }
    }

    private fun downloadData(repository: Repository) {
        val data = this.inputData
        val mediumId = data.getInt(mediumId, 0)
        val episodeIds = data.getIntArray(episodeIds)
        if (mediumId == 0 || episodeIds == null || episodeIds.isEmpty()) {
            return
        }
        val episodes = HashSet<Int>()
        for (episodeId in episodeIds) {
            episodes.add(episodeId)
        }
        val medium = repository.getSimpleMedium(mediumId)
        val download = MediumDownload(episodes, mediumId, medium.medium, medium.title)
        downloadEpisodes(setOf(download), repository, episodeIds.size)
    }

    /**
     * Download episodes for each medium id,
     * up to an episode limit defined in [DownloadPreference].
     */
    private fun downloadEpisodes(
        episodeIds: Set<MediumDownload>,
        repository: Repository,
        downloadCount: Int
    ) {
        builder
            .setContentTitle("Download in Progress [0/$downloadCount]")
            .setProgress(downloadCount, 0, true)
        notificationManager.notify(downloadNotificationId, builder.build())
        val episodePackages = getDownloadPackages(episodeIds, repository)
        var successFull = 0
        var notSuccessFull = 0
        for (episodePackage in episodePackages) {
            if (this.isStopped) {
                stopDownload()
                return
            }
            var contentTool: ContentTool? = null
            for (tool in contentTools!!) {
                if (MediumType.`is`(tool.medium, episodePackage.mediumType) && tool.isSupported) {
                    contentTool = tool
                    break
                }
            }
            if (contentTool == null) {
                notSuccessFull += episodePackage.episodeIds.size
                updateProgress(downloadCount, successFull, notSuccessFull)
                continue
            }
            try {
                val downloadedEpisodes = repository.downloadEpisodes(episodePackage.episodeIds)
                val currentlySavedEpisodes: MutableList<Int> = ArrayList()
                if (downloadedEpisodes == null) {
                    notSuccessFull = onFailed(
                        downloadCount,
                        successFull,
                        notSuccessFull,
                        repository,
                        episodePackage,
                        false
                    )
                    continue
                }
                val contentEpisodes: MutableList<ClientDownloadedEpisode> = ArrayList()
                for (downloadedEpisode in downloadedEpisodes) {
                    val episodeId = downloadedEpisode.episodeId
                    val episode = repository.getSimpleEpisode(episodeId)

                    if (downloadedEpisode.getContent().isNotEmpty()) {
                        successFull++
                        currentlySavedEpisodes.add(episodeId)
                        contentEpisodes.add(downloadedEpisode)
                        repository.addNotification(
                            NotificationItem.createNow(
                                String.format(
                                    "Episode %s of %s saved",
                                    episode.formattedTitle,
                                    episodePackage.mediumTitle
                                ),
                                ""
                            )
                        )
                    } else {
                        notSuccessFull++
                        repository.updateFailedDownloads(episodeId)
                        repository.addNotification(
                            NotificationItem.createNow(
                                String.format(
                                    "Could not save Episode %s of %s",
                                    episode.formattedTitle,
                                    episodePackage.mediumTitle
                                ),
                                ""
                            )
                        )
                    }
                }
                contentTool.saveContent(contentEpisodes, episodePackage.mediumId)
                repository.updateSaved(currentlySavedEpisodes, true)
                updateProgress(downloadCount, successFull, notSuccessFull)
            } catch (e: NotEnoughSpaceException) {
                notSuccessFull = onFailed(
                    downloadCount,
                    successFull,
                    notSuccessFull,
                    repository,
                    episodePackage,
                    true
                )
            } catch (e: IOException) {
                e.printStackTrace()
                notSuccessFull = onFailed(
                    downloadCount,
                    successFull,
                    notSuccessFull,
                    repository,
                    episodePackage,
                    false
                )
            }
        }
    }

    private fun stopDownload() {
        notificationManager.notify(
            downloadNotificationId,
            builder
                .setContentTitle("Download stopped")
                .setContentText(null)
                .build()
        )
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

    private fun onFailed(
        downloadCount: Int,
        successFull: Int,
        notSuccessFull: Int,
        repository: Repository,
        downloadPackage: DownloadPackage,
        notEnoughSpace: Boolean
    ): Int {
        for (episodeId in downloadPackage.episodeIds) {
            repository.updateFailedDownloads(episodeId)
            val episode = repository.getSimpleEpisode(episodeId)
            val format =
                if (notEnoughSpace) "Not enough Space for Episode %s of %s" else "Could not save Episode %s of %s"
            repository.addNotification(
                NotificationItem.createNow(
                    String.format(format, episode.formattedTitle, downloadPackage.mediumTitle),
                    ""
                )
            )
        }
        val unsuccessFull = notSuccessFull + downloadPackage.episodeIds.size
        updateProgress(downloadCount, successFull, unsuccessFull)
        return unsuccessFull
    }

    private fun updateProgress(downloadCount: Int, successFull: Int, notSuccessFull: Int) {
        val progress = successFull + notSuccessFull
        builder.setContentTitle(
            String.format(
                "Download in Progress [%s/%s]",
                progress,
                downloadCount
            )
        )
        builder.setContentText(String.format("Failed: %s", notSuccessFull))
        builder.setProgress(downloadCount, progress, false)
        notificationManager.notify(downloadNotificationId, builder.build())
    }

    private class MediumDownload(
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

    private class DownloadPackage(val mediumId: Int, val mediumType: Int, val mediumTitle: String) {
        val episodeIds: MutableSet<Int> = HashSet()
    }

    private fun getDownloadPackages(
        episodeIds: Set<MediumDownload>,
        repository: Repository
    ): Collection<DownloadPackage> {
        val savedEpisodes = repository.savedEpisodes
        val savedIds: Set<Int> = HashSet(savedEpisodes)
        val episodePackages: MutableCollection<DownloadPackage> = ArrayList()
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
                    episodePackages.add(downloadPackage)
                    downloadPackage = DownloadPackage(
                        mediumDownload.id,
                        mediumDownload.mediumType,
                        mediumDownload.title
                    )
                }
                downloadPackage.episodeIds.add(episodeId)
            }
            if (downloadPackage.episodeIds.isNotEmpty()) {
                episodePackages.add(downloadPackage)
            }
        }
        return episodePackages
    }

    companion object {
        private const val UNIQUE = "DOWNLOAD_WORKER"

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
                .putIntArray(Companion.episodeIds, episodeIds.stream().mapToInt { obj: Int -> obj }
                    .toArray())
                .build()

            val oneTimeWorkRequest = getWorkRequest(data)
            val uniqueWorkName = "$UNIQUE-$mediumId"

            WorkManager.getInstance(context)
                .beginUniqueWork(uniqueWorkName, ExistingWorkPolicy.REPLACE, oneTimeWorkRequest)
                .then(CheckSavedWorker.workRequest)
                .enqueue()
        }
    }

    init {
        uuids.add(this.id)
        notificationManager = NotificationManagerCompat.from(this.applicationContext)
        builder = NotificationCompat.Builder(this.applicationContext, CHANNEL_ID)
        builder
            .setContentTitle("Download in Progress...")
            .setSmallIcon(R.mipmap.ic_launcher).priority =
            NotificationCompat.PRIORITY_DEFAULT
    }
}