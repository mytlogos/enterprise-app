package com.mytlogos.enterprise.background

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.mytlogos.enterprise.background.api.Client
import com.mytlogos.enterprise.background.api.model.ClientStat.ParsedStat
import com.mytlogos.enterprise.model.*

interface Repository {
    val isClientOnline: Boolean
    val isClientAuthenticated: Boolean
    val homeStats: LiveData<HomeStats>
    val user: LiveData<User?>

    val isLoading: Boolean

    fun getExternalListItems(externalListId: Int): Collection<Int>

    suspend fun updateListName(listSetting: MediaListSetting, newName: String): String
    suspend fun updateListMedium(listSetting: MediaListSetting, newMediumType: Int): String

    fun getListSuggestion(name: String): LiveData<MutableList<MediaList>>

    fun onDownloadable(): LiveData<Boolean>

    val externalUser: LiveData<PagedList<ExternalUser>>

    fun getSpaceMedium(mediumId: Int): SpaceMedium
    fun getMediumType(mediumId: Int): Int
    fun getSimpleMedium(mediumId: Int): SimpleMedium

    fun clearLocalMediaData(context: Context)

    fun getSimpleEpisodes(ids: Collection<Int>): List<SimpleEpisode>
    val readTodayEpisodes: LiveData<PagedList<ReadEpisode>>
    fun getReleaseLinks(episodeId: Int): List<String>
    fun updateProgress(episodeId: Int, progress: Float)

    fun syncProgress()

    fun getClient(): Client
    fun getPersister(): ClientModelPersister
    fun isMediumLoaded(mediumId: Int): Boolean
    fun isPartLoaded(partId: Int): Boolean
    fun isEpisodeLoaded(episodeId: Int): Boolean
    fun isExternalUserLoaded(uuid: String): Boolean
    fun checkReload(stat: ParsedStat): ReloadStat
}