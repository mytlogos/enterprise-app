package com.mytlogos.enterprise.background

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.mytlogos.enterprise.background.api.model.ClientStat.ParsedStat
import com.mytlogos.enterprise.model.*
import com.mytlogos.enterprise.tools.Sortings
import com.mytlogos.enterprise.viewmodel.EpisodeViewModel
import org.joda.time.DateTime

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
    fun getDependantGenerator(loadedData: LoadData): DependantGenerator
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
    fun getLiveListItems(listId: Int): LiveData<List<Int>>
    fun getExternalListItems(externalListId: Int): Collection<Int>
    fun getLiveExternalListItems(externalListId: Int): LiveData<List<Int>>
    fun getDownloadableEpisodes(mediumId: Int, limit: Int): List<Int>
    fun getDownloadableEpisodes(mediaIds: Collection<Int>): List<Int>
    fun getDisplayEpisodes(filter: EpisodeViewModel.Filter): LiveData<PagedList<DisplayRelease>>
    fun getDisplayEpisodesGrouped(saved: Int, medium: Int): LiveData<PagedList<DisplayEpisode>>
    fun getLists(): LiveData<List<MediaList>>
    fun insertDanglingMedia(mediaIds: MutableCollection<Int>)
    fun removeDanglingMedia(mediaIds: Collection<Int>)
    fun getListSetting(id: Int, isExternal: Boolean): LiveData<out MediaListSetting?>
    fun getListSettingNow(id: Int, isExternal: Boolean): MediaListSetting?
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
    fun getMediumSettingsNow(mediumId: Int): MediumSetting?
    fun getToc(
        mediumId: Int,
        sortings: Sortings,
        read: Byte,
        saved: Byte
    ): LiveData<PagedList<TocEpisode>>

    fun getMediumItems(listId: Int, isExternal: Boolean): LiveData<List<MediumItem>>
    fun listExists(listName: String): Boolean
    fun countSavedEpisodes(mediumId: Int): Int
    fun getSavedEpisodes(mediumId: Int): List<Int>
    fun getEpisode(episodeId: Int): Episode
    fun getSimpleEpisodes(ids: Collection<Int>): List<SimpleEpisode>
    fun updateProgress(episodeIds: Collection<Int>, progress: Float)
    fun getMediaInWaitBy(
        filter: String,
        mediumFilter: Int,
        hostFilter: String,
        sortings: Sortings
    ): LiveData<PagedList<MediumInWait>>

    fun getReadTodayEpisodes(): LiveData<PagedList<ReadEpisode>>
    fun getInternLists(): LiveData<List<MediaList>>
    fun addItemsToList(listId: Int, ids: Collection<Int>)
    fun getSimilarMediaInWait(mediumInWait: MediumInWait): LiveData<List<MediumInWait>>
    fun getMediaSuggestions(title: String, medium: Int): LiveData<List<SimpleMedium>>
    fun getMediaInWaitSuggestions(title: String, medium: Int): LiveData<List<MediumInWait>>
    fun getListSuggestion(name: String): LiveData<List<MediaList>>
    fun onDownloadAble(): LiveData<Boolean>
    fun clearMediaInWait()
    fun deleteMediaInWait(toDelete: Collection<MediumInWait>)
    fun getAllDanglingMedia(): LiveData<List<MediumItem>>
    fun removeItemFromList(listId: Int, mediumId: Int)
    fun removeItemFromList(listId: Int, mediumId: Collection<Int>)
    fun moveItemsToList(oldListId: Int, newListId: Int, ids: Collection<Int>)
    fun getExternalUser(): LiveData<PagedList<ExternalUser>>
    fun getSpaceMedium(mediumId: Int): SpaceMedium
    fun getMediumType(mediumId: Int): Int
    fun getReleaseLinks(episodeId: Int): List<String>
    fun clearLocalMediaData()
    fun getNotifications(): LiveData<PagedList<NotificationItem>>
    fun updateFailedDownload(episodeId: Int)
    fun getFailedEpisodes(episodeIds: Collection<Int>): List<FailedEpisode>
    fun addNotification(notification: NotificationItem)
    fun getSimpleEpisode(episodeId: Int): SimpleEpisode
    fun getSimpleMedium(mediumId: Int): SimpleMedium
    fun clearNotifications()
    fun clearFailEpisodes()
    fun getAllEpisodes(mediumId: Int): Collection<Int>
    fun syncProgress()
    fun updateDataStructure(mediaIds: List<Int>, partIds: List<Int>)
    fun getEpisodeIdsWithHigherIndex(combiIndex: Double, mediumId: Int, read: Boolean): List<Int>
    fun getEpisodeIdsWithHigherIndex(combiIndex: Double, mediumId: Int): List<Int>
    fun getEpisodeIdsWithLowerIndex(combiIndex: Double, mediumId: Int, read: Boolean): List<Int>
    fun getEpisodeIdsWithLowerIndex(combiIndex: Double, mediumId: Int): List<Int>
    fun getSavedEpisodeIdsWithHigherIndex(combiIndex: Double, mediumId: Int): Collection<Int>
    fun getSavedEpisodeIdsWithLowerIndex(combiIndex: Double, mediumId: Int): Collection<Int>
    fun removeEpisodes(episodeIds: List<Int>)
    fun removeParts(partIds: Collection<Int>)
    fun getReadEpisodes(episodeIds: Collection<Int>, read: Boolean): List<Int>
    fun insertEditEvent(event: EditEvent)
    fun insertEditEvent(events: Collection<EditEvent>)
    fun getEditEvents(): MutableList<out EditEvent>
    fun removeEditEvents(editEvents: Collection<EditEvent>)
    fun checkReload(parsedStat: ParsedStat): ReloadStat
}