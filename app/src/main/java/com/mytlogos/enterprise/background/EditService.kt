package com.mytlogos.enterprise.background

import android.annotation.SuppressLint
import androidx.annotation.IntDef
import androidx.collection.ArraySet
import com.mytlogos.enterprise.background.api.Client
import com.mytlogos.enterprise.background.api.NotConnectedException
import com.mytlogos.enterprise.background.api.model.*
import com.mytlogos.enterprise.model.ExternalMediaListSetting
import com.mytlogos.enterprise.model.MediaListSetting
import com.mytlogos.enterprise.model.MediumSetting
import com.mytlogos.enterprise.model.UpdateUser
import com.mytlogos.enterprise.tools.*
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import retrofit2.Response
import java.io.IOException
import kotlin.annotation.Retention
import kotlin.annotation.AnnotationRetention
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

@SuppressLint("UseSparseArrays")
internal class EditService(
    private val client: Client,
    private val storage: DatabaseStorage,
    private val persister: ClientModelPersister
) {
    @IntDef(value = [USER, EXTERNAL_LIST, EXTERNAL_USER, LIST, MEDIUM, PART, EPISODE, RELEASE, NEWS])
    @Retention(AnnotationRetention.SOURCE)
    internal annotation class EditObject

    @IntDef(value = [ADD, REMOVE, MOVE, ADD_TO, REMOVE_FROM, MERGE, CHANGE_NAME, CHANGE_TYPE, ADD_TOC, REMOVE_TOC, CHANGE_PROGRESS, CHANGE_READ])
    @Retention(
        AnnotationRetention.SOURCE
    )
    internal annotation class Event

    private val pushEditExecutor = Executors.newSingleThreadExecutor()
    private fun publishEditEvents() {
        val events = storage.getEditEvents()
        if (events.isEmpty()) {
            return
        }
        events.sortWith(Comparator.comparing { obj: EditEvent -> obj.dateTime })
        val objectTypeEventMap: MutableMap<Int, HashMap<Int, MutableList<EditEvent>>> =
            HashMap()
        for (event in events) {
            objectTypeEventMap
                .computeIfAbsent(
                    event.objectType
                ) { HashMap<Int, MutableList<EditEvent>>() }
                .computeIfAbsent(event.eventType) { ArrayList() }
                .add(event)
        }
        val consumedEvents: MutableCollection<EditEvent> = ArrayList()
        for ((key, value1) in objectTypeEventMap) {
            try {
                var consumed = true
                when (key) {
                    USER -> publishUserEvents(value1)
                    EXTERNAL_USER -> publishExternalUserEvents(value1)
                    EXTERNAL_LIST -> publishExternalListEvents(value1)
                    LIST -> publishListEvents(value1)
                    MEDIUM -> publishMediumEvents(value1)
                    PART -> publishPartEvents(value1)
                    EPISODE -> publishEpisodeEvents(value1)
                    RELEASE -> publishReleaseEvents(value1)
                    NEWS -> publishNewsEvents(value1)
                    else -> {
                        consumed = false
                        System.err.println("unknown event object type: $key")
                    }
                }
                if (consumed) {
                    for (value in value1.values) {
                        consumedEvents.addAll(value)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        storage.removeEditEvents(consumedEvents)
    }

    private fun publishUserEvents(typeEventsMap: Map<Int, List<EditEvent?>>) {
        println(typeEventsMap)
        // TODO: 26.09.2019 implement
    }

    private fun publishExternalUserEvents(typeEventsMap: Map<Int, List<EditEvent?>>) {
        println(typeEventsMap)
        // TODO: 26.09.2019 implement
    }

    private fun publishListEvents(typeEventsMap: Map<Int, List<EditEvent?>>) {
        println(typeEventsMap)
        // TODO: 26.09.2019 implement
    }

    private fun publishExternalListEvents(typeEventsMap: Map<Int, List<EditEvent?>>) {
        println(typeEventsMap)
        // TODO: 26.09.2019 implement
    }

    private fun publishMediumEvents(typeEventsMap: Map<Int, List<EditEvent?>>) {
        for ((_, value) in typeEventsMap) {

            // TODO: 26.09.2019 implement
        }
    }

    private fun publishPartEvents(typeEventsMap: Map<Int, List<EditEvent?>>) {
        println(typeEventsMap)
    }

    @Throws(NotConnectedException::class)
    private fun publishEpisodeEvents(typeEventsMap: Map<Int, List<EditEvent>>) {
        for ((key, value) in typeEventsMap) {
            if (key == CHANGE_PROGRESS) {
                publishEpisodeProgress(value)
            }
        }
        // TODO: 26.09.2019 implement
    }

    @Throws(NotConnectedException::class)
    private fun publishEpisodeProgress(value: List<EditEvent>) {
        val latestProgress: MutableMap<Int, EditEvent> = HashMap()
        val earliestProgress: MutableMap<Int, EditEvent> = HashMap()
        for (event in value) {
            latestProgress.merge(
                event.id,
                event
            ) { editEvent: EditEvent, editEvent2: EditEvent ->
                if (editEvent2.dateTime.isAfter(editEvent.dateTime)) {
                    return@merge editEvent2
                } else {
                    return@merge editEvent
                }
            }
            earliestProgress.merge(
                event.id,
                event
            ) { editEvent: EditEvent, editEvent2: EditEvent ->
                if (editEvent2.dateTime.isBefore(editEvent.dateTime)) {
                    return@merge editEvent2
                } else {
                    return@merge editEvent
                }
            }
        }
        val currentProgressEpisodeMap: MutableMap<Float, MutableSet<Int>> = HashMap()
        for ((key, value1) in latestProgress) {
            val newValue = value1.secondValue
            val newProgress = parseProgress(newValue)
            currentProgressEpisodeMap
                .computeIfAbsent(newProgress) { ArraySet() }
                .add(key)
        }
        for ((progress, ids) in currentProgressEpisodeMap) {
            try {
                if (!updateProgressOnline(progress, ids)) {
                    val progressMap: MutableMap<Float, MutableSet<Int>> = HashMap()
                    for (id in ids) {
                        val event = earliestProgress[id]
                            ?: throw IllegalStateException("expected a value, not null for: $id")
                        val idProgress = parseProgress(event.firstValue)
                        progressMap.computeIfAbsent(idProgress) { HashSet() }
                            .add(id)
                    }
                    progressMap.forEach { (updateProgress: Float, progressIds: Set<Int>) ->
                        storage.updateProgress(
                            progressIds,
                            updateProgress
                        )
                    }
                }
            } catch (e: NotConnectedException) {
                throw NotConnectedException(e)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun parseProgress(value: String?): Float {
        return try {
            value!!.toFloat()
        } catch (e: NumberFormatException) {
            if (java.lang.Boolean.parseBoolean(value)) 1.toFloat() else 0.toFloat()
        }
    }

    private fun publishReleaseEvents(typeEventsMap: Map<Int, List<EditEvent?>>) {
        println(typeEventsMap)
        // TODO: 26.09.2019 implement
    }

    private fun publishNewsEvents(typeEventsMap: Map<Int, List<EditEvent?>>) {
        println(typeEventsMap)
        // TODO: 26.09.2019 implement
    }

    fun updateUser(updateUser: UpdateUser) {
        TaskManager.runTask {
            val value = storage.getUserNow()
                ?: throw IllegalArgumentException("cannot change user when none is logged in")
            val user = ClientUpdateUser(
                value.uuid,
                updateUser.name,
                updateUser.password,
                updateUser.newPassword
            )
            if (!client.isClientOnline) {
                System.err.println("offline user edits are not allowed")
                return@runTask
            }
            try {
                val body = client.updateUser(user).body()
                if (body != null && body) {
                    persister.persist(user)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun updateListMedium(
        listSetting: MediaListSetting,
        newMediumType: Int
    ): CompletableFuture<String> {
        return TaskManager.runCompletableTask {
            if (listSetting is ExternalMediaListSetting) {
                return@runCompletableTask "Cannot update External Lists"
            }
            val listId = listSetting.listId
            val mediaList = ClientMediaList(
                listSetting.uuid,
                listId,
                listSetting.name,
                newMediumType, IntArray(0)
            )
            updateList(listSetting.name, newMediumType, listId, mediaList)
        }
    }

    fun updateListName(listSetting: MediaListSetting, newName: String): CompletableFuture<String> {
        return TaskManager.runCompletableTask {
            if (listSetting is ExternalMediaListSetting) {
                return@runCompletableTask "Cannot update External Lists"
            }
            val listId = listSetting.listId
            val mediaList = ClientMediaList(
                listSetting.uuid,
                listId,
                newName,
                listSetting.medium, IntArray(0)
            )
            updateList(newName, listSetting.medium, listId, mediaList)
        }
    }

    private fun updateList(
        newName: String,
        newMediumType: Int,
        listId: Int,
        mediaList: ClientMediaList
    ): String {
        try {
            if (!client.isClientOnline) {
                val setting =
                    storage.getListSettingNow(listId, false) ?: return "Not available in storage"
                val editEvents: MutableList<EditEvent> = ArrayList()
                if (setting.name != newName) {
                    editEvents.add(
                        EditEventImpl(
                            listId,
                            MEDIUM,
                            CHANGE_NAME,
                            setting.name,
                            newName
                        )
                    )
                }
                if (setting.medium != newMediumType) {
                    editEvents.add(
                        EditEventImpl(
                            listId,
                            MEDIUM,
                            CHANGE_TYPE,
                            setting.medium,
                            newMediumType
                        )
                    )
                }
                storage.insertEditEvent(editEvents)
                persister.persist(mediaList).finish()
                return ""
            }
            client.updateList(ClientMinList(mediaList.name, mediaList.medium))
            val query = client.getList(listId).body()

            if (query != null) {
                persister.persist(query).finish()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return "Could not update List"
        }
        return ""
    }

    fun updateMedium(mediumSettings: MediumSetting): CompletableFuture<String> {
        return TaskManager.runCompletableTask {
            val mediumId = mediumSettings.mediumId
            val clientMedium = ClientMedium(
                IntArray(0), IntArray(0),
                mediumSettings.currentRead, IntArray(0),
                mediumId,
                mediumSettings.getCountryOfOrigin(),
                mediumSettings.getLanguageOfOrigin(),
                mediumSettings.getAuthor(),
                mediumSettings.getTitle(),
                mediumSettings.medium,
                mediumSettings.getArtist(),
                mediumSettings.getLang(),
                mediumSettings.stateOrigin,
                mediumSettings.stateTL,
                mediumSettings.getSeries(),
                mediumSettings.getUniverse()
            )
            if (!client.isClientOnline) {
                val setting = storage.getMediumSettingsNow(mediumId)
                    ?: return@runCompletableTask "Not available in storage"
                val editEvents: MutableList<EditEvent> = ArrayList()
                if (setting.getTitle() != mediumSettings.getTitle()) {
                    editEvents.add(
                        EditEventImpl(
                            mediumId,
                            MEDIUM,
                            CHANGE_NAME,
                            setting.getTitle(),
                            mediumSettings.getTitle()
                        )
                    )
                }
                if (setting.medium != mediumSettings.medium) {
                    editEvents.add(
                        EditEventImpl(
                            mediumId,
                            MEDIUM,
                            CHANGE_TYPE,
                            setting.medium,
                            mediumSettings.medium
                        )
                    )
                }
                storage.insertEditEvent(editEvents)
                persister.persist(ClientSimpleMedium(clientMedium)).finish()
            }
            try {
                client.updateMedia(clientMedium)
                runBlocking {
                    val medium = client.getMedium(mediumId).body()
                    persister.persist(ClientSimpleMedium(medium!!)).finish()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return@runCompletableTask "Could not update Medium"
            }
            ""
        }
    }

    @Throws(Exception::class)
    fun updateRead(episodeIds: Collection<Int>, read: Boolean) {
        val progress = if (read) 1f else 0f
        Utils.doPartitionedEx(episodeIds) { ids: List<Int> ->
            if (!client.isClientOnline) {
                val filteredIds = storage.getReadEpisodes(episodeIds, !read)
                if (filteredIds.isEmpty()) {
                    return@doPartitionedEx false
                }
                val events: MutableCollection<EditEvent> = ArrayList(
                    filteredIds.size
                )
                for (id in filteredIds) {
                    events.add(EditEventImpl(id, EPISODE, CHANGE_PROGRESS, null, progress))
                }
                storage.insertEditEvent(events)
                storage.updateProgress(filteredIds, progress)
                return@doPartitionedEx false
            }
            !updateProgressOnline(progress, ids)
        }
    }

    @Throws(IOException::class)
    private fun updateProgressOnline(progress: Float, ids: Collection<Int>): Boolean {
        val response: Response<Boolean> = runBlocking { client.addProgress(ids, progress) }
        if (!response.isSuccessful || response.body() == null || !response.body()!!) {
            return false
        }
        storage.updateProgress(ids, progress)
        return true
    }

    fun removeItemFromList(listId: Int, mediumIds: MutableCollection<Int>): CompletableFuture<Boolean> {
        return TaskManager.runCompletableTask {
            try {
                if (!client.isClientOnline) {
                    val events: MutableCollection<EditEvent> = ArrayList(mediumIds.size)
                    for (id in mediumIds) {
                        val event: EditEvent = EditEventImpl(id, MEDIUM, REMOVE_FROM, listId, null)
                        events.add(event)
                    }
                    storage.insertEditEvent(events)
                    storage.removeItemFromList(listId, mediumIds)
                    storage.insertDanglingMedia(mediumIds)
                    return@runCompletableTask true
                }
                val response = client.deleteListMedia(listId, mediumIds)
                val success = response.body()
                if (success != null && success) {
                    storage.removeItemFromList(listId, mediumIds)
                    storage.insertDanglingMedia(mediumIds)
                    return@runCompletableTask true
                }
                return@runCompletableTask false
            } catch (e: IOException) {
                e.printStackTrace()
            }
            false
        }
    }

    fun addMediumToList(listId: Int, ids: MutableCollection<Int>): CompletableFuture<Boolean> {
        return TaskManager.runCompletableTask {
            try {
                // to prevent duplicates
                val items = storage.getListItems(listId)
                ids.removeAll(items)

                // adding nothing cannot fail
                if (ids.isEmpty()) {
                    return@runCompletableTask true
                }
                if (!client.isClientOnline) {
                    val events: MutableCollection<EditEvent> = ArrayList(ids.size)
                    for (id in ids) {
                        val event: EditEvent = EditEventImpl(id, MEDIUM, ADD_TO, null, listId)
                        events.add(event)
                    }
                    storage.insertEditEvent(events)
                    storage.addItemsToList(listId, ids)
                    return@runCompletableTask true
                }
                val response: Response<Boolean> = client.addListMedia(listId, ids)
                if (response.body() == null || !response.body()!!) {
                    return@runCompletableTask false
                }
                storage.addItemsToList(listId, ids)
                return@runCompletableTask true
            } catch (e: IOException) {
                e.printStackTrace()
                return@runCompletableTask false
            }
        }
    }

    fun moveItemFromList(
        oldListId: Int,
        newListId: Int,
        mediumId: Int
    ): CompletableFuture<Boolean> {
        return TaskManager.runCompletableTask {
            try {
                if (!client.isClientOnline) {
                    val event: EditEvent =
                        EditEventImpl(mediumId, MEDIUM, MOVE, oldListId, newListId)
                    storage.insertEditEvent(event)
                    storage.removeItemFromList(oldListId, mediumId)
                    storage.addItemsToList(newListId, setOf(mediumId))
                    return@runCompletableTask true
                }
                val response = client.updateListMedia(oldListId, newListId, mediumId)
                val success = response.body()
                if (success != null && success) {
                    storage.removeItemFromList(oldListId, mediumId)
                    storage.addItemsToList(newListId, setOf(mediumId))
                    return@runCompletableTask true
                }
                return@runCompletableTask false
            } catch (e: IOException) {
                e.printStackTrace()
            }
            false
        }
    }

    fun moveMediaToList(
        oldListId: Int,
        listId: Int,
        ids: MutableCollection<Int>
    ): CompletableFuture<Boolean> {
        return TaskManager.runCompletableTask {
            try {
                // to prevent duplicates
                val items = storage.getListItems(listId)
                ids.removeAll(items)

                // adding nothing cannot fail
                if (ids.isEmpty()) {
                    return@runCompletableTask true
                }
                if (!client.isClientOnline) {
                    val events: MutableCollection<EditEvent> = ArrayList(ids.size)
                    for (id in ids) {
                        val event: EditEvent = EditEventImpl(id, MEDIUM, MOVE, oldListId, listId)
                        events.add(event)
                    }
                    storage.insertEditEvent(events)
                    storage.moveItemsToList(oldListId, listId, ids)
                    return@runCompletableTask true
                }
                val successMove: MutableCollection<Int> = ArrayList()
                for (id in ids) {
                    val response = client.updateListMedia(oldListId, listId, id)
                    val success = response.body()
                    if (success != null && success) {
                        successMove.add(id)
                    }
                }
                storage.moveItemsToList(oldListId, listId, successMove)
                return@runCompletableTask !successMove.isEmpty()
            } catch (e: IOException) {
                e.printStackTrace()
                return@runCompletableTask false
            }
        }
    }

    companion object {
        const val USER = 1
        const val EXTERNAL_USER = 2
        const val EXTERNAL_LIST = 3
        const val LIST = 4
        const val MEDIUM = 5
        const val PART = 6
        const val EPISODE = 7
        const val RELEASE = 8
        const val NEWS = 9
        const val ADD = 1
        const val REMOVE = 2
        const val MOVE = 3
        const val ADD_TO = 4
        const val REMOVE_FROM = 5
        const val MERGE = 6
        const val CHANGE_NAME = 7
        const val CHANGE_TYPE = 8
        const val ADD_TOC = 9
        const val REMOVE_TOC = 10
        const val CHANGE_PROGRESS = 11
        const val CHANGE_READ = 12
    }

    init {
        client.addDisconnectedListener { timeDisconnected: DateTime? -> pushEditExecutor.execute { publishEditEvents() } }
    }
}