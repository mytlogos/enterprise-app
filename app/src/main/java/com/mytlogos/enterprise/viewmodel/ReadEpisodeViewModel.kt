package com.mytlogos.enterprise.viewmodel

import android.app.Application
import androidx.paging.PagingData
import com.mytlogos.enterprise.model.ReadEpisode
import kotlinx.coroutines.flow.Flow

class ReadEpisodeViewModel(application: Application) : RepoViewModel(application) {
    fun getReadEpisodes(): Flow<PagingData<ReadEpisode>> {
        return repository.readTodayEpisodes
    }
}