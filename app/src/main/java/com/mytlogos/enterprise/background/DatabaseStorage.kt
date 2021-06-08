package com.mytlogos.enterprise.background

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.mytlogos.enterprise.background.api.model.ClientStat.ParsedStat
import com.mytlogos.enterprise.model.*

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
    suspend fun getUserNow(): User?
    fun getHomeStats(): LiveData<HomeStats>
    fun getPersister(repository: Repository, loadedData: LoadData): ClientModelPersister
    fun isLoading(): Boolean
    fun setLoading(loading: Boolean)
    fun getLoadData(): LoadData
    fun getListItems(listId: Int): Collection<Int>
    fun getExternalListItems(externalListId: Int): Collection<Int>
    fun insertDanglingMedia(mediaIds: MutableCollection<Int>)
    suspend fun getListSettingNow(id: Int, isExternal: Boolean): MediaListSetting?

    suspend fun getMediumSettingsNow(mediumId: Int): MediumSetting

    fun getSimpleEpisodes(ids: Collection<Int>): List<SimpleEpisode>
    suspend fun updateProgress(episodeIds: Collection<Int>, progress: Float)

    fun getReadTodayEpisodes(): LiveData<PagedList<ReadEpisode>>
    suspend fun addItemsToList(listId: Int, ids: Collection<Int>)
    fun getListSuggestion(name: String): LiveData<MutableList<MediaList>>
    fun onDownloadAble(): LiveData<Boolean>
    fun removeItemFromList(listId: Int, mediumId: Int)
    fun removeItemFromList(listId: Int, mediumId: Collection<Int>)
    suspend fun moveItemsToList(oldListId: Int, newListId: Int, ids: Collection<Int>)
    fun getExternalUser(): LiveData<PagedList<ExternalUser>>
    fun getSpaceMedium(mediumId: Int): SpaceMedium
    fun getMediumType(mediumId: Int): Int
    fun getReleaseLinks(episodeId: Int): List<String>
    fun clearLocalMediaData()
    fun getSimpleMedium(mediumId: Int): SimpleMedium
    fun syncProgress()
    fun getReadEpisodes(episodeIds: Collection<Int>, read: Boolean): List<Int>
    fun insertEditEvent(event: EditEvent)
    suspend fun insertEditEvent(events: Collection<EditEvent>)
    fun getEditEvents(): MutableList<out EditEvent>
    fun removeEditEvents(editEvents: Collection<EditEvent>)
    fun checkReload(parsedStat: ParsedStat): ReloadStat
}