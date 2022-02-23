package com.mytlogos.enterprise.background.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.mytlogos.enterprise.background.EditService
import com.mytlogos.enterprise.background.RepositoryImpl
import com.mytlogos.enterprise.background.api.AndroidNetworkIdentificator
import com.mytlogos.enterprise.background.api.Client
import com.mytlogos.enterprise.background.api.model.ClientMinList
import com.mytlogos.enterprise.background.room.AbstractDatabase
import com.mytlogos.enterprise.background.room.model.RoomExternListView
import com.mytlogos.enterprise.background.room.model.RoomMediaList
import com.mytlogos.enterprise.background.toRoom
import com.mytlogos.enterprise.model.ExternalMediaList
import com.mytlogos.enterprise.model.MediaList
import com.mytlogos.enterprise.model.MediaListSetting
import com.mytlogos.enterprise.model.ToDownload
import com.mytlogos.enterprise.tools.SingletonHolder
import java.io.IOException
import java.util.*

@Suppress("BlockingMethodInNonBlockingContext")
class MediaListRepository private constructor(application: Application) {
    private val mediaListDao = AbstractDatabase.getInstance(application).mediaListDao()
    private val externalMediaListDao = AbstractDatabase.getInstance(application).externalMediaListDao()
    private val toDownloadDao = AbstractDatabase.getInstance(application).toDownloadDao()
    private val danglingDao = AbstractDatabase.getInstance(application).roomDanglingDao()

    private val editService: EditService
        get() = (RepositoryImpl.instance as RepositoryImpl).editService

    val client: Client by lazy {
        Client.getInstance(AndroidNetworkIdentificator(application))
    }

    val lists: LiveData<MutableList<MediaList>>
        get() {
            val liveData = MediatorLiveData<MutableList<MediaList>>()
            liveData.addSource(mediaListDao.listViews) { mediaLists: List<MediaList>? ->
                val set = HashSet<MediaList>()
                if (liveData.value != null) {
                    set.addAll(liveData.value!!)
                }
                set.addAll(mediaLists!!)
                liveData.setValue(ArrayList(set))
            }
            liveData.addSource(externalMediaListDao.externalListViews) { roomMediaLists: List<RoomExternListView> ->
                val mediaLists: MutableList<MediaList> = ArrayList()
                for (list in roomMediaLists) {
                    val mediaList = list.mediaList
                    mediaLists.add(ExternalMediaList(
                        mediaList.uuid,
                        mediaList.externalListId,
                        mediaList.name,
                        mediaList.medium,
                        mediaList.url,
                        list.size
                    ))
                }
                val set = HashSet<MediaList>()
                if (liveData.value != null) {
                    set.addAll(liveData.value!!)
                }
                set.addAll(mediaLists)
                liveData.setValue(ArrayList(set))
            }
            return liveData
        }

    val internLists: LiveData<MutableList<MediaList>>
        get() = mediaListDao.listViews

    suspend fun removeItemFromList(
        listId: Int,
        mediumId: MutableCollection<Int>,
    ): Boolean {
        return editService.removeItemFromList(listId, mediumId)
    }

    suspend fun addMediumToList(
        listId: Int,
        ids: MutableCollection<Int>,
    ): Boolean {
        return editService.addMediumToList(listId, ids)
    }

    fun getListSetting(id: Int, isExternal: Boolean): LiveData<out MediaListSetting> {
        return if (isExternal) {
            externalMediaListDao.getExternalListSetting(id)
        } else mediaListDao.getListSettings(id)
    }

    suspend fun getListItems(listId: Int): Collection<Int> {
        return mediaListDao.getListItems(listId)
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
            toDownloadDao.insert(toDownload.toRoom())
        }
    }

    suspend fun listExists(listName: String): Boolean {
        return mediaListDao.listExists(listName)
    }

    suspend fun moveMediaToList(oldListId: Int, newListId: Int, ids: MutableCollection<Int>): Boolean {
        // to prevent duplicates
        val items = mediaListDao.getListItems(newListId)
        ids.removeAll(items.toSet())

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

    suspend fun updateListName(listSetting: MediaListSetting, newName: String): String {
        return editService.updateListName(listSetting, newName)
    }

    suspend fun updateListMedium(
        listSetting: MediaListSetting,
        newMediumType: Int,
    ): String {
        return editService.updateListMedium(listSetting, newMediumType)
    }

    companion object : SingletonHolder<MediaListRepository, Application>(::MediaListRepository)
}