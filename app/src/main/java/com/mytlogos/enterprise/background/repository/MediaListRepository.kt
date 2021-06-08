package com.mytlogos.enterprise.background.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.mytlogos.enterprise.background.*
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
import java.util.*

@Suppress("BlockingMethodInNonBlockingContext")
class MediaListRepository private constructor(application: Application) {
    private val mediaListDao = AbstractDatabase.getInstance(application).mediaListDao()
    private val externalMediaListDao = AbstractDatabase.getInstance(application).mediaListDao()
    private val toDownloadDao = AbstractDatabase.getInstance(application).toDownloadDao()
    private val danglingDao = AbstractDatabase.getInstance(application).roomDanglingDao()

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

    suspend fun removeItemFromList(listId: Int, mediumIds: MutableCollection<Int>): Boolean {
        val success = kotlin.runCatching {
            val response = client.deleteListMedia(listId, mediumIds)
            response.body()
        }.getOrNull()

        if (success != null && success) {
            mediaListDao.removeJoin(listId, mediumIds)

            val listMedia = mediaListDao.getAllLinkedMedia()
            val externalListMedia = externalMediaListDao.getAllLinkedMedia()

            mediumIds.removeAll(listMedia)
            mediumIds.removeAll(externalListMedia)

            if (mediumIds.isNotEmpty()) {
                val converter = RoomConverter()
                danglingDao.insertBulk(converter.convertToDangling(mediumIds))
            }
            return true
        }
        return false
    }

    suspend fun moveMediaToList(oldListId: Int, newListId: Int, ids: MutableCollection<Int>): Boolean {
        // to prevent duplicates
        val items = mediaListDao.getListItems(newListId)
        ids.removeAll(items)

        // adding nothing cannot fail
        if (ids.isEmpty()) {
            return true
        }
        val successMove: MutableCollection<Int> = ArrayList()

        try {
            for (id in ids) {
                val response = client.updateListMedia(oldListId, newListId, id)
                val success = response.body()

                if (success != null && success) {
                    successMove.add(id)
                }
            }
            val oldJoins: MutableCollection<RoomMediaList.MediaListMediaJoin> = ArrayList()
            val newJoins: MutableCollection<RoomMediaList.MediaListMediaJoin> = ArrayList()

            for (id in ids) {
                oldJoins.add(RoomMediaList.MediaListMediaJoin(oldListId, id))
                newJoins.add(RoomMediaList.MediaListMediaJoin(newListId, id))
            }
            mediaListDao.moveJoins(oldJoins, newJoins)

            // return true if at least one succeeded?
            return successMove.isNotEmpty()
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }


    companion object : SingletonHolder<MediaListRepository, Application>(::MediaListRepository)
}