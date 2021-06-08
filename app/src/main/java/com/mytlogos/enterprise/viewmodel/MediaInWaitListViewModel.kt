package com.mytlogos.enterprise.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.paging.PagingData
import com.mytlogos.enterprise.model.MediumInWait
import com.mytlogos.enterprise.tools.Sortings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import java.io.IOException
import kotlin.math.max

class MediaInWaitListViewModel(application: Application) :
    FilterableViewModel(application),
    SortableViewModel,
    MediumFilterableViewModel {

    @ExperimentalCoroutinesApi
    val mediaInWait: Flow<PagingData<MediumInWait>> by lazy {
        filterSortLiveData.asFlow().flatMapLatest {
            return@flatMapLatest repository.getMediaInWaitBy(
                it.titleFilter,
                it.mediumFilter,
                it.hostFilter,
                it.sortings
            )
        }
    }

    private val filterSortLiveData = MutableLiveData(FilterSort())

    @Throws(IOException::class)
    fun loadMediaInWait() {
        repository.loadMediaInWaitSync()
    }

    override fun setSort(sort: Sortings) {
        filterSortLiveData.value = getFilterSort().copy(sortings = sort)
    }

    private fun getFilterSort() = filterSortLiveData.value!!

    var titleFilter: String
        get() = getFilterSort().titleFilter ?: ""
        set(filter) {
            filterSortLiveData.value = getFilterSort().copy(titleFilter = processStringFilter(filter))
        }

    var hostFilter: String
        get() = getFilterSort().hostFilter ?: ""
        set(filter) {
            filterSortLiveData.value = getFilterSort().copy(hostFilter = processStringFilter(filter))
        }

    override var mediumFilter: Int
        get() = max(getFilterSort().mediumFilter, 0)
        set(filter) {
            filterSortLiveData.value = getFilterSort().copy(mediumFilter = filter)
        }

    override fun resetFilter() {
        val value = getFilterSort()
        filterSortLiveData.value = FilterSort(sortings = value.sortings)
    }

    private data class FilterSort(
        val sortings: Sortings = Sortings.TITLE_AZ,
        val mediumFilter: Int = 0,
        val titleFilter: String? = null,
        val hostFilter: String? = null,
    )
}