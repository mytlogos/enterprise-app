package com.mytlogos.enterprise.background.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.mytlogos.enterprise.background.api.AndroidNetworkIdentificator
import com.mytlogos.enterprise.background.api.Client
import com.mytlogos.enterprise.background.room.AbstractDatabase
import com.mytlogos.enterprise.background.room.model.RoomMediaList
import com.mytlogos.enterprise.model.MediaList
import com.mytlogos.enterprise.tools.SingletonHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException

class MediaListRepository private constructor(application: Application) {
    private val mediaListDao = AbstractDatabase.getInstance(application).mediaListDao()

    val internLists: LiveData<MutableList<MediaList>>
        get() = mediaListDao.listViews

    val client: Client by lazy {
        Client.getInstance(AndroidNetworkIdentificator(application))
    }

    suspend fun addMediumToList(
        listId: Int,
        ids: MutableCollection<Int>,
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // to prevent duplicates
            val items = mediaListDao.getListItems(listId)
            ids.removeAll(items)

            // adding nothing cannot fail
            if (ids.isEmpty()) {
                return@withContext true
            }
            @Suppress("BlockingMethodInNonBlockingContext")
            val response: Response<Boolean> = client.addListMedia(listId, ids)

            if (response.body() == null || !response.body()!!) {
                return@withContext false
            }
            val joins = ids.map { RoomMediaList.MediaListMediaJoin(listId, it) }
            mediaListDao.addJoin(joins)
            return@withContext true
        } catch (e: IOException) {
            e.printStackTrace()
            return@withContext false
        }
    }

    companion object : SingletonHolder<MediaListRepository, Application>(::MediaListRepository)
}