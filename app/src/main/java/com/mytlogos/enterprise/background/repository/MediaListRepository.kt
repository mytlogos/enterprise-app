package com.mytlogos.enterprise.background.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.mytlogos.enterprise.background.RepositoryImpl
import com.mytlogos.enterprise.background.RoomConverter
import com.mytlogos.enterprise.background.api.AndroidNetworkIdentificator
import com.mytlogos.enterprise.background.api.Client
import com.mytlogos.enterprise.background.api.model.ClientMinList
import com.mytlogos.enterprise.background.room.AbstractDatabase
import com.mytlogos.enterprise.background.room.model.RoomMediaList
import com.mytlogos.enterprise.model.MediaList
import com.mytlogos.enterprise.model.ToDownload
import com.mytlogos.enterprise.tools.SingletonHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException

@Suppress("BlockingMethodInNonBlockingContext")
class MediaListRepository private constructor(application: Application) {
    private val mediaListDao = AbstractDatabase.getInstance(application).mediaListDao()
    private val toDownloadDao = AbstractDatabase.getInstance(application).toDownloadDao()

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


    @Throws(IOException::class)
    suspend fun addList(list: MediaList, autoDownload: Boolean) {
        val mediaList = ClientMinList(
            list.name,
            list.medium
        )

        val clientMediaList = client.addList(mediaList).body()
            ?: throw IllegalArgumentException("adding list failed")

        RepositoryImpl.instance.getPersister().persist(clientMediaList)

        if (autoDownload) {
            val toDownload = ToDownload(
                false,
                null,
                clientMediaList.id,
                null
            )
            toDownloadDao.insert(RoomConverter().convert(toDownload))
        }
    }

    suspend fun listExists(listName: String): Boolean {
        return mediaListDao.listExists(listName)
    }

    companion object : SingletonHolder<MediaListRepository, Application>(::MediaListRepository)
}