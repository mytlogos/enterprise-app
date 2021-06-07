package com.mytlogos.enterprise.viewmodel

import android.app.Application
import com.mytlogos.enterprise.background.repository.EpisodeRepository
import com.mytlogos.enterprise.background.repository.MediaListRepository
import com.mytlogos.enterprise.model.MediaList
import java.io.IOException

@Suppress("BlockingMethodInNonBlockingContext")
class AddListViewModel(application: Application) : RepoViewModel(application) {
    private val listRepository: MediaListRepository by lazy {
        MediaListRepository.getInstance(application)
    }

    @Throws(IOException::class)
    suspend fun addList(list: MediaList, autoDownload: Boolean) {
        listRepository.addList(list, autoDownload)
    }

    suspend fun exists(listName: String): Boolean {
        return listRepository.listExists(listName)
    }
}