package com.mytlogos.enterprise.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import com.mytlogos.enterprise.background.repository.MediaListRepository
import com.mytlogos.enterprise.model.*
import com.mytlogos.enterprise.tools.Sortings
import java.util.concurrent.CompletableFuture

class ListMediaViewModel(application: Application) : RepoViewModel(application) {
    private var items: LiveData<MutableList<MediumItem>>? = null
    private val mediaListRepository by lazy { MediaListRepository.getInstance(application) }

    fun getMedia(listId: Int, isExternal: Boolean): LiveData<MutableList<MediumItem>> {
        if (items == null) {
            items = repository.getMediumItems(listId, isExternal)
        }
        return items!!
    }

    fun removeMedia(listId: Int, mediumId: Int): CompletableFuture<Boolean> {
        return repository.removeItemFromList(listId, mediumId)
    }

    suspend fun removeMedia(listId: Int, mediumId: MutableCollection<Int>): Boolean {
        return mediaListRepository.removeItemFromList(listId, mediumId)
    }

    fun setSort(sortings: Sortings?) {}
}