package com.mytlogos.enterprise.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.paging.PagingData
import com.mytlogos.enterprise.background.repository.EpisodeRepository
import com.mytlogos.enterprise.model.TocEpisode
import com.mytlogos.enterprise.tools.Sortings
import com.mytlogos.enterprise.ui.ActionCount
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import java.io.IOException
import kotlin.math.max
import kotlin.math.min

@Suppress("BlockingMethodInNonBlockingContext")
class TocEpisodeViewModel(application: Application) :
    FilterableViewModel(application),
    SortableViewModel {
    private val sortFilterLiveData = MutableLiveData(SortFilter())
    private val mediumId: MutableStateFlow<Int> = MutableStateFlow(-1)
    private val episodeRepository: EpisodeRepository by lazy {
        EpisodeRepository.getInstance(application)
    }

    @ExperimentalCoroutinesApi
    val toc: Flow<PagingData<TocEpisode>> by lazy {
        mediumId.combine(sortFilterLiveData.asFlow()) { id: Int, filter: SortFilter ->
            episodeRepository.getToc(
                id,
                filter.sortings,
                filter.read,
                filter.saved
            )
        }.flatMapLatest { it }
    }

    fun setMediumId(mediumId: Int) {
        this.mediumId.value = mediumId
    }

    override fun resetFilter() {
        sortFilterLiveData.value = SortFilter()
    }

    fun getSort(): Sortings {
        val value = sortFilterLiveData.value
        return value?.sortings ?: Sortings.INDEX_DESC
    }

    override fun setSort(sort: Sortings) {
        val value = sortFilterLiveData.value
        sortFilterLiveData.value = value!!.copy(sortings = sort)
    }

    var savedFilter: Byte
        get() {
            val value = sortFilterLiveData.value
            return value?.saved ?: -1
        }
        set(savedFilter) {
            val value = sortFilterLiveData.value
            sortFilterLiveData.value = value!!.copy(saved = savedFilter)
        }
    var readFilter: Byte
        get() {
            val value = sortFilterLiveData.value
            return value?.read ?: -1
        }
        set(readFilter) {
            val value = sortFilterLiveData.value
            sortFilterLiveData.value = value!!.copy(read = readFilter)
        }

    @Throws(IOException::class)
    suspend fun deleteLocalEpisode(
        episodeIds: Set<Int>,
        combiIndices: List<Double>,
        count: ActionCount?,
        mediumId: Int,
    ) {
        when (count) {
            ActionCount.ALL -> episodeRepository.deleteAllLocalEpisodes(
                mediumId,
                getApplication()
            )
            ActionCount.CURRENT -> episodeRepository.deleteLocalEpisodes(
                episodeIds,
                mediumId,
                getApplication()
            )
            ActionCount.CURRENT_AND_ONWARDS -> {
                val lowest = getLowest(combiIndices)
                episodeRepository.deleteLocalEpisodesWithHigherIndex(
                    lowest,
                    mediumId,
                    getApplication()
                )
            }
            ActionCount.CURRENT_AND_PREVIOUSLY -> {
                val highest = getHighest(combiIndices)
                episodeRepository.deleteLocalEpisodesWithLowerIndex(
                    highest,
                    mediumId,
                    getApplication()
                )
            }
        }
    }

    @Throws(Exception::class)
    suspend fun updateRead(
        episodeIds: Set<Int>,
        combiIndices: List<Double>,
        count: ActionCount?,
        mediumId: Int,
        read: Boolean,
    ) {
        when (count) {
            ActionCount.ALL -> episodeRepository.updateAllRead(mediumId, read)
            ActionCount.CURRENT -> episodeRepository.updateRead(episodeIds, read)
            ActionCount.CURRENT_AND_ONWARDS -> {
                val lowest = getLowest(combiIndices)
                episodeRepository.updateReadWithHigherIndex(lowest, read, mediumId)
            }
            ActionCount.CURRENT_AND_PREVIOUSLY -> {
                val highest = getHighest(combiIndices)
                episodeRepository.updateReadWithLowerIndex(highest, read, mediumId)
            }
        }
    }

    @Throws(Exception::class)
    suspend fun reload(
        episodeIds: Set<Int>,
        combiIndices: List<Double>,
        count: ActionCount?,
        mediumId: Int,
    ) {
        when (count) {
            ActionCount.ALL -> episodeRepository.reloadAll(mediumId)
            ActionCount.CURRENT -> episodeRepository.reload(episodeIds)
            ActionCount.CURRENT_AND_ONWARDS -> {
                val lowest = getLowest(combiIndices)
                episodeRepository.reloadHigherIndex(lowest, mediumId)
            }
            ActionCount.CURRENT_AND_PREVIOUSLY -> {
                val highest = getHighest(combiIndices)
                episodeRepository.reloadLowerIndex(highest, mediumId)
            }
        }
    }

    suspend fun download(
        episodeIds: Set<Int>,
        combiIndices: List<Double>,
        count: ActionCount?,
        mediumId: Int,
    ) {
        when (count) {
            ActionCount.ALL -> episodeRepository.downloadAll(mediumId, getApplication())
            ActionCount.CURRENT -> episodeRepository.download(episodeIds,
                mediumId,
                getApplication())
            ActionCount.CURRENT_AND_ONWARDS -> {
                val lowest = getLowest(combiIndices)
                episodeRepository.downloadHigherIndex(lowest, mediumId, getApplication())
            }
            ActionCount.CURRENT_AND_PREVIOUSLY -> {
                val highest = getHighest(combiIndices)
                episodeRepository.downloadLowerIndex(highest, mediumId, getApplication())
            }
        }
    }

    private fun getLowest(combiIndices: List<Double>): Double {
        return combiIndices.fold(Int.MAX_VALUE.toDouble(), ::min)
    }

    private fun getHighest(combiIndices: List<Double>): Double {
        return combiIndices.fold(Int.MIN_VALUE.toDouble(), ::max)
    }

    private data class SortFilter(
        val sortings: Sortings = Sortings.INDEX_DESC,
        /**
         * -1 for ignore
         * 0 for false
         * 1 for true
         */
        val read: Byte = -1,
        /**
         * -1 for ignore
         * 0 for false
         * 1 for true
         */
        val saved: Byte = -1,
    )

    init {
        resetFilter()
    }
}