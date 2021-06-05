package com.mytlogos.enterprise.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.paging.PagingData
import com.mytlogos.enterprise.background.repository.MediumRepository
import com.mytlogos.enterprise.model.MediumItem
import com.mytlogos.enterprise.tools.Sortings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import org.joda.time.DateTime

class MediumViewModel(application: Application) : FilterableViewModel(application),
    SortableViewModel, MediumFilterableViewModel {

    private val sortFilterLiveData = MutableLiveData<SortFilter>()
    private val mediumRepository = MediumRepository.getInstance(application)

    init {
        sortFilterLiveData.value = SortFilterBuilder(null).createSortFilter()
    }

    @ExperimentalCoroutinesApi
    val allMedia: Flow<PagingData<MediumItem>> by lazy {
        sortFilterLiveData.asFlow().flatMapLatest { input: SortFilter ->
            mediumRepository.getAllMedia(
                input.sortings!!,
                input.title,
                input.medium,
                input.author,
                input.lastUpdate,
                input.minCountEpisodes,
                input.minCountReadEpisodes
            )
        }
    }

    private fun getSortFilter() = sortFilterLiveData.value!!

    override fun resetFilter() {
        sortFilterLiveData.value = SortFilterBuilder(null).createSortFilter()
    }

    override fun setSort(sort: Sortings) {
        val value = getSortFilter()
        sortFilterLiveData.value = SortFilterBuilder(value).setSortings(sort).createSortFilter()
    }

    override var mediumFilter: Int
        get() {
            val value = getSortFilter()
            return if (value.medium < 0) 0 else value.medium
        }
        set(medium) {
            val value = getSortFilter()
            sortFilterLiveData.value = SortFilterBuilder(value).setMedium(medium).createSortFilter()
        }

    var minReadEpisodeFilter: Int
        get() {
            val value = getSortFilter()
            return value.minCountReadEpisodes
        }
        set(minReadEpisodeFilter) {
            val value = getSortFilter()
            sortFilterLiveData.value =
                SortFilterBuilder(value).setMinReadEpisodes(minReadEpisodeFilter).createSortFilter()
        }
    var minEpisodeFilter: Int
        get() {
            val value = getSortFilter()
            return value.minCountEpisodes
        }
        set(minEpisodeFilter) {
            val value = getSortFilter()
            sortFilterLiveData.value =
                SortFilterBuilder(value).setMinCountEpisodes(minEpisodeFilter).createSortFilter()
        }
    var titleFilter: String
        get() {
            val value = getSortFilter()
            return value.title ?: ""
        }
        set(titleFilter) {
            val value = getSortFilter()
            sortFilterLiveData.value =
                SortFilterBuilder(value).setTitle(processStringFilter(titleFilter))
                    .createSortFilter()
        }
    var authorFilter: String
        get() {
            val value = getSortFilter()
            return value.author ?: ""
        }
        set(authorFilter) {
            val value = getSortFilter()
            sortFilterLiveData.value =
                SortFilterBuilder(value).setAuthor(processStringFilter(authorFilter))
                    .createSortFilter()
        }

    fun setLastUpdateFilter(lastUpdateFilter: DateTime) {
        val value = getSortFilter()
        sortFilterLiveData.value =
            SortFilterBuilder(value).setLastUpdate(lastUpdateFilter).createSortFilter()
    }

    val lastUpdateFilter: DateTime?
        get() {
            val value = getSortFilter()
            return value.lastUpdate
        }

    private class SortFilterBuilder(filter: SortFilter?) {
        private var sortings: Sortings? = null
        private var medium = 0
        private var minReadEpisodes = 0
        private var minCountEpisodes = 0
        private var title: String? = null
        private var author: String? = null
        private var lastUpdate: DateTime? = null
        fun setSortings(sortings: Sortings): SortFilterBuilder {
            this.sortings = sortings
            return this
        }

        fun setMedium(medium: Int): SortFilterBuilder {
            this.medium = medium
            return this
        }

        fun setTitle(title: String?): SortFilterBuilder {
            this.title = title
            return this
        }

        fun setAuthor(author: String?): SortFilterBuilder {
            this.author = author
            return this
        }

        fun setMinReadEpisodes(minReadEpisodes: Int): SortFilterBuilder {
            this.minReadEpisodes = minReadEpisodes
            return this
        }

        fun setMinCountEpisodes(minCountEpisodes: Int): SortFilterBuilder {
            this.minCountEpisodes = minCountEpisodes
            return this
        }

        fun setLastUpdate(lastUpdate: DateTime): SortFilterBuilder {
            this.lastUpdate = lastUpdate
            return this
        }

        fun createSortFilter(): SortFilter {
            return SortFilter(sortings,
                medium,
                title,
                author,
                minReadEpisodes,
                minCountEpisodes,
                lastUpdate)
        }

        init {
            if (filter == null) {
                sortings = Sortings.TITLE_AZ
                minCountEpisodes = -1
                minReadEpisodes = -1
            } else {
                sortings = filter.sortings
                author = filter.author
                lastUpdate = filter.lastUpdate
                medium = filter.medium
                minCountEpisodes = filter.minCountEpisodes
                minReadEpisodes = filter.minCountReadEpisodes
                title = filter.title
            }
        }
    }

    private class SortFilter(
        val sortings: Sortings?,
        val medium: Int,
        val title: String?,
        val author: String?,
        val minCountReadEpisodes: Int,
        val minCountEpisodes: Int,
        val lastUpdate: DateTime?,
    )
}