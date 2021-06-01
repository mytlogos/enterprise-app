package com.mytlogos.enterprise.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.mytlogos.enterprise.model.MediumInWait
import com.mytlogos.enterprise.tools.Sortings
import java.io.IOException

class MediaInWaitListViewModel(application: Application?) : FilterableViewModel(application),
    SortableViewModel, MediumFilterableViewModel {
    var mediaInWait: LiveData<PagedList<MediumInWait>>? = null
        get() {
            if (field == null) {
                field = Transformations.switchMap(
                    filterSortLiveData
                ) { input: FilterSort? ->
                    if (input == null) {
                        return@switchMap repository.getMediaInWaitBy(
                            null,
                            0,
                            null,
                            Sortings.TITLE_AZ
                        )
                    } else {
                        return@switchMap repository.getMediaInWaitBy(
                            input.titleFilter,
                            input.mediumFilter,
                            input.hostFilter,
                            input.sortings
                        )
                    }
                }
                filterSortLiveData.value = FilterSort()
            }
            return field
        }
        private set
    private val filterSortLiveData = MutableLiveData<FilterSort?>()
    @Throws(IOException::class)
    fun loadMediaInWait() {
        repository.loadMediaInWaitSync()
    }

    override fun setSort(sort: Sortings) {
        var value = filterSortLiveData.value
        value = if (value != null) {
            FilterSort(sort, value.mediumFilter, value.titleFilter, value.hostFilter)
        } else {
            FilterSort(sort, 0, null, null)
        }
        filterSortLiveData.value = value
    }

    var titleFilter: String
        get() {
            val value = filterSortLiveData.value
            return if (value == null) "" else value.titleFilter ?: ""
        }
        set(filter) {
            var filter = filter
            var value = filterSortLiveData.value
            filter = processStringFilter(filter)
            value = if (value != null) {
                FilterSort(value.sortings, value.mediumFilter, filter, value.hostFilter)
            } else {
                FilterSort(Sortings.TITLE_AZ, 0, filter, null)
            }
            filterSortLiveData.value = value
        }
    var hostFilter: String
        get() {
            val value = filterSortLiveData.value
            return if (value == null) "" else value.hostFilter ?: ""
        }
        set(filter) {
            var filter = filter
            var value = filterSortLiveData.value
            filter = processStringFilter(filter)
            value = if (value != null) {
                FilterSort(value.sortings, value.mediumFilter, value.titleFilter, filter)
            } else {
                FilterSort(Sortings.TITLE_AZ, 0, null, filter)
            }
            filterSortLiveData.value = value
        }
    override var mediumFilter: Int
        get() {
            val value = filterSortLiveData.value
            return if (value == null) 0 else if (value.mediumFilter < 0) 0 else value.mediumFilter
        }
        set(filter) {
            var value = filterSortLiveData.value
            value = if (value != null) {
                FilterSort(value.sortings, filter, value.titleFilter, value.hostFilter)
            } else {
                FilterSort(Sortings.TITLE_AZ, filter, null, null)
            }
            filterSortLiveData.value = value
        }

    override fun resetFilter() {
        val sort: FilterSort
        sort = if (filterSortLiveData.value != null) {
            FilterSort(filterSortLiveData.value!!.sortings)
        } else {
            FilterSort()
        }
        filterSortLiveData.value = sort
    }

    private class FilterSort(
        val sortings: Sortings,
        val mediumFilter: Int,
        val titleFilter: String?,
        val hostFilter: String?
    ) {
        @JvmOverloads
        constructor(sortings: Sortings = Sortings.TITLE_AZ) : this(sortings,
            0,
            null,
            null) {
        }
    }
}