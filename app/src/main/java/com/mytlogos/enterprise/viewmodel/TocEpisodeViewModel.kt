package com.mytlogos.enterprise.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.mytlogos.enterprise.model.TocEpisode
import com.mytlogos.enterprise.tools.Sortings
import com.mytlogos.enterprise.ui.ActionCount
import java.io.IOException

class TocEpisodeViewModel(application: Application?) : FilterableViewModel(application),
    SortableViewModel {
    private val sortFilterLiveData = MutableLiveData<SortFilter>()
    private var repositoryToc: LiveData<PagedList<TocEpisode>>? = null
    override fun resetFilter() {
        sortFilterLiveData.value =
            Builder(null).createSortFilter()
    }

    override fun setSort(sort: Sortings) {
        val value = sortFilterLiveData.value
        sortFilterLiveData.value =
            Builder(value).setSortings(sort)
                .createSortFilter()
    }

    fun getSort(): Sortings {
        val value = sortFilterLiveData.value
        return if (value == null) Sortings.INDEX_DESC else value.sortings!!
    }

    var savedFilter: Byte
        get() {
            val value = sortFilterLiveData.value
            return value?.saved ?: -1
        }
        set(savedFilter) {
            val value = sortFilterLiveData.value
            sortFilterLiveData.value =
                Builder(value).setSaved(savedFilter)
                    .createSortFilter()
        }
    var readFilter: Byte
        get() {
            val value = sortFilterLiveData.value
            return value?.read ?: -1
        }
        set(readFilter) {
            val value = sortFilterLiveData.value
            sortFilterLiveData.value =
                Builder(value).setRead(readFilter)
                    .createSortFilter()
        }

    fun getToc(mediumId: Int): LiveData<PagedList<TocEpisode>> {
        if (repositoryToc == null) {
            repositoryToc = Transformations.switchMap(
                sortFilterLiveData
            ) { input: SortFilter ->
                repository.getToc(mediumId,
                    input.sortings!!,
                    input.read,
                    input.saved)
            }
        }
        return repositoryToc!!
    }

    @Throws(IOException::class)
    fun deleteLocalEpisode(
        episodeIds: Set<Int>,
        combiIndices: List<Double>,
        count: ActionCount?,
        mediumId: Int
    ) {
        when (count) {
            ActionCount.ALL -> repository.deleteAllLocalEpisodes(mediumId, getApplication())
            ActionCount.CURRENT -> repository.deleteLocalEpisodes(
                episodeIds,
                mediumId,
                getApplication()
            )
            ActionCount.CURRENT_AND_ONWARDS -> {
                val lowest = getLowest(combiIndices)
                repository.deleteLocalEpisodesWithHigherIndex(lowest, mediumId, getApplication())
            }
            ActionCount.CURRENT_AND_PREVIOUSLY -> {
                val highest = getHighest(combiIndices)
                repository.deleteLocalEpisodesWithLowerIndex(highest, mediumId, getApplication())
            }
        }
    }

    @Throws(Exception::class)
    fun updateRead(
        episodeIds: Set<Int>,
        combiIndices: List<Double>,
        count: ActionCount?,
        mediumId: Int,
        read: Boolean
    ) {
        when (count) {
            ActionCount.ALL -> repository.updateAllRead(mediumId, read)
            ActionCount.CURRENT -> repository.updateRead(episodeIds, read)
            ActionCount.CURRENT_AND_ONWARDS -> {
                val lowest = getLowest(combiIndices)
                repository.updateReadWithHigherIndex(lowest, read, mediumId)
            }
            ActionCount.CURRENT_AND_PREVIOUSLY -> {
                val highest = getHighest(combiIndices)
                repository.updateReadWithLowerIndex(highest, read, mediumId)
            }
        }
    }

    @Throws(Exception::class)
    fun reload(
        episodeIds: Set<Int>,
        combiIndices: List<Double>,
        count: ActionCount?,
        mediumId: Int
    ) {
        when (count) {
            ActionCount.ALL -> repository.reloadAll(mediumId)
            ActionCount.CURRENT -> repository.reload(episodeIds)
            ActionCount.CURRENT_AND_ONWARDS -> {
                val lowest = getLowest(combiIndices)
                repository.reloadHigherIndex(lowest, mediumId)
            }
            ActionCount.CURRENT_AND_PREVIOUSLY -> {
                val highest = getHighest(combiIndices)
                repository.reloadLowerIndex(highest, mediumId)
            }
        }
    }

    fun download(
        episodeIds: Set<Int>,
        combiIndices: List<Double>,
        count: ActionCount?,
        mediumId: Int
    ) {
        when (count) {
            ActionCount.ALL -> repository.downloadAll(mediumId, getApplication())
            ActionCount.CURRENT -> repository.download(episodeIds, mediumId, getApplication())
            ActionCount.CURRENT_AND_ONWARDS -> {
                val lowest = getLowest(combiIndices)
                repository.downloadHigherIndex(lowest, mediumId, getApplication())
            }
            ActionCount.CURRENT_AND_PREVIOUSLY -> {
                val highest = getHighest(combiIndices)
                repository.downloadLowerIndex(highest, mediumId, getApplication())
            }
        }
    }

    private fun getLowest(combiIndices: List<Double>): Double {
        var lowest = Int.MAX_VALUE.toDouble()
        for (index in combiIndices) {
            lowest = Math.min(index, lowest)
        }
        return lowest
    }

    private fun getHighest(combiIndices: List<Double>): Double {
        var highest = Int.MIN_VALUE.toDouble()
        for (index in combiIndices) {
            highest = Math.max(index, highest)
        }
        return highest
    }

    private class Builder(sortFilter: SortFilter?) {
        private var sortings: Sortings? = null
        private var read: Byte = 0
        private var saved: Byte = 0
        fun setSortings(sortings: Sortings?): Builder {
            this.sortings = sortings
            return this
        }

        fun setRead(read: Byte): Builder {
            var read = read
            if (read < -1) {
                read = -1
            } else if (read > 1) {
                read = 1
            }
            this.read = read
            return this
        }

        fun setSaved(saved: Byte): Builder {
            var saved = saved
            if (saved < -1) {
                saved = -1
            } else if (saved > 1) {
                saved = 1
            }
            this.saved = saved
            return this
        }

        fun createSortFilter(): SortFilter {
            return SortFilter(sortings, read, saved)
        }

        init {
            if (sortFilter == null) {
                sortings = Sortings.INDEX_DESC
                read = -1
                saved = -1
            } else {
                sortings = sortFilter.sortings
                read = sortFilter.read
                saved = sortFilter.saved
            }
        }
    }

    private class SortFilter(
        val sortings: Sortings?,
        /**
         * -1 for ignore
         * 0 for false
         * 1 for true
         */
        val read: Byte,
        /**
         * -1 for ignore
         * 0 for false
         * 1 for true
         */
        val saved: Byte
    )

    init {
        resetFilter()
    }
}