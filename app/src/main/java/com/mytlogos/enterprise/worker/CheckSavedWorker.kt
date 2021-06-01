package com.mytlogos.enterprise.worker

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.mytlogos.enterprise.R
import com.mytlogos.enterprise.background.*
import com.mytlogos.enterprise.background.RepositoryImpl.Companion.getInstance
import com.mytlogos.enterprise.tools.ContentTool
import com.mytlogos.enterprise.tools.FileTools
import java.util.*

class CheckSavedWorker(
    context: Context,
    workerParams: WorkerParameters
): Worker(context, workerParams) {

    private var notificationManager: NotificationManagerCompat? = null
    private var builder: NotificationCompat.Builder? = null
    private val checkLocalNotificationId = 0x300
    private var correctedSaveState = 0
    private var clearedLooseEpisodes = 0
    private var checkedCount = 0
    private var mediaToCheck = 0

    @SuppressLint("UseSparseArrays")
    override fun doWork(): Result {
        val application = this.applicationContext as Application
        if (SynchronizeWorker.isRunning(application) || DownloadWorker.isRunning(
                application
            )
        ) {
            return Result.retry()
        }
        notificationManager = NotificationManagerCompat.from(this.applicationContext)
        builder = NotificationCompat.Builder(this.applicationContext, CHANNEL_ID)
        builder!!
            .setContentTitle("Checking Local Content Integrity")
            .setSmallIcon(R.mipmap.ic_launcher).priority = NotificationCompat.PRIORITY_DEFAULT
        notificationManager!!.notify(checkLocalNotificationId, builder!!.build())
        val tools = FileTools.getSupportedContentTools(application)
        val repository = getInstance(application)
        val mediumSavedEpisodes: MutableMap<Int, MutableSet<Int>> = HashMap()
        for (tool in tools) {
            putItemContainer(tool, mediumSavedEpisodes, true, repository)
            putItemContainer(tool, mediumSavedEpisodes, false, repository)
        }
        val typeMediumSavedEpisodes: MutableMap<Int, MutableMap<Int, MutableSet<Int>>> = HashMap()
        for ((key, value) in mediumSavedEpisodes) {
            val mediumType = repository.getMediumType(key)
            typeMediumSavedEpisodes
                .computeIfAbsent(mediumType) { HashMap() }[key] = value
        }
        mediaToCheck = 0
        for (map in typeMediumSavedEpisodes.values) {
            mediaToCheck += map.size
        }
        checkedCount = 0
        updateNotificationContentText()
        for ((mediumType, value) in typeMediumSavedEpisodes) {
            val tool = FileTools.getContentTool(mediumType, application)
            checkLocalContentFiles(tool, repository, value)
            notificationManager!!.notify(checkLocalNotificationId, builder!!.build())
        }
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        notificationManager!!.cancel(checkLocalNotificationId)
        return Result.success()
    }

    private fun checkLocalContentFiles(
        tool: ContentTool,
        repository: Repository,
        mediumSavedEpisodes: Map<Int, MutableSet<Int>>
    ) {
        for ((key, looseIds) in mediumSavedEpisodes) {
            val savedEpisodes = repository.getSavedEpisodes(key)
            val unSavedIds: MutableSet<Int> = HashSet(savedEpisodes)
            unSavedIds.removeAll(looseIds)
            looseIds.removeAll(savedEpisodes)

            if (unSavedIds.isNotEmpty()) {
                repository.updateSaved(unSavedIds, false)
                correctedSaveState += unSavedIds.size
            }

            if (looseIds.isNotEmpty()) {
                try {
                    tool.removeMediaEpisodes(key, looseIds)
                    clearedLooseEpisodes += looseIds.size
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            checkedCount++
            updateNotificationContentText()
        }
    }

    private fun updateNotificationContentText() {
        builder!!.setContentTitle(
            String.format(
                "Checking Local Content Integrity [%s/%s]",
                checkedCount,
                mediaToCheck
            )
        )
        builder!!.setStyle(
            NotificationCompat.InboxStyle()
                .addLine(String.format("Corrected Save State: %s", correctedSaveState))
                .addLine(String.format("Cleared Loose Episodes: %s", clearedLooseEpisodes))
        )
        notificationManager!!.notify(checkLocalNotificationId, builder!!.build())
    }

    private fun putItemContainer(
        bookTool: ContentTool,
        mediumSavedEpisodes: MutableMap<Int, MutableSet<Int>>,
        externalSpace: Boolean,
        repository: Repository
    ) {
        for ((key, value) in bookTool.getItemContainers(externalSpace)) {
            val episodePaths = bookTool.getEpisodePaths(value.absolutePath)
            val episodeIds: MutableSet<Int> = HashSet(episodePaths.keys)
            mediumSavedEpisodes.merge(
                key,
                episodeIds
            ) { integers: MutableSet<Int>, integers2: Set<Int>? ->
                integers.addAll(
                    integers2!!
                )
                integers
            }
        }
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