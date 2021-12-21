package com.mytlogos.enterprise.worker

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.background.Repository
import com.mytlogos.enterprise.background.RepositoryImpl.Companion.getInstance
import com.mytlogos.enterprise.background.repository.EpisodeRepository
import com.mytlogos.enterprise.background.repository.MediaListRepository
import com.mytlogos.enterprise.background.repository.ToDownloadRepository
import com.mytlogos.enterprise.tools.ContentTool
import com.mytlogos.enterprise.tools.getContentTool
import com.mytlogos.enterprise.tools.getSupportedContentTools
import kotlinx.coroutines.delay
import java.util.*
import kotlin.collections.HashSet

class CheckSavedWorker(
    context: Context,
    workerParams: WorkerParameters
): CoroutineWorker(context, workerParams) {

    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var builder: NotificationCompat.Builder
    private lateinit var episodeRepository: EpisodeRepository
    private lateinit var mediaListRepository: MediaListRepository
    private val checkLocalNotificationId = 0x300
    private var correctedSaveState = 0
    private var clearedLooseEpisodes = 0
    private var checkedCount = 0
    private var mediaToCheck = 0
    // TODO: add preference for user
    private var handleLoose: HandleLooseEpisode = HandleLooseEpisode.UPDATE_STATE

    private enum class HandleLooseEpisode {
        DELETE,
        UPDATE_STATE
    }

    /**
     * Initialize [notificationManager] and [builder] and notify
     * User that it starts working now.
     */
    private fun initWork() {
        notificationManager = NotificationManagerCompat.from(this.applicationContext)
        builder = NotificationCompat.Builder(this.applicationContext, CHANNEL_ID)
        builder
            .setContentTitle("Checking Local Content Integrity")
            .setSmallIcon(R.mipmap.ic_launcher).priority = NotificationCompat.PRIORITY_DEFAULT
        notificationManager.notify(checkLocalNotificationId, builder.build())
        episodeRepository = EpisodeRepository.getInstance(applicationContext as Application)
        mediaListRepository = MediaListRepository.getInstance(applicationContext as Application)
    }

    @SuppressLint("UseSparseArrays")
    override suspend fun doWork(): Result {
        val application = this.applicationContext as Application
        if (SynchronizeWorker.isRunning(application)
            || DownloadWorker.isRunning(application)
        ) {
            return Result.retry()
        }
        this.initWork()

        val tools = getSupportedContentTools(application)
        val repository = getInstance(application)
        val mediumSavedEpisodes: MutableMap<Int, MutableSet<Int>> = HashMap()

        // get mappings for locally saved episode from both internal and external storage
        // for all available media
        for (tool in tools) {
            updateSavedEpisodesMapping(tool, mediumSavedEpisodes, true, repository)
            updateSavedEpisodesMapping(tool, mediumSavedEpisodes, false, repository)
        }

        val typeMediumSavedEpisodes = splitByType(mediumSavedEpisodes, repository)

        initProgress(typeMediumSavedEpisodes)
        updateNotificationContentText()

        // check state of saved Episode with their database counterparts by type
        for ((mediumType, value) in typeMediumSavedEpisodes) {
            val tool = getContentTool(mediumType, application)

            checkLocalContentFiles(tool, value)

            notificationManager.notify(checkLocalNotificationId, builder.build())
        }

        // let notification stay for 10s
        delay(10000)
        notificationManager.cancel(checkLocalNotificationId)
        return Result.success()
    }

    /**
     * Initialize the Counts of [mediaToCheck] to number of mediumIds from [typeMediumSavedEpisodes].
     * Initializes [checkedCount] to 0.
     */
    private fun initProgress(typeMediumSavedEpisodes: MutableMap<Int, MutableMap<Int, MutableSet<Int>>>) {
        checkedCount = 0
        mediaToCheck = typeMediumSavedEpisodes.values.fold(
            0,
            { accumulator, map -> accumulator + map.size }
        )
    }

    /**
     * Split the Mappings by `MediumType` of the Medium of the respective MediumIds.
     */
    private suspend fun splitByType(
        mediumSavedEpisodes: MutableMap<Int, MutableSet<Int>>,
        repository: Repository,
    ): MutableMap<Int, MutableMap<Int, MutableSet<Int>>> {
        val typeMediumSavedEpisodes: MutableMap<Int, MutableMap<Int, MutableSet<Int>>> = HashMap()

        // split mappings of `mediumSavedEpisodes` by `MediumType`
        for ((key, value) in mediumSavedEpisodes) {
            val mediumType = repository.getMediumType(key)
            val mapping = typeMediumSavedEpisodes.computeIfAbsent(mediumType) { HashMap() }
            mapping[key] = value
        }
        return typeMediumSavedEpisodes
    }

    /**
     * Update Episode Saved State.
     */
    private suspend fun checkLocalContentFiles(
        tool: ContentTool,
        mediumSavedEpisodes: Map<Int, MutableSet<Int>>
    ) {
        for ((mediumId, looseIds) in mediumSavedEpisodes) {
            // get the episodeIds with saved=true of medium
            val savedEpisodes = episodeRepository.getSavedEpisodes(mediumId)
            val unSavedIds: MutableSet<Int> = HashSet(savedEpisodes)
            unSavedIds.removeAll(looseIds)
            looseIds.removeAll(savedEpisodes.toSet().toSet())

            if (unSavedIds.isNotEmpty()) {
                episodeRepository.updateSaved(unSavedIds, false)
                correctedSaveState += unSavedIds.size
            }

            if (looseIds.isNotEmpty()) {
                when (handleLoose) {
                    HandleLooseEpisode.DELETE -> {
                        try {
                            tool.removeMediaEpisodes(mediumId, looseIds)
                            clearedLooseEpisodes += looseIds.size
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    HandleLooseEpisode.UPDATE_STATE -> {
                        episodeRepository.updateSaved(looseIds, true)
                        correctedSaveState += looseIds.size
                    }
                }
            }
            checkedCount++
            updateNotificationContentText()
        }
    }

    /**
     * Notify User of the current Check Progress
     */
    private fun updateNotificationContentText() {
        builder.setContentTitle(
            "Checking Local Content Integrity [$checkedCount/$mediaToCheck]"
        )
        builder.setStyle(
            NotificationCompat.InboxStyle()
                .addLine("Corrected Save State: $correctedSaveState")
                .addLine("Cleared Loose Episodes: $clearedLooseEpisodes")
        )
        notificationManager.notify(checkLocalNotificationId, builder.build())
    }

    /**
     * Updates [mediumSavedEpisodes] with the mapping of MediumId -> Set<Locally Saved Episode>.
     * Looks up these mappings either internal or external, decided by [externalSpace].
     * Adds empty mappings for non-prohibited media Ids affected by ToDownload.
     */
    private suspend fun updateSavedEpisodesMapping(
        contentTool: ContentTool,
        mediumSavedEpisodes: MutableMap<Int, MutableSet<Int>>,
        externalSpace: Boolean,
        repository: Repository
    ) {
        // iterate over the mapping of mediumId -> File
        // and merge the id of the currently as file saved episodes
        // it into the mediumId -> Set<EpisodeId> mapping
        for ((key, value) in contentTool.getItemContainers(externalSpace)) {
            // get currently available mappings between episodeId -> Path (String)
            val episodePaths = contentTool.getEpisodePaths(value.absolutePath)
            val episodeIds: MutableSet<Int> = HashSet(episodePaths.keys)

            mediumSavedEpisodes.merge(
                key,
                episodeIds
            ) { oldIds: MutableSet<Int>, newIds: Set<Int> ->
                oldIds.addAll(newIds)
                oldIds
            }
        }

        // get the current toDownload List
        val toDownloadList = ToDownloadRepository.getInstance(applicationContext as Application).getToDownloads()

        val prohibitedMedia: MutableList<Int> = ArrayList()
        val toDownloadMedia: MutableSet<Int> = HashSet()

        for (toDownload in toDownloadList) {
            val affectedMediaIds = HashSet<Int>()

            if (toDownload.mediumId != null) {
                affectedMediaIds.add(toDownload.mediumId)
            }
            if (toDownload.externalListId != null) {
                affectedMediaIds.addAll(repository.getExternalListItems(toDownload.externalListId))
            }
            if (toDownload.listId != null) {
                affectedMediaIds.addAll(mediaListRepository.getListItems(toDownload.listId))
            }

            if (toDownload.isProhibited) {
                prohibitedMedia.addAll(affectedMediaIds)
            } else {
                toDownloadMedia.addAll(affectedMediaIds)
            }
        }
        toDownloadMedia.removeAll(prohibitedMedia.toSet())

        for (mediumId in toDownloadMedia) {
            mediumSavedEpisodes.putIfAbsent(mediumId, HashSet())
        }
    }

    companion object {
        const val CHECK_SAVED_WORKER = "CHECK_SAVED_WORKER"

        // TODO: 08.08.2019 use this for sdk >= 28
        const val CHANNEL_ID = "CHECK_SAVED_WORKER"
        @JvmStatic
        fun checkLocal(context: Context?) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()
            WorkManager.getInstance(context!!)
                .beginWith(workRequest)
                .then(
                    OneTimeWorkRequest.Builder(DownloadWorker::class.java)
                        .setConstraints(constraints).build()
                )
                .enqueue()
        }

        val workRequest: OneTimeWorkRequest
            get() = OneTimeWorkRequest.Builder(CheckSavedWorker::class.java).build()
    }
}