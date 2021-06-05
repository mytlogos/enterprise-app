package com.mytlogos.enterprise.background.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.mytlogos.enterprise.background.room.AbstractDatabase
import com.mytlogos.enterprise.model.MediaList
import com.mytlogos.enterprise.tools.SingletonHolder

class MediaListRepository private constructor(application: Application) {
    private val mediaListDao = AbstractDatabase.getInstance(application).mediaListDao()

    val internLists: LiveData<MutableList<MediaList>>
        get() = mediaListDao.listViews


    companion object: SingletonHolder<MediaListRepository, Application>(::MediaListRepository)
}