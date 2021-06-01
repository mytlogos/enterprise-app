package com.mytlogos.enterprise.background

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import androidx.work.Worker
import com.mytlogos.enterprise.background.api.AndroidNetworkIdentificator
import com.mytlogos.enterprise.background.api.Client
import com.mytlogos.enterprise.background.api.NetworkIdentificator
import com.mytlogos.enterprise.background.api.model.*
import com.mytlogos.enterprise.background.api.model.ClientStat.ParsedStat
import com.mytlogos.enterprise.background.resourceLoader.BlockingLoadWorker
import com.mytlogos.enterprise.background.resourceLoader.LoadWorkGenerator
import com.mytlogos.enterprise.background.resourceLoader.LoadWorker
import com.mytlogos.enterprise.background.room.RoomStorage
import com.mytlogos.enterprise.model.*
import com.mytlogos.enterprise.preferences.UserPreferences
import com.mytlogos.enterprise.tools.*
import com.mytlogos.enterprise.viewmodel.EpisodeViewModel
import com.mytlogos.enterprise.worker.DownloadWorker
import org.joda.time.DateTime
import java.io.IOException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Supplier

class RepositoryImpl private constructor(application: Application) : Repository {
    private val persister: ClientModelPersister
    override val user: LiveData<User?>
    private val client: Client
    private val storage: DatabaseStorage
    private val loadedData: LoadData
    override val loadWorker: LoadWorker
    private val editService: EditService

    private fun loadLoadedData() {
        val loadData = storage.getLoadData()
        loadedData.media.addAll(loadData.media)
        loadedData.part.addAll(loadData.part)
        loadedData.episodes.addAll(loadData.episodes)
        loadedData.news.addAll(loadData.news)
        loadedData.mediaList.addAll(loadData.mediaList)
        loadedData.externalUser.addAll(loadData.externalUser)
        loadedData.externalMediaList.addAll(loadData.externalMediaList)
    }

    override val isClientOnline: Boolean
        get() = client.isClientOnline
    override val isClientAuthenticated: Boolean
        get() = client.isClientAuthenticated

    override val homeStats: LiveData<HomeStats>
        get() = storage.getHomeStats()

    override fun updateUser(updateUser: UpdateUser) {
        editService.updateUser(updateUser)
    }

    override fun deleteAllUser() {
        TaskManager.runTask { storage.deleteAllUser() }
    }

    /**
     * Synchronous Login.
     *
     * @param email    email or name of the user
     * @param password password of the user
     * @throws IOException if an connection problem arose
     */
    @Throws(IOException::class)
    override fun login(email: String, password: String) {
        val response = client.login(email, password)
        val user = response.body()
        if (user != null) {
            // set authentication in client before persisting user,
            // as it may load data which requires authentication
            this.client.setAuthentication(user.uuid, user.session)
        }
        persister.persist(user)
    }

    /**
     * Synchronous Registration.
     *
     * @param email    email or name of the user
     * @param password password of the user
     */
    @Throws(IOException::class)
    override fun register(email: String, password: String) {
        val response = client.register(email, password)
        val user = response.body()
        if (user != null) {
            // set authentication in client before persisting user,
            // as it may load data which requires authentication
            this.client.setAuthentication(user.uuid, user.session)
        }
        persister.persist(user).finish()
    }

    override fun logout() {
        TaskManager.runTask {
            try {
                val response = client.logout()
                if (!response.isSuccessful) {
                    println("Log out was not successful: " + response.message())
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            storage.deleteAllUser()
        }
    }

    override fun loadAllMedia() {
        try {
            val mediaIds = Utils.checkAndGetBody(client.allMedia)
                ?: return
            for (mediumId in mediaIds) {
                if (loadedData.media.contains(mediumId) || loadWorker.isMediumLoading(mediumId)) {
                    continue
                }
                loadWorker.addIntegerIdTask(mediumId, null, loadWorker.MEDIUM_LOADER)
            }
            loadWorker.work()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun loadEpisodeAsync(episodeIds: Collection<Int>): CompletableFuture<List<ClientEpisode>?> {
        return CompletableFuture.supplyAsync { loadEpisodeSync(episodeIds) }
    }

    override fun loadEpisodeSync(episodeIds: Collection<Int>): List<ClientEpisode>? {
        return try {
            println("loading episodes: " + episodeIds + " on " + Thread.currentThread())
            client.getEpisodes(episodeIds).body()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override fun loadMediaAsync(mediaIds: Collection<Int>): CompletableFuture<List<ClientMedium>?> {
        return CompletableFuture.supplyAsync { loadMediaSync(mediaIds) }
    }

    override fun loadMediaSync(mediaIds: Collection<Int>): List<ClientMedium>? {
        return try {
            println("loading media: " + mediaIds + " on " + Thread.currentThread())
            client.getMedia(mediaIds).body()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override fun loadPartAsync(partIds: Collection<Int>): CompletableFuture<List<ClientPart>?> {
        return CompletableFuture.supplyAsync { loadPartSync(partIds) }
    }

    override fun loadPartSync(partIds: Collection<Int>): List<ClientPart>? {
        return try {
            println("loading parts: " + partIds + " on " + Thread.currentThread())
            client.getParts(partIds).body()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override fun loadMediaListAsync(listIds: Collection<Int>): CompletableFuture<ClientMultiListQuery?> {
        return CompletableFuture.supplyAsync { loadMediaListSync(listIds) }
    }

    override fun loadMediaListSync(listIds: Collection<Int>): ClientMultiListQuery? {
        return try {
            println("loading lists: " + listIds + " on " + Thread.currentThread())
            client.getLists(listIds).body()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override fun loadExternalMediaListAsync(externalListIds: Collection<Int>): CompletableFuture<List<ClientExternalMediaList>?> {
        return CompletableFuture.supplyAsync { loadExternalMediaListSync(externalListIds) }
    }

    override fun loadExternalMediaListSync(externalListIds: Collection<Int>): List<ClientExternalMediaList>? {
        println("loading ExtLists: " + externalListIds + " on " + Thread.currentThread())
        //        try {
//                List<ClientEpisode> body = this.client.getExternalUser(episodeIds).execute().body();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        // todo implement loading of externalMediaLists
        return null
    }

    override fun loadExternalUserAsync(externalUuids: Collection<String>): CompletableFuture<List<ClientExternalUser>?> {
        return CompletableFuture.supplyAsync { loadExternalUserSync(externalUuids) }
    }

    override fun loadExternalUserSync(externalUuids: Collection<String>): List<ClientExternalUser>? {
        return try {
            println("loading ExternalUser: " + externalUuids + " on " + Thread.currentThread())
            client.getExternalUser(externalUuids).body()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override fun loadNewsAsync(newsIds: Collection<Int>): CompletableFuture<List<ClientNews>?> {
        return CompletableFuture.supplyAsync { loadNewsSync(newsIds) }
    }

    override fun loadNewsSync(newsIds: Collection<Int>): List<ClientNews>? {
        return try {
            println("loading News: " + newsIds + " on " + Thread.currentThread())
            client.getNews(newsIds).body()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override val news: LiveData<PagedList<News>>
        get() = storage.getNews()

    override fun removeOldNews() {
        TaskManager.runTask { storage.deleteOldNews() }
    }

    override val isLoading: Boolean
        get() = storage.isLoading()

    @Throws(IOException::class)
    override fun refreshNews(latest: DateTime?) {
        val news = Utils.checkAndGetBody(
            client.getNews(latest, null)
        )
        if (news != null) {
            persister.persistNews(news)
        }
    }

    @Throws(IOException::class)
    override fun loadInvalidated() {
        val invalidatedData = client.invalidated.body()
        if (invalidatedData == null || invalidatedData.isEmpty()) {
            return
        }
        var userUpdated = false
        val loadWorker = loadWorker
        for (datum in invalidatedData) {
            if (datum.isUserUuid) {
                userUpdated = true
            } else if (datum.episodeId > 0) {
                loadWorker.addIntegerIdTask(datum.episodeId, null, loadWorker.EPISODE_LOADER)
            } else if (datum.partId > 0) {
                loadWorker.addIntegerIdTask(datum.partId, null, loadWorker.PART_LOADER)
            } else if (datum.mediumId > 0) {
                loadWorker.addIntegerIdTask(datum.mediumId, null, loadWorker.MEDIUM_LOADER)
            } else if (datum.listId > 0) {
                loadWorker.addIntegerIdTask(datum.listId, null, loadWorker.MEDIALIST_LOADER)
            } else if (datum.externalListId > 0) {
                loadWorker.addIntegerIdTask(
                    datum.externalListId,
                    null,
                    loadWorker.EXTERNAL_MEDIALIST_LOADER
                )
            } else if (datum.externalUuid != null && !datum.externalUuid.isEmpty()) {
                loadWorker.addStringIdTask(
                    datum.externalUuid,
                    null,
                    loadWorker.EXTERNAL_USER_LOADER
                )
            } else if (datum.newsId > 0) {
                loadWorker.addIntegerIdTask(datum.newsId, null, loadWorker.NEWS_LOADER)
            } else {
                println("unknown invalid data: $datum")
            }
        }
        if (userUpdated) {
            val user = client.checkLogin().body()
            persister.persist(user)
        }
        loadWorker.work()
    }

    override val savedEpisodes: List<Int>
        get() = storage.getSavedEpisodes()

    override fun updateSaved(episodeId: Int, saved: Boolean) {
        storage.updateSaved(episodeId, saved)
    }

    override fun updateSaved(episodeIds: Collection<Int>, saved: Boolean) {
        try {
            Utils.doPartitionedEx(episodeIds) { ids: List<Int> ->
                storage.updateSaved(ids, saved)
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override val toDeleteEpisodes: List<Int>
        get() = storage.getToDeleteEpisodes()

    @Throws(IOException::class)
    override fun downloadEpisodes(episodeIds: Collection<Int>): List<ClientDownloadedEpisode>? {
        return client.downloadEpisodes(episodeIds).body()
    }

    override val toDownload: List<ToDownload>
        get() = storage.getAllToDownloads()

    override fun addToDownload(toDownload: ToDownload) {
        persister.persist(toDownload).finish()
    }

    override fun removeToDownloads(toDownloads: Collection<ToDownload>) {
        storage.removeToDownloads(toDownloads)
    }

    override fun getExternalListItems(externalListId: Int): Collection<Int> {
        return storage.getExternalListItems(externalListId)
    }

    override fun getListItems(listId: Int): Collection<Int> {
        return storage.getListItems(listId)
    }

    override fun getDownloadableEpisodes(mediaIds: Collection<Int>): List<Int> {
        return storage.getDownloadableEpisodes(mediaIds)
    }

    override fun getDownloadableEpisodes(mediumId: Int, limit: Int): List<Int> {
        return storage.getDownloadableEpisodes(mediumId, limit)
    }

    override fun getDisplayEpisodes(filter: EpisodeViewModel.Filter): LiveData<PagedList<DisplayRelease>> {
        return storage.getDisplayEpisodes(filter)
    }

    override fun getDisplayEpisodesGrouped(
        saved: Int,
        medium: Int
    ): LiveData<PagedList<DisplayEpisode>> {
        return storage.getDisplayEpisodesGrouped(saved, medium)
    }

    override val lists: LiveData<List<MediaList>>
        get() = storage.getLists()

    override fun getListSettings(id: Int, isExternal: Boolean): LiveData<out MediaListSetting> {
        return storage.getListSetting(id, isExternal)
    }

    override fun updateListName(
        listSetting: MediaListSetting,
        newName: String
    ): CompletableFuture<String> {
        return editService.updateListName(listSetting, newName)
    }

    override fun updateListMedium(
        listSetting: MediaListSetting,
        newMediumType: Int
    ): CompletableFuture<String> {
        return editService.updateListMedium(listSetting, newMediumType)
    }

    override fun updateToDownload(add: Boolean, toDownload: ToDownload) {
        storage.updateToDownload(add, toDownload)
    }

    override fun getAllMedia(
        sortings: Sortings,
        title: String?,
        medium: Int,
        author: String?,
        lastUpdate: DateTime?,
        minCountEpisodes: Int,
        minCountReadEpisodes: Int
    ): LiveData<PagedList<MediumItem>> {
        return storage.getAllMedia(
            sortings,
            title,
            medium,
            author,
            lastUpdate,
            minCountEpisodes,
            minCountReadEpisodes
        )
    }

    override fun getMediumSettings(mediumId: Int): LiveData<MediumSetting?> {
        return storage.getMediumSettings(mediumId)
    }

    override fun updateMedium(mediumSettings: MediumSetting): CompletableFuture<String> {
        return editService.updateMedium(mediumSettings)
    }

    override fun getToc(
        mediumId: Int,
        sortings: Sortings,
        read: Byte,
        saved: Byte
    ): LiveData<PagedList<TocEpisode>> {
        return storage.getToc(mediumId, sortings, read, saved)
    }

    override fun getMediumItems(listId: Int, isExternal: Boolean): LiveData<List<MediumItem>> {
        return storage.getMediumItems(listId, isExternal)
    }

    @Throws(IOException::class)
    override fun loadMediaInWaitSync() {
        val medium = Utils.checkAndGetBody(client.mediumInWait)
        if (medium != null && !medium.isEmpty()) {
            storage.clearMediaInWait()
            persister.persistMediaInWait(medium)
        }
    }

    @Throws(IOException::class)
    override fun addList(list: MediaList, autoDownload: Boolean) {
        val value = user.value
        check(!(value == null || value.uuid.isEmpty())) { "user is not authenticated" }
        val mediaList = ClientMinList(
            list.name,
            list.medium
        )
        val clientMediaList = client.addList(mediaList).body()
            ?: throw IllegalArgumentException("adding list failed")
        persister.persist(clientMediaList)
        val toDownload = ToDownload(
            false,
            null,
            clientMediaList.id,
            null
        )
        storage.updateToDownload(true, toDownload)
    }

    override fun listExists(listName: String): Boolean {
        return storage.listExists(listName)
    }

    override fun countSavedUnreadEpisodes(mediumId: Int): Int {
        return storage.countSavedEpisodes(mediumId)
    }

    override fun getSavedEpisodes(mediumId: Int): List<Int> {
        return storage.getSavedEpisodes(mediumId)
    }

    override fun getEpisode(episodeId: Int): Episode {
        return storage.getEpisode(episodeId)
    }

    override fun getSimpleEpisodes(ids: Collection<Int>): List<SimpleEpisode> {
        return storage.getSimpleEpisodes(ids)
    }

    override fun addProgressListener(consumer: Consumer<Int>) {
        loadWorker.addProgressListener(consumer)
    }

    override fun removeProgressListener(consumer: Consumer<Int>) {
        loadWorker.removeProgressListener(consumer)
    }

    override fun addTotalWorkListener(consumer: Consumer<Int>) {
        loadWorker.addTotalWorkListener(consumer)
    }

    override fun removeTotalWorkListener(consumer: Consumer<Int>) {
        loadWorker.removeTotalWorkListener(consumer)
    }

    override val loadWorkerProgress: Int
        get() = loadWorker.progress
    override val loadWorkerTotalWork: Int
        get() = loadWorker.totalWork

    override fun syncProgress() {
        storage.syncProgress()
    }

    override fun updateDataStructure(mediaIds: List<Int>, partIds: List<Int>) {
        storage.updateDataStructure(mediaIds, partIds)
    }

    @Throws(Exception::class)
    override fun reloadLowerIndex(combiIndex: Double, mediumId: Int) {
        val episodeIds = storage.getEpisodeIdsWithLowerIndex(combiIndex, mediumId)
        reloadEpisodes(episodeIds)
    }

    @Throws(Exception::class)
    override fun reloadHigherIndex(combiIndex: Double, mediumId: Int) {
        val episodeIds = storage.getEpisodeIdsWithHigherIndex(combiIndex, mediumId)
        reloadEpisodes(episodeIds)
    }

    @Throws(Exception::class)
    override fun reload(episodeIds: Set<Int>) {
        reloadEpisodes(episodeIds)
    }

    @Throws(IOException::class)
    override fun reloadAll(mediumId: Int) {
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
        val partBody = client.getParts(partIds).body()
            ?: return
        val loadedPartIds: MutableList<Int> = ArrayList()
        for (part in partBody) {
            loadedPartIds.add(part.id)
        }
        val generator = LoadWorkGenerator(loadedData)
        val filteredParts = generator.filterParts(partBody)
        persister.persist(filteredParts)
        partIds.removeAll(loadedPartIds)
        storage.removeParts(partIds)
    }

    @Throws(Exception::class)
    private fun reloadEpisodes(episodeIds: Collection<Int>) {
        Utils.doPartitionedEx(episodeIds) { integers: MutableList<Int> ->
            val episodes = client.getEpisodes(integers).body()
                ?: return@doPartitionedEx true
            val generator = LoadWorkGenerator(loadedData)
            val filteredEpisodes = generator.filterEpisodes(episodes)
            persister.persist(filteredEpisodes)
            val loadedIds: MutableList<Int> = ArrayList()
            for (episode in episodes) {
                loadedIds.add(episode.id)
            }
            integers.removeAll(loadedIds)
            storage.removeEpisodes(integers)
            true
        }
    }

    override fun downloadLowerIndex(combiIndex: Double, mediumId: Int, context: Context) {
        val episodeIds = storage.getEpisodeIdsWithLowerIndex(combiIndex, mediumId)
        DownloadWorker.enqueueDownloadTask(context, mediumId, episodeIds)
    }

    override fun downloadHigherIndex(combiIndex: Double, mediumId: Int, context: Context) {
        val episodeIds = storage.getEpisodeIdsWithHigherIndex(combiIndex, mediumId)
        DownloadWorker.enqueueDownloadTask(context, mediumId, episodeIds)
    }

    override fun download(episodeIds: Set<Int>, mediumId: Int, context: Context) {
        DownloadWorker.enqueueDownloadTask(context, mediumId, episodeIds)
    }

    override fun downloadAll(mediumId: Int, context: Context) {
        val episodeIds = storage.getAllEpisodes(mediumId)
        DownloadWorker.enqueueDownloadTask(context, mediumId, episodeIds)
    }

    override fun updateProgress(episodeId: Int, progress: Float) {
        TaskManager.Companion.runTask(Runnable {
            storage.updateProgress(
                setOf(episodeId),
                progress
            )
        })
    }

    override fun getClient(worker: Worker): Client {
        require(!(worker == null || worker.isStopped)) { "not an active Worker" }
        return client
    }

    override fun getPersister(worker: Worker): ClientModelPersister {
        require(!(worker == null || worker.isStopped)) { "not an active Worker" }
        return persister
    }

    override fun isMediumLoaded(mediumId: Int): Boolean {
        return loadedData.media.contains(mediumId)
    }

    override fun isPartLoaded(partId: Int): Boolean {
        return loadedData.part.contains(partId)
    }

    override fun isEpisodeLoaded(episodeId: Int): Boolean {
        return loadedData.episodes.contains(episodeId)
    }

    override fun isExternalUserLoaded(uuid: String): Boolean {
        return loadedData.externalUser.contains(uuid)
    }

    override fun checkReload(stat: ParsedStat): ReloadStat {
        return storage.checkReload(stat)
    }

    @Throws(IOException::class)
    override fun deleteLocalEpisodesWithLowerIndex(
        combiIndex: Double,
        mediumId: Int,
        application: Application
    ) {
        val episodeIds = storage.getSavedEpisodeIdsWithLowerIndex(combiIndex, mediumId)
        deleteLocalEpisodes(HashSet(episodeIds), mediumId, application)
    }

    @Throws(IOException::class)
    override fun deleteLocalEpisodesWithHigherIndex(
        combiIndex: Double,
        mediumId: Int,
        application: Application
    ) {
        val episodeIds = storage.getSavedEpisodeIdsWithHigherIndex(combiIndex, mediumId)
        deleteLocalEpisodes(HashSet(episodeIds), mediumId, application)
    }

    @Throws(IOException::class)
    override fun deleteAllLocalEpisodes(mediumId: Int, application: Application) {
        val episodes: Collection<Int> = storage.getSavedEpisodes(mediumId)
        deleteLocalEpisodes(HashSet(episodes), mediumId, application)
    }

    @Throws(IOException::class)
    override fun deleteLocalEpisodes(
        episodeIds: Set<Int>,
        mediumId: Int,
        application: Application
    ) {
        val medium = getMediumType(mediumId)
        val contentTool = FileTools.getContentTool(medium, application)
        if (!contentTool.isSupported) {
            throw IOException("medium type: $medium is not supported")
        }
        contentTool.removeMediaEpisodes(mediumId, episodeIds)
        this.updateSaved(episodeIds, false)
    }

    @Throws(Exception::class)
    override fun updateReadWithHigherIndex(combiIndex: Double, read: Boolean, mediumId: Int) {
        val episodeIds = storage.getEpisodeIdsWithHigherIndex(combiIndex, mediumId, read)
        this.updateRead(episodeIds, read)
    }

    @Throws(Exception::class)
    override fun updateAllRead(mediumId: Int, read: Boolean) {
        val episodeIds = storage.getAllEpisodes(mediumId)
        this.updateRead(episodeIds, read)
    }

    @Throws(Exception::class)
    override fun updateRead(episodeId: Int, read: Boolean) {
        editService.updateRead(listOf(episodeId), read)
    }

    @Throws(Exception::class)
    override fun updateRead(episodeIds: Collection<Int>, read: Boolean) {
        editService.updateRead(episodeIds, read)
    }

    override val readTodayEpisodes: LiveData<PagedList<ReadEpisode>>
        get() = storage.getReadTodayEpisodes()

    override fun getMediaInWaitBy(
        filter: String,
        mediumFilter: Int,
        hostFilter: String,
        sortings: Sortings
    ): LiveData<PagedList<MediumInWait>> {
        return storage.getMediaInWaitBy(filter, mediumFilter, hostFilter, sortings)
    }

    override val internLists: LiveData<List<MediaList>>
        get() = storage.getInternLists()

    override fun getSimilarMediaInWait(mediumInWait: MediumInWait): LiveData<List<MediumInWait>> {
        return storage.getSimilarMediaInWait(mediumInWait)
    }

    override fun getMediaSuggestions(title: String, medium: Int): LiveData<List<SimpleMedium>> {
        return storage.getMediaSuggestions(title, medium)
    }

    override fun getMediaInWaitSuggestions(
        title: String,
        medium: Int
    ): LiveData<List<MediumInWait>> {
        return storage.getMediaInWaitSuggestions(title, medium)
    }

    override fun consumeMediumInWait(
        selectedMedium: SimpleMedium,
        mediumInWaits: List<MediumInWait>
    ): CompletableFuture<Boolean> {
        return TaskManager.runCompletableTask {
            val others: MutableCollection<ClientMediumInWait> = HashSet()
            for (inWait in mediumInWaits) {
                others.add(
                    ClientMediumInWait(
                        inWait.title,
                        inWait.medium,
                        inWait.link
                    )
                )
            }
            try {
                val success = client.consumeMediumInWait(selectedMedium.mediumId, others).body()
                if (success != null && success) {
                    storage.deleteMediaInWait(mediumInWaits)
                    return@runCompletableTask true
                } else {
                    return@runCompletableTask false
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return@runCompletableTask false
            }
        }
    }

    override fun createMedium(
        mediumInWait: MediumInWait,
        mediumInWaits: List<MediumInWait>,
        list: MediaList
    ): CompletableFuture<Boolean> {
        return TaskManager.runCompletableTask {
            val medium = ClientMediumInWait(
                mediumInWait.title,
                mediumInWait.medium,
                mediumInWait.link
            )
            val others: MutableCollection<ClientMediumInWait> = HashSet()
            for (inWait in mediumInWaits) {
                others.add(
                    ClientMediumInWait(
                        inWait.title,
                        inWait.medium,
                        inWait.link
                    )
                )
            }
            val listId = list.listId
            try {
                val clientMedium = client.createFromMediumInWait(medium, others, listId).body()
                    ?: return@runCompletableTask false
                persister.persist(ClientSimpleMedium(clientMedium))
                val toDelete: MutableCollection<MediumInWait> = HashSet()
                toDelete.add(mediumInWait)
                toDelete.addAll(mediumInWaits)
                storage.deleteMediaInWait(toDelete)
                if (listId > 0) {
                    storage.addItemsToList(listId, setOf(clientMedium.id))
                }
                return@runCompletableTask true
            } catch (e: IOException) {
                e.printStackTrace()
                return@runCompletableTask false
            }
        }
    }

    override fun moveMediaToList(
        oldListId: Int,
        listId: Int,
        ids: MutableCollection<Int>
    ): CompletableFuture<Boolean> {
        return editService.moveMediaToList(oldListId, listId, ids)
    }

    override fun removeItemFromList(listId: Int, mediumId: Int): CompletableFuture<Boolean> {
        return editService.removeItemFromList(listId, mutableSetOf(mediumId))
    }

    override fun removeItemFromList(
        listId: Int,
        mediumId: MutableCollection<Int>
    ): CompletableFuture<Boolean> {
        return editService.removeItemFromList(listId, mediumId)
    }

    override fun addMediumToList(
        listId: Int,
        ids: MutableCollection<Int>
    ): CompletableFuture<Boolean> {
        return editService.addMediumToList(listId, ids)
    }

    override fun moveItemFromList(
        oldListId: Int,
        newListId: Int,
        mediumId: Int
    ): CompletableFuture<Boolean> {
        return editService.moveItemFromList(oldListId, newListId, mediumId)
    }

    override val externalUser: LiveData<PagedList<ExternalUser>>
        get() = storage.getExternalUser()

    override fun getSpaceMedium(mediumId: Int): SpaceMedium {
        return storage.getSpaceMedium(mediumId)
    }

    override fun getMediumType(mediumId: Int): Int {
        return storage.getMediumType(mediumId)
    }

    override fun getReleaseLinks(episodeId: Int): List<String> {
        return storage.getReleaseLinks(episodeId)
    }

    @Throws(IOException::class)
    override fun syncUser() {
        val user = client.user
        val body = user.body()
        if (!user.isSuccessful) {
            try {
                user.errorBody().use { responseBody ->
                    if (responseBody != null) {
                        println(responseBody.string())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        persister.persist(body)
    }

    @Throws(Exception::class)
    override fun updateReadWithLowerIndex(combiIndex: Double, read: Boolean, mediumId: Int) {
        val episodeIds = storage.getEpisodeIdsWithLowerIndex(combiIndex, mediumId, read)
        this.updateRead(episodeIds, read)
    }

    override fun clearLocalMediaData(context: Context) {
        UserPreferences.setLastSync(DateTime(0))
        TaskManager.Companion.runTask(Runnable {
            loadedData.part.clear()
            loadedData.episodes.clear()
            storage.clearLocalMediaData()
        })
    }

    override val notifications: LiveData<PagedList<NotificationItem>>
        get() = storage.getNotifications()

    override fun updateFailedDownloads(episodeId: Int) {
        storage.updateFailedDownload(episodeId)
    }

    override fun getFailedEpisodes(episodeIds: Collection<Int>): List<FailedEpisode> {
        return storage.getFailedEpisodes(episodeIds)
    }

    override fun addNotification(notification: NotificationItem) {
        storage.addNotification(notification)
    }

    override fun getSimpleEpisode(episodeId: Int): SimpleEpisode {
        return storage.getSimpleEpisode(episodeId)
    }

    override fun getSimpleMedium(mediumId: Int): SimpleMedium {
        return storage.getSimpleMedium(mediumId)
    }

    override fun clearNotifications() {
        storage.clearNotifications()
    }

    override fun clearFailEpisodes() {
        TaskManager.runAsyncTask { storage.clearFailEpisodes() }
    }

    override fun getListSuggestion(name: String): LiveData<List<MediaList>> {
        return storage.getListSuggestion(name)
    }

    override fun onDownloadable(): LiveData<Boolean> {
        return storage.onDownloadAble()
    }

    override fun removeDanglingMedia(mediaIds: Collection<Int>) {
        storage.removeDanglingMedia(mediaIds)
    }

    override val allDanglingMedia: LiveData<List<MediumItem>>
        get() = storage.getAllDanglingMedia()

    companion object {
        private var INSTANCE: RepositoryImpl? = null

        /**
         * Return the Repository Singleton Instance.
         *
         * @return returns the singleton
         */
        @kotlin.jvm.JvmStatic
        val instance: Repository
            get() {
                checkNotNull(INSTANCE) { "Repository not yet initialized" }
                return INSTANCE!!
            }

        fun getInstance(application: Application): Repository {
            if (INSTANCE == null) {
                synchronized(RepositoryImpl::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = RepositoryImpl(application)
                        INSTANCE!!.storage.setLoading(true)
                        println("querying")

                        // storage.getHomeStats() does nothing, but storageHomeStatsLiveData invalidates instantly
                        //  storageHomeStatsLiveData does nothing
//                    new Handler(Looper.getMainLooper()).post(() -> );
                        // check first login
                        TaskManager.runTask {
                            try {
                                // ask the database what data it has, to check if it needs to be loaded from the server
                                INSTANCE!!.loadLoadedData()
                                val call = INSTANCE!!.client.checkLogin()
                                val clientUser = call.body()
                                if (clientUser != null) {
                                    INSTANCE!!.client.setAuthentication(
                                        clientUser.getUuid(),
                                        clientUser.getSession()
                                    )
                                }
                                INSTANCE!!.persister.persist(clientUser).finish()
                                Log.i(RepositoryImpl::class.java.simpleName, "successful query")
                            } catch (e: IOException) {
                                Log.e(RepositoryImpl::class.java.simpleName, "failed query", e)
                            }
                        }
                    }
                }
            }
            return INSTANCE!!
        }
    }

    init {
        storage = RoomStorage(application)
        loadedData = LoadData()
        user = Transformations.map(storage.getUser()) { value: User? ->
            if (value == null) {
                INSTANCE!!.client.clearAuthentication()
            } else {
                INSTANCE!!.client.setAuthentication(value.uuid, value.session)
            }
            value
        }
        persister = storage.getPersister(this, loadedData)
        val identificator: NetworkIdentificator =
            AndroidNetworkIdentificator(application.applicationContext)
        client = Client(identificator)
        val dependantGenerator = storage.getDependantGenerator(loadedData)
        loadWorker = BlockingLoadWorker(
            loadedData,
            this,
            persister,
            dependantGenerator
        )
        editService = EditService(client, storage, persister)
    }
}