package com.mytlogos.enterprise.background

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagingData
import com.mytlogos.enterprise.background.api.AndroidNetworkIdentificator
import com.mytlogos.enterprise.background.api.Client
import com.mytlogos.enterprise.background.api.NetworkIdentificator
import com.mytlogos.enterprise.background.api.model.ClientStat.ParsedStat
import com.mytlogos.enterprise.background.room.RoomStorage
import com.mytlogos.enterprise.model.*
import com.mytlogos.enterprise.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow
import org.joda.time.DateTime
import java.io.IOException

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

    suspend fun getUserNow(): User? {
        return this.storage.getUserNow()
    }

    override val isLoading: Boolean
        get() = storage.isLoading()

    override fun getExternalListItems(externalListId: Int): Collection<Int> {
        return storage.getExternalListItems(externalListId)
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

    override fun getSimpleEpisodes(ids: Collection<Int>): List<SimpleEpisode> {
        return storage.getSimpleEpisodes(ids)
    }

    override fun syncProgress() {
        storage.syncProgress()
    }

    override fun updateProgress(episodeId: Int, progress: Float) {
        TaskManager.runTaskSuspend {
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

    override val readTodayEpisodes: Flow<PagingData<ReadEpisode>>
        get() = storage.getReadTodayEpisodes()

    override val externalUser: Flow<PagingData<ExternalUser>>
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
        TaskManager.runTask {
            loadedData.part.clear()
            loadedData.episodes.clear()
            storage.clearLocalMediaData()
        }
    }

    override fun getSimpleMedium(mediumId: Int): SimpleMedium {
        return storage.getSimpleMedium(mediumId)
    }

    override fun getListSuggestion(name: String): LiveData<MutableList<MediaList>> {
        return storage.getListSuggestion(name)
    }

    override fun onDownloadable(): LiveData<Boolean> {
        return storage.onDownloadAble()
    }

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
                    var instance = INSTANCE
                    if (instance == null) {
                        instance = RepositoryImpl(application)
                        INSTANCE = instance
                        instance.storage.setLoading(true)
                        println("querying")

                        // storage.getHomeStats() does nothing, but storageHomeStatsLiveData invalidates instantly
                        //  storageHomeStatsLiveData does nothing
//                    new Handler(Looper.getMainLooper()).post(() -> );
                        // check first login
                        TaskManager.runTask {
                            try {
                                // ask the database what data it has, to check if it needs to be loaded from the server
                                instance.loadLoadedData()
                                val call = instance.client.checkLogin()
                                val clientUser = call.body()

                                if (clientUser != null) {
                                    instance.client.setAuthentication(
                                        clientUser.getUuid(),
                                        clientUser.getSession()
                                    )
                                }
                                instance.persister.persist(clientUser).finish()
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