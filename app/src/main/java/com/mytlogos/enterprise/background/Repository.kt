package com.mytlogos.enterprise.background

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import com.mytlogos.enterprise.background.api.Client
import com.mytlogos.enterprise.background.api.model.ClientStat.ParsedStat
import com.mytlogos.enterprise.model.*
import kotlinx.coroutines.flow.Flow

interface Repository {
    val isClientOnline: Boolean
    val isClientAuthenticated: Boolean
    val homeStats: LiveData<HomeStats>
    val user: LiveData<User?>

    val isLoading: Boolean

    suspend fun getExternalListItems(externalListId: Int): Collection<Int>

    fun onDownloadable(): LiveData<Boolean>

    val externalUser: Flow<PagingData<ExternalUser>>

    suspend fun getSpaceMedium(mediumId: Int): SpaceMedium
    suspend fun getMediumType(mediumId: Int): Int
    suspend fun getSimpleMedium(mediumId: Int): SimpleMedium

    fun clearLocalMediaData(context: Context)

    suspend fun getSimpleEpisodes(ids: Collection<Int>): List<SimpleEpisode>
    val readTodayEpisodes: Flow<PagingData<ReadEpisode>>
    suspend fun getReleaseLinks(episodeId: Int): List<String>
    fun updateProgress(episodeId: Int, progress: Float)

    suspend fun syncProgress()

    fun getClient(): Client
    fun getPersister(): ClientModelPersister
    fun isMediumLoaded(mediumId: Int): Boolean
    fun isPartLoaded(partId: Int): Boolean
    fun isEpisodeLoaded(episodeId: Int): Boolean
    fun isExternalUserLoaded(uuid: String): Boolean
    suspend fun checkReload(stat: ParsedStat): ReloadStat
}