package com.mytlogos.enterprise.background.repository

import android.app.Application
import android.content.Context
import androidx.paging.*
import com.mytlogos.enterprise.background.RepositoryImpl
import com.mytlogos.enterprise.background.RoomConverter
import com.mytlogos.enterprise.background.api.AndroidNetworkIdentificator
import com.mytlogos.enterprise.background.api.Client
import com.mytlogos.enterprise.background.api.model.ClientDownloadedEpisode
import com.mytlogos.enterprise.background.resourceLoader.LoadWorkGenerator
import com.mytlogos.enterprise.background.room.AbstractDatabase
import com.mytlogos.enterprise.background.room.model.RoomFailedEpisode
import com.mytlogos.enterprise.model.DisplayRelease
import com.mytlogos.enterprise.model.FailedEpisode
import com.mytlogos.enterprise.model.SimpleEpisode
import com.mytlogos.enterprise.model.TocEpisode
import com.mytlogos.enterprise.tools.*
import com.mytlogos.enterprise.viewmodel.EpisodeViewModel
import com.mytlogos.enterprise.worker.DownloadWorker
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.joda.time.DateTime
import retrofit2.Response
import java.io.IOException
import java.util.*

@Suppress("BlockingMethodInNonBlockingContext")
class EpisodeRepository private constructor(application: Application) {
    private val failedEpisodesDao = AbstractDatabase.getInstance(application).failedEpisodesDao()
    private val episodeDao = AbstractDatabase.getInstance(application).episodeDao()
    private val mediumDao = AbstractDatabase.getInstance(application).mediumDao()
    private val partDao = AbstractDatabase.getInstance(application).partDao()

    val client: Client by lazy {
        Client.getInstance(AndroidNetworkIdentificator(application))
    }

    fun getDisplayEpisodes(filter: EpisodeViewModel.Filter): Flow<PagingData<DisplayRelease>> {
        val query = if (filter.latestOnly) {
            episodeDao::getDisplayEpisodesLatestOnlyPaging
        } else {
            episodeDao::getDisplayEpisodesPaging
        }
        return transformFlow(
            pagingSourceFactory = {
                query(
                    filter.saved,
                    filter.read,
                    filter.medium,
                    filter.minIndex,
                    filter.maxIndex,
                    filter.filterListIds,
                    filter.filterListIds.isEmpty()
                )
            }
        )
    }

    fun getToc(
        mediumId: Int,
        sortings: Sortings,
        read: Byte,
        saved: Byte,
    ): Flow<PagingData<TocEpisode>> {
        val converter = RoomConverter()
        return transformFlow(
            pagingSourceFactory = {
                if (sortings.sortValue > 0) {
                    episodeDao.getTocEpisodesAsc(mediumId, read, saved)
                } else {
                    episodeDao.getTocEpisodesDesc(mediumId, read, saved)
                }
            }
        ).map { data -> data.map { converter.convertTocEpisode(it) } }
    }


    @Throws(Exception::class)
    suspend fun reloadLowerIndex(combiIndex: Double, mediumId: Int) {
        val episodeIds = episodeDao.getEpisodeIdsWithLowerIndex(mediumId, combiIndex)
        reloadEpisodes(episodeIds)
    }

    @Throws(Exception::class)
    suspend fun reloadHigherIndex(combiIndex: Double, mediumId: Int) {
        val episodeIds = episodeDao.getEpisodeIdsWithHigherIndex(mediumId, combiIndex)
        reloadEpisodes(episodeIds)
    }

    @Throws(Exception::class)
    suspend fun reload(episodeIds: Set<Int>) {
        reloadEpisodes(episodeIds)
    }

    @Throws(IOException::class)
    suspend fun reloadAll(mediumId: Int) {
        val medium = client.getMedium(mediumId).body()

        if (medium == null) {
            System.err.println("missing medium: $mediumId")
            return
        }

        val parts = medium.parts ?: intArrayOf()
        val partIds: MutableCollection<Int> = ArrayList(parts.size)

        for (part in parts) {
            partIds.add(part)
        }

        val partBody = client.getParts(partIds).body() ?: return
        val loadedPartIds: MutableList<Int> = ArrayList()

        for (part in partBody) {
            loadedPartIds.add(part.id)
        }

        val repositoryImpl = RepositoryImpl.instance as RepositoryImpl
        val persister = repositoryImpl.getPersister()
        val loadedData = repositoryImpl.getLoadedData()

        val generator = LoadWorkGenerator(loadedData)
        val filteredParts = generator.filterParts(partBody)

        persister.persist(filteredParts)
        partIds.removeAll(loadedPartIds)
        partDao.deletePerId(partIds)
    }

    @Throws(Exception::class)
    private suspend fun reloadEpisodes(episodeIds: Collection<Int>) {
        val repositoryImpl = RepositoryImpl.instance as RepositoryImpl
        val persister = repositoryImpl.getPersister()
        val loadedData = repositoryImpl.getLoadedData()

        coroutineScope {
            episodeIds.doPartitionedExSuspend { integers: List<Int> ->
                async {
                    val episodes = client.getEpisodes(integers).body()
                        ?: return@async false


                    val generator = LoadWorkGenerator(loadedData)
                    val filteredEpisodes = generator.filterEpisodes(episodes)
                    persister.persist(filteredEpisodes)

                    val loadedIds: MutableList<Int> = ArrayList()
                    for (episode in episodes) {
                        loadedIds.add(episode.id)
                    }
                    val ids = ArrayList(integers)
                    ids.removeAll(loadedIds)
                    episodeDao.deletePerId(ids)
                    false
                }
            }
        }
    }

    suspend fun downloadEpisodes(episodeIds: Collection<Int>): List<ClientDownloadedEpisode>? {
        return client.downloadEpisodes(episodeIds).body()
    }

    @Throws(IOException::class)
    suspend fun deleteLocalEpisodesWithLowerIndex(
        combiIndex: Double,
        mediumId: Int,
        application: Application,
    ) {
        val episodeIds = episodeDao.getSavedEpisodeIdsWithLowerIndex(mediumId, combiIndex)
        deleteLocalEpisodes(HashSet(episodeIds), mediumId, application)
    }

    @Throws(IOException::class)
    suspend fun deleteLocalEpisodesWithHigherIndex(
        combiIndex: Double,
        mediumId: Int,
        application: Application,
    ) {
        val episodeIds = episodeDao.getSavedEpisodeIdsWithHigherIndex(mediumId, combiIndex)
        deleteLocalEpisodes(HashSet(episodeIds), mediumId, application)
    }

    @Throws(IOException::class)
    suspend fun deleteAllLocalEpisodes(mediumId: Int, application: Application) {
        val episodes: Collection<Int> = episodeDao.getSavedEpisodes(mediumId)
        deleteLocalEpisodes(HashSet(episodes), mediumId, application)
    }

    @Throws(IOException::class)
    suspend fun deleteLocalEpisodes(
        episodeIds: Set<Int>,
        mediumId: Int,
        application: Application,
    ) {
        val medium = mediumDao.getMediumType(mediumId)
        val contentTool = getContentTool(medium, application)
        if (!contentTool.isSupported) {
            throw IOException("medium type: $medium is not supported")
        }
        contentTool.removeMediaEpisodes(mediumId, episodeIds)
        this.updateSaved(episodeIds, false)
    }

    @Throws(Exception::class)
    suspend fun updateReadWithHigherIndex(combiIndex: Double, read: Boolean, mediumId: Int) {
        val episodeIds = episodeDao.getEpisodeIdsWithHigherIndex(mediumId, combiIndex, read)
        this.updateRead(episodeIds, read)
    }

    @Throws(Exception::class)
    suspend fun updateReadWithLowerIndex(combiIndex: Double, read: Boolean, mediumId: Int) {
        val episodeIds = episodeDao.getEpisodeIdsWithLowerIndex(mediumId, combiIndex, read)
        this.updateRead(episodeIds, read)
    }

    @Throws(Exception::class)
    suspend fun updateAllRead(mediumId: Int, read: Boolean) {
        val episodeIds = episodeDao.getAllEpisodes(mediumId)
        this.updateRead(episodeIds, read)
    }

    @Throws(Exception::class)
    suspend fun updateRead(episodeIds: Collection<Int>, read: Boolean) {
        val progress = if (read) 1f else 0f

        coroutineScope {
            episodeIds.doPartitionedExSuspend { ids: List<Int> ->
                async {
                    val response: Response<Boolean> = client.addProgress(ids, progress)
                    if (!response.isSuccessful || response.body() == null || !response.body()!!) {
                        return@async false
                    }
                    episodeDao.updateProgress(ids, progress, DateTime.now())
                    false
                }
            }
        }
    }

    suspend fun getAllSavedEpisodes(): List<Int> = episodeDao.getAllSavedEpisodes()

    suspend fun updateSaved(episodeId: Int, saved: Boolean) {
        episodeDao.updateSaved(episodeId, saved)
    }

    suspend fun updateSaved(episodeIds: Collection<Int>, saved: Boolean) {
        try {
            coroutineScope {
                episodeIds.doPartitionedExSuspend { ids: List<Int> ->
                    async {
                        episodeDao.updateSaved(ids, saved)
                        false
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    suspend fun downloadLowerIndex(combiIndex: Double, mediumId: Int, context: Context) {
        val episodeIds = episodeDao.getEpisodeIdsWithLowerIndex(mediumId, combiIndex)
        DownloadWorker.enqueueDownloadTask(context, mediumId, episodeIds)
    }

    suspend fun downloadHigherIndex(combiIndex: Double, mediumId: Int, context: Context) {
        val episodeIds = episodeDao.getEpisodeIdsWithHigherIndex(mediumId, combiIndex)
        DownloadWorker.enqueueDownloadTask(context, mediumId, episodeIds)
    }

    fun download(episodeIds: Set<Int>, mediumId: Int, context: Context) {
        DownloadWorker.enqueueDownloadTask(context, mediumId, episodeIds)
    }

    suspend fun downloadAll(mediumId: Int, context: Context) {
        val episodeIds = episodeDao.getAllEpisodes(mediumId)
        DownloadWorker.enqueueDownloadTask(context, mediumId, episodeIds)
    }

    suspend fun getSimpleEpisode(episodeId: Int): SimpleEpisode {
        return episodeDao.getSimpleEpisode(episodeId)
    }

    suspend fun getSimpleEpisodes(ids: Collection<Int>): List<SimpleEpisode> {
        return episodeDao.getSimpleEpisodes(ids)
    }

    suspend fun getDownloadableEpisodes(mediumId: Int, limit: Int): List<Int> {
        return episodeDao.getDownloadableEpisodes(mediumId, limit)
    }

    fun getSavedEpisodes(mediumId: Int): Collection<Int> {
        return runBlocking { episodeDao.getSavedEpisodes(mediumId) }
    }

    suspend fun clearFailEpisodes() {
        failedEpisodesDao.clearAll()
    }

    suspend fun getFailedEpisodes(episodeIds: Collection<Int>): List<FailedEpisode> {
        return failedEpisodesDao.getFailedEpisodes(episodeIds)
    }

    suspend fun updateFailedDownload(episodeId: Int) {
        val failedEpisode = failedEpisodesDao.getFailedEpisode(episodeId)
        var failedCount = 0
        if (failedEpisode != null) {
            failedCount = failedEpisode.failCount
        }
        failedCount++
        failedEpisodesDao.insert(RoomFailedEpisode(episodeId, failedCount))
    }

    companion object : SingletonHolder<EpisodeRepository, Application>(::EpisodeRepository)
}