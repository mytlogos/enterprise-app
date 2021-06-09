package com.mytlogos.enterprise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.mytlogos.enterprise.background.repository.MediaListRepository
import com.mytlogos.enterprise.background.repository.MediumRepository
import com.mytlogos.enterprise.model.MediumItem
import com.mytlogos.enterprise.tools.Sortings

class ListMediaViewModel(application: Application) : AndroidViewModel(application) {
    private var items: LiveData<MutableList<MediumItem>>? = null
    private val mediaListRepository by lazy { MediaListRepository.getInstance(application) }
    private val mediumRepository by lazy { MediumRepository.getInstance(application) }

    fun getMedia(listId: Int, isExternal: Boolean): LiveData<MutableList<MediumItem>> {
        if (items == null) {
            items = mediumRepository.getMediumItems(listId, isExternal)
        }
        return items!!
    }

    suspend fun removeMedia(listId: Int, mediumId: MutableCollection<Int>): Boolean {
        return mediaListRepository.removeItemFromList(listId, mediumId)
    }

    fun setSort(sortings: Sortings?) {}
}