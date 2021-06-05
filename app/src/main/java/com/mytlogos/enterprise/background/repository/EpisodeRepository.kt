package com.mytlogos.enterprise.background.repository

import android.app.Application
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.mytlogos.enterprise.background.room.AbstractDatabase
import com.mytlogos.enterprise.model.DisplayRelease
import com.mytlogos.enterprise.tools.SingletonHolder
import com.mytlogos.enterprise.viewmodel.EpisodeViewModel
import kotlinx.coroutines.flow.Flow

class EpisodeRepository private constructor(application: Application) {
    private val episodeDao = AbstractDatabase.getInstance(application).episodeDao()

    fun getDisplayEpisodes(filter: EpisodeViewModel.Filter): Flow<PagingData<DisplayRelease>> {
        val query = if (filter.latestOnly) {
            episodeDao::getDisplayEpisodesLatestOnlyPaging
        } else {
            episodeDao::getDisplayEpisodesPaging
        }
        return Pager(
            PagingConfig(50),
            pagingSourceFactory = {
                query(
                    filter.saved,
                    filter.read,
                    filter.medium,
                    filter.minIndex,
                    filter.maxIndex,
                    filter.filterListIds,
                    filter.filterListIds.isEmpty()
                )
            }
        ).flow
    }

    companion object : SingletonHolder<EpisodeRepository, Application>(::EpisodeRepository)
}