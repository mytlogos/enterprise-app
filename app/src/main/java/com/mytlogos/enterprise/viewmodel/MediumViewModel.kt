package com.mytlogos.enterprise.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.mytlogos.enterprise.model.MediumItem
import com.mytlogos.enterprise.tools.Sortings
import org.joda.time.DateTime

class MediumViewModel(application: Application) : FilterableViewModel(application),
    SortableViewModel, MediumFilterableViewModel {
    var allMedia: LiveData<PagedList<MediumItem>>? = null
        get() {
            if (field == null) {
                field = Transformations.switchMap(sortFilterLiveData) { input: SortFilter ->
                    repository.getAllMedia(
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
            return field
        }
        private set
    private val sortFilterLiveData = MutableLiveData<SortFilter>()
    override fun resetFilter() {
        sortFilterLiveData.value = SortFilterBuilder(null).createSortFilter()
    }

    override fun setSort(sort: Sortings) {
        val value = sortFilterLiveData.value
        sortFilterLiveData.value = SortFilterBuilder(value).setSortings(sort).createSortFilter()
    }

    override var mediumFilter: Int
        get() {
            val value = sortFilterLiveData.value
            return if (value == null) 0 else if (value.medium < 0) 0 else value.medium
        }
        set(medium) {
            val value = sortFilterLiveData.value
            sortFilterLiveData.value = SortFilterBuilder(value).setMedium(medium).createSortFilter()
        }
    var minReadEpisodeFilter: Int
        get() {
            val value = sortFilterLiveData.value
            return value?.minCountReadEpisodes ?: -1
        }
        set(minReadEpisodeFilter) {
            val value = sortFilterLiveData.value
            sortFilterLiveData.value =
                SortFilterBuilder(value).setMinReadEpisodes(minReadEpisodeFilter).createSortFilter()
        }
    var minEpisodeFilter: Int
        get() {
            val value = sortFilterLiveData.value
            return value?.minCountEpisodes ?: -1
        }
        set(minEpisodeFilter) {
            val value = sortFilterLiveData.value
            sortFilterLiveData.value =
                SortFilterBuilder(value).setMinCountEpisodes(minEpisodeFilter).createSortFilter()
        }
    var titleFilter: String
        get() {
            val value = sortFilterLiveData.value
            return if (value?.title == null) "" else value.title
        }
        set(titleFilter) {
            var titleFilter = titleFilter
            val value = sortFilterLiveData.value
            titleFilter = processStringFilter(titleFilter)
            sortFilterLiveData.value =
                SortFilterBuilder(value).setTitle(titleFilter).createSortFilter()
        }
    var authorFilter: String
        get() {
            val value = sortFilterLiveData.value
            return if (value?.author == null) "" else value.author
        }
        set(titleFilter) {
            var titleFilter = titleFilter
            val value = sortFilterLiveData.value
            titleFilter = processStringFilter(titleFilter)
            sortFilterLiveData.value =
                SortFilterBuilder(value).setAuthor(titleFilter).createSortFilter()
        }

    fun setLastUpdateFilter(lastUpdateFilter: DateTime) {
        val value = sortFilterLiveData.value
        sortFilterLiveData.value =
            SortFilterBuilder(value).setLastUpdate(lastUpdateFilter).createSortFilter()
    }

    val lastUpdateFilter: DateTime?
        get() {
            val value = sortFilterLiveData.value
            return value?.lastUpdate
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
        val lastUpdate: DateTime?
    )

    init {
        sortFilterLiveData.value = SortFilterBuilder(null).createSortFilter()
    }
}