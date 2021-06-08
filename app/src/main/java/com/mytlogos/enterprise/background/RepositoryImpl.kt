package com.mytlogos.enterprise.background

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import androidx.paging.PagingData
import com.mytlogos.enterprise.background.api.AndroidNetworkIdentificator
import com.mytlogos.enterprise.background.api.Client
import com.mytlogos.enterprise.background.api.NetworkIdentificator
import com.mytlogos.enterprise.background.api.model.ClientDownloadedEpisode
import com.mytlogos.enterprise.background.api.model.ClientStat.ParsedStat
import com.mytlogos.enterprise.background.room.RoomStorage
import com.mytlogos.enterprise.model.*
import com.mytlogos.enterprise.preferences.UserPreferences
import com.mytlogos.enterprise.tools.Sortings
import com.mytlogos.enterprise.tools.Utils
import com.mytlogos.enterprise.viewmodel.EpisodeViewModel
import kotlinx.coroutines.flow.Flow
import org.joda.time.DateTime
import java.io.IOException
import java.util.concurrent.CompletableFuture

@Suppress("BlockingMethodInNonBlockingContext")
class RepositoryImpl private constructor(application: Application) : Repository {
    private val persister: ClientModelPersister
    override val user: LiveData<User?>
    private val client: Client
    private val storage: DatabaseStorage
    private val loadedData: LoadData
    internal val editService: EditService

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

    fun getUserNow(): User? {
        return this.storage.getUserNow()
    }

    /**
     * Synchronous Login.
     *
     * @param email    email or name of the user
     * @param password password of the user
     * @throws IOException if an connection problem arose
     */
    @Throws(IOException::class)
    override suspend fun login(email: String, password: String) {
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
    override suspend fun register(email: String, password: String) {
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

    override val isLoading: Boolean
        get() = storage.isLoading()

    override val savedEpisodes: List<Int>
        get() = storage.getSavedEpisodes()

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

    override suspend fun downloadEpisodes(episodeIds: Collection<Int>): List<ClientDownloadedEpisode>? {
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
        medium: Int,
    ): LiveData<PagedList<DisplayEpisode>> {
        return storage.getDisplayEpisodesGrouped(saved, medium)
    }

    override val lists: LiveData<MutableList<MediaList>>
        get() = storage.getLists()

    override fun getListSettings(id: Int, isExternal: Boolean): LiveData<out MediaListSetting> {
        return storage.getListSetting(id, isExternal)
    }

    override suspend fun updateListName(listSetting: MediaListSetting, newName: String): String {
        return editService.updateListName(listSetting, newName)
    }

    override suspend fun updateListMedium(
        listSetting: MediaListSetting,
        newMediumType: Int,
    ): String {
        return editService.updateListMedium(listSetting, newMediumType)
    }

    override fun updateToDownload(add: Boolean, toDownload: ToDownload) {
        storage.updateToDownload(add, toDownload)
    }

    override fun getMediumSettings(mediumId: Int): LiveData<MediumSetting> {
        return storage.getMediumSettings(mediumId)
    }

    override fun getMediumItems(
        listId: Int,
        isExternal: Boolean,
    ): LiveData<MutableList<MediumItem>> {
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

    override fun syncProgress() {
        storage.syncProgress()
    }

    override fun updateDataStructure(mediaIds: List<Int>, partIds: List<Int>) {
        storage.updateDataStructure(mediaIds, partIds)
    }

    override fun updateProgress(episodeId: Int, progress: Float) {
        TaskManager.runTask {
            storage.updateProgress(
                setOf(episodeId),
                progress
            )
        }
    }

    override fun getClient(): Client {
        return client
    }

    override fun getPersister(): ClientModelPersister {
        return persister
    }

    fun getLoadedData(): LoadData {
        return loadedData
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

    override val readTodayEpisodes: LiveData<PagedList<ReadEpisode>>
        get() = storage.getReadTodayEpisodes()

    override fun getMediaInWaitBy(
        filter: String?,
        mediumFilter: Int,
        hostFilter: String?,
        sortings: Sortings,
    ): Flow<PagingData<MediumInWait>> {
        return storage.getMediaInWaitBy(filter, mediumFilter, hostFilter, sortings)
    }

    override val internLists: LiveData<MutableList<MediaList>>
        get() = storage.getInternLists()

    override fun getSimilarMediaInWait(mediumInWait: MediumInWait): LiveData<MutableList<MediumInWait>> {
        return storage.getSimilarMediaInWait(mediumInWait)
    }

    override fun getMediaSuggestions(
        title: String,
        medium: Int,
    ): LiveData<MutableList<SimpleMedium>> {
        return storage.getMediaSuggestions(title, medium)
    }

    override fun getMediaInWaitSuggestions(
        title: String,
        medium: Int,
    ): LiveData<MutableList<MediumInWait>> {
        return storage.getMediaInWaitSuggestions(title, medium)
    }

    override fun moveMediaToList(
        oldListId: Int,
        listId: Int,
        ids: MutableCollection<Int>,
    ): CompletableFuture<Boolean> {
        return editService.moveMediaToList(oldListId, listId, ids)
    }

    override fun removeItemFromList(listId: Int, mediumId: Int): CompletableFuture<Boolean> {
        return editService.removeItemFromList(listId, mutableSetOf(mediumId))
    }

    override fun removeItemFromList(
        listId: Int,
        mediumId: MutableCollection<Int>,
    ): CompletableFuture<Boolean> {
        return editService.removeItemFromList(listId, mediumId)
    }

    override fun addMediumToList(
        listId: Int,
        ids: MutableCollection<Int>,
    ): CompletableFuture<Boolean> {
        return editService.addMediumToList(listId, ids)
    }

    override fun moveItemFromList(
        oldListId: Int,
        newListId: Int,
        mediumId: Int,
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

    override fun clearLocalMediaData(context: Context) {
        UserPreferences.lastSync = DateTime(0)
        TaskManager.Companion.runTask(Runnable {
            loadedData.part.clear()
            loadedData.episodes.clear()
            storage.clearLocalMediaData()
        })
    }

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

    override fun clearFailEpisodes() {
        TaskManager.runAsyncTask { storage.clearFailEpisodes() }
    }

    override fun getListSuggestion(name: String): LiveData<MutableList<MediaList>> {
        return storage.getListSuggestion(name)
    }

    override fun onDownloadable(): LiveData<Boolean> {
        return storage.onDownloadAble()
    }

    override fun removeDanglingMedia(mediaIds: Collection<Int>) {
        storage.removeDanglingMedia(mediaIds)
    }

    override val allDanglingMedia: LiveData<MutableList<MediumItem>>
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
        client = Client.getInstance(identificator)
        editService = EditService(client, storage, persister)
    }
}