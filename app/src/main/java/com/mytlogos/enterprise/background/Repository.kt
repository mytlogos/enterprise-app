package com.mytlogos.enterprise.background

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.mytlogos.enterprise.background.api.Client
import com.mytlogos.enterprise.background.api.model.*
import com.mytlogos.enterprise.background.api.model.ClientStat.ParsedStat
import com.mytlogos.enterprise.model.*
import com.mytlogos.enterprise.tools.Sortings
import com.mytlogos.enterprise.viewmodel.EpisodeViewModel
import org.joda.time.DateTime
import java.io.IOException
import java.util.concurrent.CompletableFuture

interface Repository {
    val isClientOnline: Boolean
    val isClientAuthenticated: Boolean
    val homeStats: LiveData<HomeStats>
    val user: LiveData<User?>
    fun updateUser(updateUser: UpdateUser)
    fun deleteAllUser()

    @Throws(IOException::class)
    fun login(email: String, password: String)

    @Throws(IOException::class)
    fun register(email: String, password: String)
    fun logout()
    val news: LiveData<PagedList<News>>
    fun removeOldNews()
    val isLoading: Boolean

    @Throws(IOException::class)
    fun refreshNews(latest: DateTime?)

    val savedEpisodes: List<Int>
    fun updateSaved(episodeIds: Collection<Int>, saved: Boolean)
    val toDeleteEpisodes: List<Int>

    suspend fun downloadEpisodes(episodeIds: Collection<Int>): List<ClientDownloadedEpisode>?
    val toDownload: List<ToDownload>
    fun addToDownload(toDownload: ToDownload)
    fun removeToDownloads(toDownloads: Collection<ToDownload>)
    fun getExternalListItems(externalListId: Int): Collection<Int>
    fun getListItems(listId: Int): Collection<Int>
    fun getDownloadableEpisodes(mediaIds: Collection<Int>): List<Int>
    fun getDownloadableEpisodes(mediumId: Int, limit: Int): List<Int>
    fun getDisplayEpisodes(filter: EpisodeViewModel.Filter): LiveData<PagedList<DisplayRelease>>
    fun getDisplayEpisodesGrouped(saved: Int, medium: Int): LiveData<PagedList<DisplayEpisode>>
    val lists: LiveData<MutableList<MediaList>>
    fun getListSettings(id: Int, isExternal: Boolean): LiveData<out MediaListSetting?>
    fun updateListName(listSetting: MediaListSetting, newName: String): CompletableFuture<String>
    fun updateListMedium(
        listSetting: MediaListSetting,
        newMediumType: Int,
    ): CompletableFuture<String>

    fun updateToDownload(add: Boolean, toDownload: ToDownload)

    fun getMediumSettings(mediumId: Int): LiveData<MediumSetting>
    fun updateMedium(mediumSettings: MediumSetting): CompletableFuture<String>

    fun getMediumItems(listId: Int, isExternal: Boolean): LiveData<MutableList<MediumItem>>

    @Throws(IOException::class)
    fun loadMediaInWaitSync()

    fun countSavedUnreadEpisodes(mediumId: Int): Int
    fun getSavedEpisodes(mediumId: Int): List<Int>
    fun getEpisode(episodeId: Int): Episode
    fun getSimpleEpisodes(ids: Collection<Int>): List<SimpleEpisode>
    val readTodayEpisodes: LiveData<PagedList<ReadEpisode>>
    fun getMediaInWaitBy(
        filter: String?,
        mediumFilter: Int,
        hostFilter: String?,
        sortings: Sortings,
    ): LiveData<PagedList<MediumInWait>>

    val internLists: LiveData<MutableList<MediaList>>

    fun moveMediaToList(
        oldListId: Int,
        listId: Int,
        ids: MutableCollection<Int>,
    ): CompletableFuture<Boolean>

    fun getSimilarMediaInWait(mediumInWait: MediumInWait): LiveData<MutableList<MediumInWait>>
    fun getMediaSuggestions(title: String, medium: Int): LiveData<MutableList<SimpleMedium>>
    fun getMediaInWaitSuggestions(title: String, medium: Int): LiveData<MutableList<MediumInWait>>
    fun consumeMediumInWait(
        selectedMedium: SimpleMedium,
        mediumInWaits: List<MediumInWait>,
    ): CompletableFuture<Boolean>

    fun createMedium(
        mediumInWait: MediumInWait,
        mediumInWaits: List<MediumInWait>,
        list: MediaList,
    ): CompletableFuture<Boolean>

    fun removeItemFromList(listId: Int, mediumId: Int): CompletableFuture<Boolean>
    fun removeItemFromList(
        listId: Int,
        mediumId: MutableCollection<Int>,
    ): CompletableFuture<Boolean>

    fun moveItemFromList(
        oldListId: Int,
        newListId: Int,
        mediumId: Int,
    ): CompletableFuture<Boolean>

    fun getListSuggestion(name: String): LiveData<MutableList<MediaList>>
    fun onDownloadable(): LiveData<Boolean>
    fun removeDanglingMedia(mediaIds: Collection<Int>)
    val allDanglingMedia: LiveData<MutableList<MediumItem>>
    fun addMediumToList(listId: Int, ids: MutableCollection<Int>): CompletableFuture<Boolean>
    val externalUser: LiveData<PagedList<ExternalUser>>
    fun getSpaceMedium(mediumId: Int): SpaceMedium
    fun getMediumType(mediumId: Int): Int
    fun getReleaseLinks(episodeId: Int): List<String>

    fun clearLocalMediaData(context: Context)
    val notifications: LiveData<PagedList<NotificationItem>>
    fun updateFailedDownloads(episodeId: Int)
    fun getFailedEpisodes(episodeIds: Collection<Int>): List<FailedEpisode>
    fun addNotification(notification: NotificationItem)
    fun getSimpleEpisode(episodeId: Int): SimpleEpisode
    fun getSimpleMedium(mediumId: Int): SimpleMedium
    fun clearNotifications()
    fun clearFailEpisodes()

    fun syncProgress()
    fun updateDataStructure(mediaIds: List<Int>, partIds: List<Int>)

    fun updateProgress(episodeId: Int, progress: Float)
    fun getClient(): Client
    fun getPersister(): ClientModelPersister
    fun isMediumLoaded(mediumId: Int): Boolean
    fun isPartLoaded(partId: Int): Boolean
    fun isEpisodeLoaded(episodeId: Int): Boolean
    fun isExternalUserLoaded(uuid: String): Boolean
    fun checkReload(stat: ParsedStat): ReloadStat
}