package com.mytlogos.enterprise.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import com.mytlogos.enterprise.model.*
import com.mytlogos.enterprise.tools.Sortings
import java.util.concurrent.CompletableFuture

class ListMediaViewModel(application: Application) : RepoViewModel(application) {
    private var items: LiveData<MutableList<MediumItem>>? = null
    fun getMedia(listId: Int, isExternal: Boolean): LiveData<MutableList<MediumItem>> {
        if (items == null) {
            items = repository.getMediumItems(listId, isExternal)
        }
        return items!!
    }

    fun removeMedia(listId: Int, mediumId: Int): CompletableFuture<Boolean> {
        return repository.removeItemFromList(listId, mediumId)
    }

    fun removeMedia(listId: Int, mediumId: MutableCollection<Int>): CompletableFuture<Boolean> {
        return repository.removeItemFromList(listId, mediumId)
    }

    fun setSort(sortings: Sortings?) {}
}