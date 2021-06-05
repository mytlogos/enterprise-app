package com.mytlogos.enterprise.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.mytlogos.enterprise.model.ReadEpisode

class ReadEpisodeViewModel(application: Application) : RepoViewModel(application) {
    fun getReadEpisodes(): LiveData<PagedList<ReadEpisode>> {
        return repository.readTodayEpisodes
    }
}