package com.mytlogos.enterprise.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import com.mytlogos.enterprise.background.TaskManager.Companion.runTask
import com.mytlogos.enterprise.background.repository.MediaListRepository
import com.mytlogos.enterprise.background.repository.MediumRepository
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

    private val mediaListRepository by lazy { MediaListRepository.getInstance(application) }
    private val mediumRepository by lazy { MediumRepository.getInstance(application) }

    fun getListSettings(id: Int, isExternal: Boolean): LiveData<out MediaListSetting?> {
        if (listSettings == null) {
            listSettings = repository.getListSettings(id, isExternal)
        }
        return listSettings!!
    }

    suspend fun updateListName(listSetting: MediaListSetting, text: String): String {
        return repository.updateListName(listSetting, text)
    }

    suspend fun updateListMedium(
        listSetting: MediaListSetting?,
        newMediumType: Int
    ): String {
        return repository.updateListMedium(listSetting!!, newMediumType)
    }

    fun updateToDownload(add: Boolean, toDownload: ToDownload?) {
        runTask { repository.updateToDownload(add, toDownload!!) }
    }

    fun getMediumSettings(mediumId: Int): LiveData<MediumSetting> {
        if (settings == null) {
            settings = repository.getMediumSettings(mediumId)
        }
        return settings!!
    }

    suspend fun updateMedium(mediumSettings: MediumSetting): String {
        return mediumRepository.updateMedium(mediumSettings)
    }

    suspend fun moveMediumToList(
        oldListId: Int,
        newListId: Int,
        ids: MutableCollection<Int>,
    ): Boolean {
        return mediaListRepository.moveMediaToList(oldListId, newListId, ids)
    }

    suspend fun addMediumToList(listId: Int, ids: MutableCollection<Int>): Boolean {
        return MediaListRepository.getInstance(getApplication()).addMediumToList(listId, ids)
    }
}