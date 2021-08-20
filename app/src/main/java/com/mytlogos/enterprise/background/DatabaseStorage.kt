package com.mytlogos.enterprise.background

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import com.mytlogos.enterprise.background.api.model.ClientStat.ParsedStat
import com.mytlogos.enterprise.model.*
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
    suspend fun getUserNow(): User?
    fun getHomeStats(): LiveData<HomeStats>
    fun getPersister(repository: Repository, loadedData: LoadData): ClientModelPersister
    fun isLoading(): Boolean
    fun setLoading(loading: Boolean)
    suspend fun getLoadData(): LoadData
    suspend fun getListItems(listId: Int): Collection<Int>
    suspend fun getExternalListItems(externalListId: Int): Collection<Int>
    suspend fun insertDanglingMedia(mediaIds: MutableCollection<Int>)
    suspend fun getListSettingNow(id: Int, isExternal: Boolean): MediaListSetting?

    suspend fun getMediumSettingsNow(mediumId: Int): MediumSetting

    suspend fun getSimpleEpisodes(ids: Collection<Int>): List<SimpleEpisode>
    suspend fun updateProgress(episodeIds: Collection<Int>, progress: Float)

    fun getReadTodayEpisodes(): Flow<PagingData<ReadEpisode>>
    suspend fun addItemsToList(listId: Int, ids: Collection<Int>)
    fun getListSuggestion(name: String): LiveData<MutableList<MediaList>>
    fun onDownloadAble(): LiveData<Boolean>
    suspend fun removeItemFromList(listId: Int, mediumId: Collection<Int>)
    suspend fun moveItemsToList(oldListId: Int, newListId: Int, ids: Collection<Int>)
    fun getExternalUser(): Flow<PagingData<ExternalUser>>
    suspend fun getSpaceMedium(mediumId: Int): SpaceMedium
    suspend fun getMediumType(mediumId: Int): Int
    suspend fun getReleaseLinks(episodeId: Int): List<String>
    suspend fun clearLocalMediaData()
    suspend fun getSimpleMedium(mediumId: Int): SimpleMedium
    suspend fun syncProgress()
    suspend fun getReadEpisodes(episodeIds: Collection<Int>, read: Boolean): List<Int>
    suspend fun insertEditEvent(event: EditEvent)
    suspend fun insertEditEvent(events: Collection<EditEvent>)
    suspend fun getEditEvents(): MutableList<out EditEvent>
    suspend fun removeEditEvents(editEvents: Collection<EditEvent>)
    suspend fun checkReload(parsedStat: ParsedStat): ReloadStat
}