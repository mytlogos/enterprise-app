package com.mytlogos.enterprise.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mytlogos.enterprise.background.repository.MediaListRepository
import com.mytlogos.enterprise.background.repository.MediumInWaitRepository
import com.mytlogos.enterprise.model.MediaList
import com.mytlogos.enterprise.model.MediumInWait
import com.mytlogos.enterprise.model.SimpleMedium

class MediumInWaitViewModel(application: Application) : FilterableViewModel(application) {
    private val mediumTitleFilterLiveData = MutableLiveData<String>()
    private val mediumInWaitTitleFilterLiveData = MutableLiveData<String>()
    private val listNameFilterLiveData = MutableLiveData<String>()
    private val mediumInWaitRepository by lazy { MediumInWaitRepository.getInstance(application) }
    private val mediaListRepository by lazy { MediaListRepository.getInstance(application) }

    override fun resetFilter() {}

    fun getInternalLists(): LiveData<MutableList<MediaList>> {
        return mediaListRepository.internLists
    }

    fun getSimilarMediaInWait(mediumInWait: MediumInWait): LiveData<MutableList<MediumInWait>> {
        return mediumInWaitRepository.getSimilarMediaInWait(mediumInWait)
    }

    fun getMediumSuggestions(medium: Int): LiveData<MutableList<SimpleMedium>> {
        return Transformations.switchMap(
            mediumTitleFilterLiveData
        ) { input: String -> mediumInWaitRepository.getMediaSuggestions(input, medium) }
    }

    fun getMediumInWaitSuggestions(medium: Int): LiveData<MutableList<MediumInWait>> {
        return Transformations.switchMap(
            mediumInWaitTitleFilterLiveData
        ) { input: String -> mediumInWaitRepository.getMediaInWaitSuggestions(input, medium) }
    }

    fun setMediumInWaitTitleFilter(titleFilter: String) {
        mediumInWaitTitleFilterLiveData.value = processStringFilter(titleFilter)
    }

    fun setMediumTitleFilter(titleFilter: String) {
        mediumTitleFilterLiveData.value = processStringFilter(titleFilter)
    }

    fun setListNameFilter(filter: String) {
        listNameFilterLiveData.value = processStringFilter(filter)
    }

    suspend fun consumeMediumInWait(
        selectedMedium: SimpleMedium,
        mediumInWaits: List<MediumInWait>,
    ): Boolean {
        return mediumInWaitRepository.consumeMediumInWait(selectedMedium, mediumInWaits)
    }

    suspend fun createMedium(
        mediumInWait: MediumInWait,
        mediumInWaits: List<MediumInWait>,
        list: MediaList,
    ): Boolean {
        return mediumInWaitRepository.createMedium(mediumInWait, mediumInWaits, list)
    }

    val listSuggestion: LiveData<MutableList<MediaList>>
        get() = Transformations.switchMap(listNameFilterLiveData) { name: String ->
            repository.getListSuggestion(name)
        }
}