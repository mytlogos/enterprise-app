package com.mytlogos.enterprise.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mytlogos.enterprise.model.MediaList
import com.mytlogos.enterprise.model.MediumInWait
import com.mytlogos.enterprise.model.SimpleMedium
import java.util.concurrent.CompletableFuture

class MediumInWaitViewModel(application: Application?) : FilterableViewModel(application) {
    private val mediumTitleFilterLiveData = MutableLiveData<String?>()
    private val mediumInWaitTitleFilterLiveData = MutableLiveData<String?>()
    private val listNameFilterLiveData = MutableLiveData<String?>()
    override fun resetFilter() {}

    fun getInternalLists(): LiveData<MutableList<MediaList>> {
        return repository.internLists
    }

    fun getSimilarMediaInWait(mediumInWait: MediumInWait?): LiveData<MutableList<MediumInWait>> {
        return repository.getSimilarMediaInWait(mediumInWait!!)
    }

    fun getMediumSuggestions(medium: Int): LiveData<MutableList<SimpleMedium>> {
        return Transformations.switchMap(
            mediumTitleFilterLiveData
        ) { input: String? ->
            repository.getMediaSuggestions(
                input!!, medium)
        }
    }

    fun getMediumInWaitSuggestions(medium: Int): LiveData<MutableList<MediumInWait>> {
        return Transformations.switchMap(
            mediumInWaitTitleFilterLiveData
        ) { input: String? ->
            repository.getMediaInWaitSuggestions(
                input!!, medium)
        }
    }

    fun setMediumInWaitTitleFilter(titleFilter: String) {
        var titleFilter = titleFilter
        titleFilter = processStringFilter(titleFilter)
        mediumInWaitTitleFilterLiveData.value = titleFilter
    }

    fun setMediumTitleFilter(titleFilter: String) {
        var titleFilter = titleFilter
        titleFilter = processStringFilter(titleFilter)
        mediumTitleFilterLiveData.value = titleFilter
    }

    fun setListNameFilter(filter: String) {
        var filter = filter
        filter = processStringFilter(filter)
        listNameFilterLiveData.value = filter
    }

    fun consumeMediumInWait(
        selectedMedium: SimpleMedium,
        mediumInWaits: List<MediumInWait>,
    ): CompletableFuture<Boolean> {
        return repository.consumeMediumInWait(selectedMedium, mediumInWaits)
    }

    fun createMedium(
        mediumInWait: MediumInWait,
        mediumInWaits: List<MediumInWait>,
        list: MediaList?,
    ): CompletableFuture<Boolean> {
        return repository.createMedium(mediumInWait, mediumInWaits, list!!)
    }

    val listSuggestion: LiveData<MutableList<MediaList>>
        get() = Transformations.switchMap(listNameFilterLiveData) { name: String? ->
            repository.getListSuggestion(
                name!!)
        }
}