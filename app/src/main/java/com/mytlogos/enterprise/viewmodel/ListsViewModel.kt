package com.mytlogos.enterprise.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import com.mytlogos.enterprise.background.TaskManager.Companion.runTask
import com.mytlogos.enterprise.model.MediaList
import com.mytlogos.enterprise.model.MediaListSetting
import com.mytlogos.enterprise.model.MediumSetting
import com.mytlogos.enterprise.model.ToDownload
import java.util.concurrent.CompletableFuture

class ListsViewModel(application: Application) : RepoViewModel(application) {
    private var listSettings: LiveData<out MediaListSetting?>? = null
    private var settings: LiveData<MediumSetting>? = null
    val lists: LiveData<MutableList<MediaList>>
        get() = repository.lists
    val internLists: LiveData<MutableList<MediaList>>
        get() = repository.internLists

    fun getListSettings(id: Int, isExternal: Boolean): LiveData<out MediaListSetting?> {
        if (listSettings == null) {
            listSettings = repository.getListSettings(id, isExternal)
        }
        return listSettings!!
    }

    fun updateListName(listSetting: MediaListSetting?, text: String?): CompletableFuture<String> {
        return repository.updateListName(listSetting!!, text!!)
    }

    fun updateListMedium(
        listSetting: MediaListSetting?,
        newMediumType: Int
    ): CompletableFuture<String> {
        return repository.updateListMedium(listSetting!!, newMediumType)
    }

    fun updateToDownload(add: Boolean, toDownload: ToDownload?) {
        runTask { repository.updateToDownload(add, toDownload!!) }
    }

    fun getMediumSettings(mediumId: Int): LiveData<MediumSetting>? {
        if (settings == null) {
            settings = repository.getMediumSettings(mediumId)
        }
        return settings
    }

    fun updateMedium(mediumSettings: MediumSetting?): CompletableFuture<String> {
        return repository.updateMedium(mediumSettings!!)
    }

    fun moveMediumToList(
        oldListId: Int,
        newListId: Int,
        ids: MutableCollection<Int>,
    ): CompletableFuture<Boolean> {
        return repository.moveMediaToList(oldListId, newListId, ids)
    }

    fun addMediumToList(listId: Int, ids: MutableCollection<Int>): CompletableFuture<Boolean> {
        return repository.addMediumToList(listId, ids)
    }
}