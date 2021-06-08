package com.mytlogos.enterprise.background.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.mytlogos.enterprise.background.RepositoryImpl
import com.mytlogos.enterprise.background.room.AbstractDatabase
import com.mytlogos.enterprise.model.DisplayRelease
import com.mytlogos.enterprise.model.MediaList
import com.mytlogos.enterprise.model.MediumItem
import com.mytlogos.enterprise.model.MediumSetting
import com.mytlogos.enterprise.tools.SingletonHolder
import com.mytlogos.enterprise.tools.Sortings
import com.mytlogos.enterprise.viewmodel.EpisodeViewModel
import com.mytlogos.enterprise.viewmodel.MediumViewModel
import kotlinx.coroutines.flow.Flow
import org.joda.time.DateTime

class MediumRepository private constructor(application: Application) {
    private val mediumDao = AbstractDatabase.getInstance(application).mediumDao()

    fun getAllMedia(
        sortings: Sortings,
        title: String?,
        medium: Int,
        author: String?,
        lastUpdate: DateTime?,
        minCountEpisodes: Int,
        minCountReadEpisodes: Int,
    ): Flow<PagingData<MediumItem>> {
        val sortValue = sortings.sortValue
        val query = if (sortValue > 0) {
            mediumDao::getAllAsc
        } else {
            mediumDao::getAllDesc
        }
        return Pager(
            PagingConfig(50),
            pagingSourceFactory = {
                query(
                    sortValue,
                    title,
                    medium,
                    author,
                    lastUpdate,
                    minCountEpisodes,
                    minCountReadEpisodes,
                )
            }
        ).flow
    }

    suspend fun updateMedium(settings: MediumSetting): String {
        return (RepositoryImpl.instance as RepositoryImpl).editService.updateMedium(settings)
    }

    companion object: SingletonHolder<MediumRepository, Application>(::MediumRepository)
}