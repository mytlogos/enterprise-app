package com.mytlogos.enterprise.viewmodel

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.gson.Gson
import com.mytlogos.enterprise.background.repository.EpisodeRepository
import com.mytlogos.enterprise.background.repository.MediaListRepository
import com.mytlogos.enterprise.model.DisplayRelease
import com.mytlogos.enterprise.model.MediaList
import com.mytlogos.enterprise.preferences.UserPreferences.Companion.episodesFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import java.util.*

class EpisodeViewModel(
    application: Application,
) : AndroidViewModel(application),
    MediumFilterableViewModel {

    private val filter = MutableLiveData<Filter>()

    val internLists: LiveData<MutableList<MediaList>> by lazy {
        MediaListRepository.getInstance(application).internLists
    }

    @ExperimentalCoroutinesApi
    val displayEpisodes: Flow<PagingData<DisplayRelease>> by lazy {
        filter.asFlow().flatMapLatest { input: Filter ->
            println("filtering episodes after: $input")
            EpisodeRepository.getInstance(application).getDisplayEpisodes(input)
        }.cachedIn(viewModelScope)
    }

    init {
        // initialize the Filter LivaData from Preferences and save changes in Preferences
        val filter = Gson().fromJson(episodesFilter, Filter::class.java)
        this.filter.value = filter ?: Filter()
        this.filter.observeForever { newFilter: Filter ->
            val json = Gson().toJson(newFilter)
            episodesFilter = json
        }
    }

    private fun getFilter(): Filter {
        return filter.value!!
    }

    var grouped: Boolean
        get() = getFilter().grouped
        set(grouped) {
            filter.value = getFilter().copy(grouped = grouped)
        }

    override var mediumFilter: Int
        get() = getFilter().medium
        set(filter) {
            this.filter.value = getFilter().copy(medium = filter)
        }
    var saved: Int
        get() = getFilter().saved
        set(saved) {
            filter.value = getFilter().copy(saved = saved)
        }
    var read: Int
        get() = getFilter().read
        set(read) {
            filter.value = getFilter().copy(read = read)
        }
    var maxIndex: Int
        get() = getFilter().maxIndex
        set(maxIndex) {
            filter.value = getFilter().copy(maxIndex = maxIndex)
        }
    var minIndex: Int
        get() = getFilter().minIndex
        set(minIndex) {
            filter.value = getFilter().copy(minIndex = minIndex)
        }
    var host: String
        get() = getFilter().host ?: ""
        set(host) {
            filter.value = getFilter().copy(host = host.lowercase(Locale.getDefault()))
        }
    var isLatestOnly: Boolean
        get() = getFilter().latestOnly
        set(latestOnly) {
            filter.value = getFilter().copy(latestOnly = latestOnly)
        }
    var filterListIds: List<Int>
        get() = ArrayList(getFilter().filterListIds)
        set(filterListIds) {
            filter.value = getFilter().copy(filterListIds = filterListIds)
        }

    data class Filter(
        val grouped: Boolean = false,
        val medium: Int = 0,
        val saved: Int = -1,
        val read: Int = -1,
        val minIndex: Int = -1,
        val maxIndex: Int = -1,
        val host: String? = null,
        val latestOnly: Boolean = false,
        val filterListIds: List<Int> = emptyList(),
    )
}