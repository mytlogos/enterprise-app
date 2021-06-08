package com.mytlogos.enterprise.background

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import androidx.paging.PagingData
import com.mytlogos.enterprise.background.api.model.ClientStat.ParsedStat
import com.mytlogos.enterprise.model.*
import com.mytlogos.enterprise.tools.Sortings
import com.mytlogos.enterprise.viewmodel.EpisodeViewModel
import kotlinx.coroutines.flow.Flow

/**
 * Interface for querying and deleting data.
 * It models a server-driven database.
 *
 *
 * To insert or update data, [.getPersister] is used to persist data
 * from the server.
 *
 */
interface DatabaseStorage {
    fun getUser(): LiveData<User?>
    fun getUserNow(): User?
    fun getHomeStats(): LiveData<HomeStats>
    fun deleteAllUser()
    fun getPersister(repository: Repository, loadedData: LoadData): ClientModelPersister
    fun deleteOldNews()
    fun isLoading(): Boolean
    fun setLoading(loading: Boolean)
    fun getLoadData(): LoadData
    fun getNews(): LiveData<PagedList<News>>
    fun getSavedEpisodes(): List<Int>
    fun getToDeleteEpisodes(): List<Int>
    fun updateSaved(episodeId: Int, saved: Boolean)
    fun updateSaved(episodeIds: Collection<Int>, saved: Boolean)
    fun getAllToDownloads(): List<ToDownload>
    fun removeToDownloads(toDownloads: Collection<ToDownload>)
    fun getListItems(listId: Int): Collection<Int>
    fun getLiveListItems(listId: Int): LiveData<MutableList<Int>>
    fun getExternalListItems(externalListId: Int): Collection<Int>
    fun getLiveExternalListItems(externalListId: Int): LiveData<MutableList<Int>>
    fun getDownloadableEpisodes(mediumId: Int, limit: Int): List<Int>
    fun getDownloadableEpisodes(mediaIds: Collection<Int>): List<Int>
    fun getDisplayEpisodes(filter: EpisodeViewModel.Filter): LiveData<PagedList<DisplayRelease>>
    fun getDisplayEpisodesGrouped(saved: Int, medium: Int): LiveData<PagedList<DisplayEpisode>>
    fun getLists(): LiveData<MutableList<MediaList>>
    fun insertDanglingMedia(mediaIds: MutableCollection<Int>)
    fun removeDanglingMedia(mediaIds: Collection<Int>)
    fun getListSetting(id: Int, isExternal: Boolean): LiveData<out MediaListSetting?>
    suspend fun getListSettingNow(id: Int, isExternal: Boolean): MediaListSetting?
    fun updateToDownload(add: Boolean, toDownload: ToDownload)

    fun getMediumSettings(mediumId: Int): LiveData<MediumSetting>
    fun getMediumSettingsNow(mediumId: Int): MediumSetting

    fun getMediumItems(listId: Int, isExternal: Boolean): LiveData<MutableList<MediumItem>>
    fun countSavedEpisodes(mediumId: Int): Int
    fun getSavedEpisodes(mediumId: Int): List<Int>
    fun getEpisode(episodeId: Int): Episode
    fun getSimpleEpisodes(ids: Collection<Int>): List<SimpleEpisode>
    fun updateProgress(episodeIds: Collection<Int>, progress: Float)

    fun getMediaInWaitBy(
        filter: String?,
        mediumFilter: Int,
        hostFilter: String?,
        sortings: Sortings
    ): Flow<PagingData<MediumInWait>>

    fun getReadTodayEpisodes(): LiveData<PagedList<ReadEpisode>>
    fun getInternLists(): LiveData<MutableList<MediaList>>
    fun addItemsToList(listId: Int, ids: Collection<Int>)
    fun getSimilarMediaInWait(mediumInWait: MediumInWait): LiveData<MutableList<MediumInWait>>
    fun getMediaSuggestions(title: String, medium: Int): LiveData<MutableList<SimpleMedium>>
    fun getMediaInWaitSuggestions(title: String, medium: Int): LiveData<MutableList<MediumInWait>>
    fun getListSuggestion(name: String): LiveData<MutableList<MediaList>>
    fun onDownloadAble(): LiveData<Boolean>
    fun clearMediaInWait()
    fun deleteMediaInWait(toDelete: Collection<MediumInWait>)
    fun getAllDanglingMedia(): LiveData<MutableList<MediumItem>>
    fun removeItemFromList(listId: Int, mediumId: Int)
    fun removeItemFromList(listId: Int, mediumId: Collection<Int>)
    fun moveItemsToList(oldListId: Int, newListId: Int, ids: Collection<Int>)
    fun getExternalUser(): LiveData<PagedList<ExternalUser>>
    fun getSpaceMedium(mediumId: Int): SpaceMedium
    fun getMediumType(mediumId: Int): Int
    fun getReleaseLinks(episodeId: Int): List<String>
    fun clearLocalMediaData()
    fun updateFailedDownload(episodeId: Int)
    fun getFailedEpisodes(episodeIds: Collection<Int>): List<FailedEpisode>
    fun addNotification(notification: NotificationItem)
    fun getSimpleEpisode(episodeId: Int): SimpleEpisode
    fun getSimpleMedium(mediumId: Int): SimpleMedium
    fun clearFailEpisodes()
    fun syncProgress()
    fun updateDataStructure(mediaIds: List<Int>, partIds: List<Int>)
    fun getReadEpisodes(episodeIds: Collection<Int>, read: Boolean): List<Int>
    fun insertEditEvent(event: EditEvent)
    suspend fun insertEditEvent(events: Collection<EditEvent>)
    fun getEditEvents(): MutableList<out EditEvent>
    fun removeEditEvents(editEvents: Collection<EditEvent>)
    fun checkReload(parsedStat: ParsedStat): ReloadStat
}