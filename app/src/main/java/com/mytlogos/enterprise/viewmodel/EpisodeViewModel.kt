package com.mytlogos.enterprise.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.google.gson.Gson
import com.mytlogos.enterprise.model.*
import com.mytlogos.enterprise.preferences.UserPreferences
import com.mytlogos.enterprise.preferences.UserPreferences.Companion.episodesFilter
import java.util.*

class EpisodeViewModel(application: Application) : RepoViewModel(application),
    MediumFilterableViewModel {
    private var episodes: LiveData<PagedList<DisplayRelease>>? = null
    private var listLiveData: LiveData<MutableList<MediaList>>? = null
    private val filter = MutableLiveData<Filter>()
    val displayEpisodes: LiveData<PagedList<DisplayRelease>>
        get() {
            if (episodes == null) {
                episodes = Transformations.switchMap(filter) { input: Filter ->
                    println("filtering episodes after: $input")
                    repository.getDisplayEpisodes(input)
                }
            }
            return episodes!!
        }

    fun setGrouped(grouped: Boolean) {
        filter.value = FilterBuilder(filter.value).setGrouped(grouped).build()
    }

    override var mediumFilter: Int
        get() {
            val value = filter.value
            return value?.medium ?: 0
        }
        set(filter) {
            this.filter.value = FilterBuilder(this.filter.value).setMedium(filter).build()
        }
    var saved: Int
        get() {
            val value = filter.value
            return value?.saved ?: -1
        }
        set(saved) {
            filter.value = FilterBuilder(filter.value).setSaved(saved).build()
        }
    var read: Int
        get() {
            val value = filter.value
            return value?.read ?: -1
        }
        set(read) {
            filter.value = FilterBuilder(filter.value).setRead(read).build()
        }
    var maxIndex: Int
        get() {
            val value = filter.value
            return value?.maxIndex ?: -1
        }
        set(maxIndex) {
            filter.value = FilterBuilder(filter.value).setMaxIndex(maxIndex).build()
        }
    var minIndex: Int
        get() {
            val value = filter.value
            return value?.minIndex ?: -1
        }
        set(minIndex) {
            filter.value = FilterBuilder(filter.value).setMinIndex(minIndex).build()
        }
    var host: String
        get() {
            val value = filter.value
            return value?.host ?: ""
        }
        set(host) {
            filter.value = FilterBuilder(filter.value).setHost(host).build()
        }
    var isLatestOnly: Boolean
        get() {
            val value = filter.value
            return value != null && value.latestOnly
        }
        set(latestOnly) {
            filter.value = FilterBuilder(filter.value).setLatestOnly(latestOnly).build()
        }
    var filterListIds: List<Int>
        get() {
            val value = filter.value
            return if (value != null) ArrayList(value.filterListIds) else emptyList()
        }
        set(filterListIds) {
            filter.value = FilterBuilder(filter.value).setFilterListIds(filterListIds).build()
        }
    val lists: LiveData<MutableList<MediaList>>
        get() {
            if (listLiveData == null) {
                listLiveData = repository.internLists
            }
            return listLiveData!!
        }

    private class FilterBuilder(filter: Filter?) {
        private var grouped: Boolean
        private var medium: Int
        private var saved: Int
        private var read: Int
        private var minIndex: Int
        private var maxIndex: Int
        private var host: String?
        private var latestOnly: Boolean
        private var filterListIds: List<Int>

        fun setGrouped(grouped: Boolean): FilterBuilder {
            this.grouped = grouped
            return this
        }

        fun setMedium(medium: Int): FilterBuilder {
            this.medium = medium
            return this
        }

        fun setSaved(saved: Int): FilterBuilder {
            this.saved = saved
            return this
        }

        fun setRead(read: Int): FilterBuilder {
            this.read = read
            return this
        }

        fun setMinIndex(minIndex: Int): FilterBuilder {
            this.minIndex = minIndex
            return this
        }

        fun setMaxIndex(maxIndex: Int): FilterBuilder {
            this.maxIndex = maxIndex
            return this
        }

        fun setHost(host: String?): FilterBuilder {
            this.host = host
            return this
        }

        fun setLatestOnly(latestOnly: Boolean): FilterBuilder {
            this.latestOnly = latestOnly
            return this
        }

        fun setFilterListIds(filterListIds: List<Int>): FilterBuilder {
            this.filterListIds = filterListIds
            return this
        }

        fun build(): Filter {
            return Filter(grouped,
                medium,
                saved,
                read,
                minIndex,
                maxIndex,
                host,
                latestOnly,
                filterListIds)
        }

        init {
            var filter = filter
            if (filter == null) {
                filter = Filter()
            }
            grouped = filter.grouped
            saved = filter.saved
            medium = filter.medium
            read = filter.read
            maxIndex = filter.maxIndex
            minIndex = filter.minIndex
            host = filter.host
            latestOnly = filter.latestOnly
            filterListIds = filter.filterListIds
        }
    }

    class Filter(
        val grouped: Boolean = false,
        val medium: Int = 0,
        saved: Int = -1,
        read: Int = -1,
        minIndex: Int = -1,
        maxIndex: Int = -1,
        host: String? = null,
        latestOnly: Boolean = false,
        filterListIds: List<Int>? = emptyList()
    ) {
        val saved: Int
        val read: Int
        val minIndex: Int
        val maxIndex: Int
        val host: String?
        val latestOnly: Boolean
        val filterListIds: List<Int>
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val filter = other as Filter
            if (grouped != filter.grouped) return false
            if (medium != filter.medium) return false
            if (saved != filter.saved) return false
            if (read != filter.read) return false
            if (minIndex != filter.minIndex) return false
            if (maxIndex != filter.maxIndex) return false
            if (latestOnly != filter.latestOnly) return false
            return if (host != filter.host) false else filterListIds == filter.filterListIds
        }

        override fun hashCode(): Int {
            var result = if (grouped) 1 else 0
            result = 31 * result + medium
            result = 31 * result + saved
            result = 31 * result + read
            result = 31 * result + minIndex
            result = 31 * result + maxIndex
            result = 31 * result + (host?.hashCode() ?: 0)
            result = 31 * result + if (latestOnly) 1 else 0
            result = 31 * result + filterListIds.hashCode()
            return result
        }

        override fun toString(): String {
            return "Filter{" +
                    "grouped=" + grouped +
                    ", medium=" + medium +
                    ", saved=" + saved +
                    ", read=" + read +
                    ", minIndex=" + minIndex +
                    ", maxIndex=" + maxIndex +
                    ", host='" + host + '\'' +
                    ", latestOnly=" + latestOnly +
                    ", filterListIds=" + filterListIds +
                    '}'
        }

        init {
            this.saved = if (saved > 0) 1 else saved
            this.read = if (read > 0) 1 else read
            this.minIndex = minIndex
            this.maxIndex = maxIndex
            this.host = host
            this.latestOnly = latestOnly
            this.filterListIds = Collections.unmodifiableList(filterListIds)
        }
    }

    init {
        val episodesFilter = episodesFilter
        val filter = Gson().fromJson(episodesFilter, Filter::class.java)
        this.filter.value = filter ?: Filter()
        this.filter.observeForever { newFilter: Filter? ->
            val json = Gson().toJson(newFilter)
            UserPreferences.episodesFilter = json
        }
    }
}