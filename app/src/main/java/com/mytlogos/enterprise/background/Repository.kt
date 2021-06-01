package com.mytlogos.enterprise.background

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import androidx.work.Worker
import com.mytlogos.enterprise.background.api.Client
import com.mytlogos.enterprise.background.api.model.*
import com.mytlogos.enterprise.background.api.model.ClientStat.ParsedStat
import com.mytlogos.enterprise.background.resourceLoader.LoadWorker
import com.mytlogos.enterprise.model.*
import com.mytlogos.enterprise.tools.Sortings
import com.mytlogos.enterprise.viewmodel.EpisodeViewModel
import org.joda.time.DateTime
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

interface Repository {
    val isClientOnline: Boolean
    val isClientAuthenticated: Boolean
    val loadWorker: LoadWorker
    val homeStats: LiveData<HomeStats>
    val user: LiveData<User?>
    fun updateUser(updateUser: UpdateUser)
    fun deleteAllUser()

    @Throws(IOException::class)
    fun login(email: String, password: String)

    @Throws(IOException::class)
    fun register(email: String, password: String)
    fun logout()
    fun loadAllMedia()
    fun loadEpisodeAsync(episodeIds: Collection<Int>): CompletableFuture<List<ClientEpisode>?>
    fun loadEpisodeSync(episodeIds: Collection<Int>): List<ClientEpisode>?
    fun loadMediaAsync(mediaIds: Collection<Int>): CompletableFuture<List<ClientMedium>?>
    fun loadMediaSync(mediaIds: Collection<Int>): List<ClientMedium>?
    fun loadPartAsync(partIds: Collection<Int>): CompletableFuture<List<ClientPart>?>
    fun loadPartSync(partIds: Collection<Int>): List<ClientPart>?
    fun loadMediaListAsync(listIds: Collection<Int>): CompletableFuture<ClientMultiListQuery?>
    fun loadMediaListSync(listIds: Collection<Int>): ClientMultiListQuery?
    fun loadExternalMediaListAsync(externalListIds: Collection<Int>): CompletableFuture<List<ClientExternalMediaList>?>
    fun loadExternalMediaListSync(externalListIds: Collection<Int>): List<ClientExternalMediaList>?
    fun loadExternalUserAsync(externalUuids: Collection<String>): CompletableFuture<List<ClientExternalUser>?>
    fun loadExternalUserSync(externalUuids: Collection<String>): List<ClientExternalUser>?
    fun loadNewsAsync(newsIds: Collection<Int>): CompletableFuture<List<ClientNews>?>
    fun loadNewsSync(newsIds: Collection<Int>): List<ClientNews>?
    val news: LiveData<PagedList<News>>
    fun removeOldNews()
    val isLoading: Boolean

    @Throws(IOException::class)
    fun refreshNews(latest: DateTime?)

    @Throws(IOException::class)
    fun loadInvalidated()
    val savedEpisodes: List<Int>
    fun updateSaved(episodeId: Int, saved: Boolean)
    fun updateSaved(episodeIds: Collection<Int>, saved: Boolean)
    val toDeleteEpisodes: List<Int>

    @Throws(IOException::class)
    fun downloadEpisodes(episodeIds: Collection<Int>): List<ClientDownloadedEpisode>?
    val toDownload: List<ToDownload>
    fun addToDownload(toDownload: ToDownload)
    fun removeToDownloads(toDownloads: Collection<ToDownload>)
    fun getExternalListItems(externalListId: Int): Collection<Int>
    fun getListItems(listId: Int): Collection<Int>
    fun getDownloadableEpisodes(mediaIds: Collection<Int>): List<Int>
    fun getDownloadableEpisodes(mediumId: Int, limit: Int): List<Int>
    fun getDisplayEpisodes(filter: EpisodeViewModel.Filter): LiveData<PagedList<DisplayRelease>>
    fun getDisplayEpisodesGrouped(saved: Int, medium: Int): LiveData<PagedList<DisplayEpisode>>
    val lists: LiveData<List<MediaList>>
    fun getListSettings(id: Int, isExternal: Boolean): LiveData<out MediaListSetting?>
    fun updateListName(listSetting: MediaListSetting, newName: String): CompletableFuture<String>
    fun updateListMedium(
        listSetting: MediaListSetting,
        newMediumType: Int
    ): CompletableFuture<String>

    fun updateToDownload(add: Boolean, toDownload: ToDownload)
    fun getAllMedia(
        sortings: Sortings,
        title: String?,
        medium: Int,
        author: String?,
        lastUpdate: DateTime?,
        minCountEpisodes: Int,
        minCountReadEpisodes: Int
    ): LiveData<PagedList<MediumItem>>

    fun getMediumSettings(mediumId: Int): LiveData<MediumSetting?>
    fun updateMedium(mediumSettings: MediumSetting): CompletableFuture<String>
    fun getToc(
        mediumId: Int,
        sortings: Sortings,
        read: Byte,
        saved: Byte
    ): LiveData<PagedList<TocEpisode>>

    fun getMediumItems(listId: Int, isExternal: Boolean): LiveData<List<MediumItem>>

    @Throws(IOException::class)
    fun loadMediaInWaitSync()

    @Throws(IOException::class)
    fun addList(list: MediaList, autoDownload: Boolean)
    fun listExists(listName: String): Boolean
    fun countSavedUnreadEpisodes(mediumId: Int): Int
    fun getSavedEpisodes(mediumId: Int): List<Int>
    fun getEpisode(episodeId: Int): Episode
    fun getSimpleEpisodes(ids: Collection<Int>): List<SimpleEpisode>
    val readTodayEpisodes: LiveData<PagedList<ReadEpisode>>
    fun getMediaInWaitBy(
        filter: String?,
        mediumFilter: Int,
        hostFilter: String?,
        sortings: Sortings
    ): LiveData<PagedList<MediumInWait>>

    val internLists: LiveData<List<MediaList>>
    fun moveMediaToList(
        oldListId: Int,
        listId: Int,
        ids: MutableCollection<Int>
    ): CompletableFuture<Boolean>

    fun getSimilarMediaInWait(mediumInWait: MediumInWait): LiveData<List<MediumInWait>>
    fun getMediaSuggestions(title: String, medium: Int): LiveData<List<SimpleMedium>>
    fun getMediaInWaitSuggestions(title: String, medium: Int): LiveData<List<MediumInWait>>
    fun consumeMediumInWait(
        selectedMedium: SimpleMedium,
        mediumInWaits: List<MediumInWait>
    ): CompletableFuture<Boolean>

    fun createMedium(
        mediumInWait: MediumInWait,
        mediumInWaits: List<MediumInWait>,
        list: MediaList
    ): CompletableFuture<Boolean>

    fun removeItemFromList(listId: Int, mediumId: Int): CompletableFuture<Boolean>
    fun removeItemFromList(listId: Int, mediumId: MutableCollection<Int>): CompletableFuture<Boolean>
    fun moveItemFromList(
        oldListId: Int,
        newListId: Int,
        mediumId: Int
    ): CompletableFuture<Boolean>

    fun getListSuggestion(name: String): LiveData<List<MediaList>>
    fun onDownloadable(): LiveData<Boolean>
    fun removeDanglingMedia(mediaIds: Collection<Int>)
    val allDanglingMedia: LiveData<List<MediumItem>>
    fun addMediumToList(listId: Int, ids: MutableCollection<Int>): CompletableFuture<Boolean>
    val externalUser: LiveData<PagedList<ExternalUser>>
    fun getSpaceMedium(mediumId: Int): SpaceMedium
    fun getMediumType(mediumId: Int): Int
    fun getReleaseLinks(episodeId: Int): List<String>

    @Throws(IOException::class)
    fun syncUser()
    fun clearLocalMediaData(context: Context)
    val notifications: LiveData<PagedList<NotificationItem>>
    fun updateFailedDownloads(episodeId: Int)
    fun getFailedEpisodes(episodeIds: Collection<Int>): List<FailedEpisode>
    fun addNotification(notification: NotificationItem)
    fun getSimpleEpisode(episodeId: Int): SimpleEpisode
    fun getSimpleMedium(mediumId: Int): SimpleMedium
    fun clearNotifications()
    fun clearFailEpisodes()

    @Throws(Exception::class)
    fun updateRead(episodeId: Int, read: Boolean)

    @Throws(Exception::class)
    fun updateRead(episodeIds: Collection<Int>, read: Boolean)

    @Throws(Exception::class)
    fun updateAllRead(episodeId: Int, read: Boolean)

    @Throws(Exception::class)
    fun updateReadWithHigherIndex(combiIndex: Double, read: Boolean, mediumId: Int)

    @Throws(Exception::class)
    fun updateReadWithLowerIndex(combiIndex: Double, read: Boolean, mediumId: Int)

    @Throws(IOException::class)
    fun deleteAllLocalEpisodes(mediumId: Int, application: Application)

    @Throws(IOException::class)
    fun deleteLocalEpisodesWithLowerIndex(
        episodeId: Double,
        mediumId: Int,
        application: Application
    )

    @Throws(IOException::class)
    fun deleteLocalEpisodesWithHigherIndex(
        combiIndex: Double,
        mediumId: Int,
        application: Application
    )

    @Throws(IOException::class)
    fun deleteLocalEpisodes(episodeId: Set<Int>, mediumId: Int, application: Application)
    fun addProgressListener(consumer: Consumer<Int>)
    fun removeProgressListener(consumer: Consumer<Int>)
    fun addTotalWorkListener(consumer: Consumer<Int>)
    fun removeTotalWorkListener(consumer: Consumer<Int>)
    val loadWorkerProgress: Int
    val loadWorkerTotalWork: Int
    fun syncProgress()
    fun updateDataStructure(mediaIds: List<Int>, partIds: List<Int>)

    @Throws(Exception::class)
    fun reloadLowerIndex(combiIndex: Double, mediumId: Int)

    @Throws(Exception::class)
    fun reloadHigherIndex(combiIndex: Double, mediumId: Int)

    @Throws(Exception::class)
    fun reload(episodeIds: Set<Int>)

    @Throws(IOException::class)
    fun reloadAll(mediumId: Int)
    fun downloadLowerIndex(combiIndex: Double, mediumId: Int, context: Context)
    fun downloadHigherIndex(combiIndex: Double, mediumId: Int, context: Context)
    fun download(episodeId: Set<Int>, mediumId: Int, context: Context)
    fun downloadAll(mediumId: Int, context: Context)
    fun updateProgress(episodeId: Int, progress: Float)
    fun getClient(worker: Worker): Client
    fun getPersister(worker: Worker): ClientModelPersister
    fun isMediumLoaded(mediumId: Int): Boolean
    fun isPartLoaded(partId: Int): Boolean
    fun isEpisodeLoaded(episodeId: Int): Boolean
    fun isExternalUserLoaded(uuid: String): Boolean
    fun checkReload(stat: ParsedStat): ReloadStat
}