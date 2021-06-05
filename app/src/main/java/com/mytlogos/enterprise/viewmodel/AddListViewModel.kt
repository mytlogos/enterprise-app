package com.mytlogos.enterprise.viewmodel

import android.app.Application
import com.mytlogos.enterprise.model.MediaList
import java.io.IOException

class AddListViewModel(application: Application) : RepoViewModel(application) {
    @Throws(IOException::class)
    fun addList(list: MediaList?, autoDownload: Boolean) {
        repository.addList(list!!, autoDownload)
    }

    fun exists(listName: String?): Boolean {
        return repository.listExists(listName!!)
    }
}