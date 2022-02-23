package com.mytlogos.enterprise.background.room

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.*
import com.mytlogos.enterprise.background.*
import com.mytlogos.enterprise.background.api.model.*
import com.mytlogos.enterprise.background.api.model.ClientStat.ParsedStat
import com.mytlogos.enterprise.background.api.model.ClientStat.Partstat
import com.mytlogos.enterprise.background.resourceLoader.*
import com.mytlogos.enterprise.background.resourceLoader.LoadWorkGenerator.*
import com.mytlogos.enterprise.background.room.model.*
import com.mytlogos.enterprise.background.room.model.RoomExternalMediaList.ExternalListMediaJoin
import com.mytlogos.enterprise.background.room.model.RoomMediaList.MediaListMediaJoin
import com.mytlogos.enterprise.model.*
import com.mytlogos.enterprise.tools.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import java.util.*
import java.util.function.Consumer

class RoomStorage(
    application: Application,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : DatabaseStorage {
    private val userDao: UserDao
    private val newsDao: NewsDao
    private val episodeDao: EpisodeDao
    private val partDao: PartDao
    private val mediumDao: MediumDao
    private val mediaListDao: MediaListDao
    private val externalMediaListDao: ExternalMediaListDao
    private val externalUserDao: ExternalUserDao
    private val notificationDao: NotificationDao
    private val failedEpisodesDao: FailedEpisodesDao
    private val userLiveData: LiveData<User?>
    private val mediumInWaitDao: RoomMediumInWaitDao
    private val roomDanglingDao: RoomDanglingDao
    private val mediumProgressDao: MediumProgressDao
    private val dataStructureDao: DataStructureDao
    private val toDownloadDao: ToDownloadDao
    private val editDao: EditDao
    private val tocDao: TocDao
    private var loading = false

    init {
        val database = AbstractDatabase.getInstance(application)
        userDao = database.userDao()
        newsDao = database.newsDao()
        externalUserDao = database.externalUserDao()
        externalMediaListDao = database.externalMediaListDao()
        mediaListDao = database.mediaListDao()
        mediumDao = database.mediumDao()
        partDao = database.partDao()
        episodeDao = database.episodeDao()
        toDownloadDao = database.toDownloadDao()
        mediumInWaitDao = database.roomMediumInWaitDao()
        roomDanglingDao = database.roomDanglingDao()
        notificationDao = database.notificationDao()
        mediumProgressDao = database.mediumProgressDao()
        failedEpisodesDao = database.failedEpisodesDao()
        dataStructureDao = database.dataStructureDao()
        editDao = database.editDao()
        tocDao = database.tocDao()
        userLiveData = userDao.user
    }

    override fun getUser(): LiveData<User?> {
        return userLiveData
    }

    override suspend fun getUserNow(): User? = withContext(ioDispatcher) {
        return@withContext userDao.getUserNow().fromRoom()
    }

    override fun getHomeStats(): LiveData<HomeStats> {
        return userDao.homeStats
    }

    fun deleteAllUser() {
        TaskManager.runTaskSuspend { userDao.deleteAllUser() }
    }

    override fun getPersister(repository: Repository, loadedData: LoadData): ClientModelPersister {
        return RoomPersister(loadedData)
    }

    override fun isLoading(): Boolean {
        return !loading
    }

    override fun setLoading(loading: Boolean) {
        this.loading = loading
    }

    override suspend fun getLoadData(): LoadData = withContext(ioDispatcher) {
        // todo maybe load this asynchronous?
        val data = LoadData()
        data.episodes.addAll(episodeDao.loaded())
        data.part.addAll(partDao.loaded())
        data.news.addAll(newsDao.loaded())
        data.media.addAll(mediumDao.loaded())
        data.externalMediaList.addAll(externalMediaListDao.loaded())
        data.externalUser.addAll(externalUserDao.loaded())
        data.mediaList.addAll(mediaListDao.loaded())
        return@withContext data
    }

    override suspend fun getListItems(listId: Int): Collection<Int> = withContext(ioDispatcher) {
        return@withContext mediaListDao.getListItems(listId)
    }

    override suspend fun getExternalListItems(externalListId: Int): Collection<Int> =
        withContext(ioDispatcher) {
            return@withContext externalMediaListDao.getExternalListItems(externalListId)
        }

    override suspend fun insertDanglingMedia(mediaIds: MutableCollection<Int>) =
        withContext(ioDispatcher) {
            val listMedia = mediaListDao.getAllLinkedMedia()
            val externalListMedia = externalMediaListDao.getAllLinkedMedia()
            mediaIds.removeAll(listMedia.toSet())
            mediaIds.removeAll(externalListMedia.toSet())
            if (mediaIds.isEmpty()) {
                return@withContext
            }
            mediaIds.toRoomDangling().doChunked(roomDanglingDao::insertBulk)
        }

    override suspend fun getListSettingNow(id: Int, isExternal: Boolean): MediaListSetting =
        withContext(ioDispatcher) {
            return@withContext if (isExternal) {
                externalMediaListDao.getExternalListSettingNow(id)
            } else mediaListDao.getListSettingsNow(id)
        }

    override suspend fun getMediumSettingsNow(mediumId: Int): MediumSetting =
        withContext(ioDispatcher) {
            return@withContext mediumDao.getMediumSettingsNow(mediumId)
        }

    override suspend fun getSimpleEpisodes(ids: Collection<Int>): List<SimpleEpisode> =
        withContext(ioDispatcher) {
            return@withContext ids.mapChunked { episodeDao.getSimpleEpisodes(it) }
        }

    override suspend fun updateProgress(episodeIds: Collection<Int>, progress: Float) =
        withContext(ioDispatcher) {
            val now = DateTime.now()
            episodeIds.doChunked { episodeDao.updateProgress(it, progress, now) }
        }

    override fun getReadTodayEpisodes(): Flow<PagingData<ReadEpisode>> {
        return episodeDao.readTodayEpisodes
            .map(RoomReadEpisode::fromRoom)
            .transformFlow()
    }

    override suspend fun addItemsToList(listId: Int, ids: Collection<Int>) =
        withContext(ioDispatcher) {
            val joins: MutableList<MediaListMediaJoin> = ArrayList()
            for (id in ids) {
                joins.add(MediaListMediaJoin(listId, id))
            }
            joins.doChunked(mediaListDao::addJoin)
        }

    override fun getListSuggestion(name: String): LiveData<MutableList<MediaList>> {
        return mediaListDao.getSuggestion(name)
    }

    override fun onDownloadAble(): LiveData<Boolean> {
        val previousDownloadCount = MutableLiveData<Int>()
        val previousEpisodeCount = MutableLiveData<Int>()
        val toDownloadCount = toDownloadDao.countMediaRows()
        val downloadableEpisodeCount = episodeDao.countDownloadableRows()
        val downloadAbles = MediatorLiveData<Boolean>()
        downloadAbles.addSource(toDownloadCount) { input: Int ->
            val previous = previousDownloadCount.value ?: 0
            previousDownloadCount.value = input
            downloadAbles.postValue(input > previous)
        }
        downloadAbles.addSource(downloadableEpisodeCount) { input: Int ->
            val previous = previousEpisodeCount.value ?: 0
            previousEpisodeCount.value = input
            downloadAbles.postValue(input > previous)
        }
        return downloadAbles
    }

    suspend fun clearMediaInWait() {
        mediumInWaitDao.clear()
    }

    override suspend fun removeItemFromList(listId: Int, mediumId: Collection<Int>) =
        withContext(ioDispatcher) {
            mediumId.doChunked { mediaListDao.removeJoin(listId, it) }
        }

    override suspend fun moveItemsToList(oldListId: Int, newListId: Int, ids: Collection<Int>) =
        withContext(ioDispatcher) {
            val oldJoins: MutableCollection<MediaListMediaJoin> = ArrayList()
            val newJoins: MutableCollection<MediaListMediaJoin> = ArrayList()
            for (id in ids) {
                oldJoins.add(MediaListMediaJoin(oldListId, id))
                newJoins.add(MediaListMediaJoin(newListId, id))
            }
            mediaListDao.moveJoins(oldJoins, newJoins)
        }

    override fun getExternalUser(): Flow<PagingData<ExternalUser>> {
        return externalUserDao.all.transformFlow()
    }

    override suspend fun getSpaceMedium(mediumId: Int): SpaceMedium = withContext(ioDispatcher) {
        return@withContext mediumDao.getSpaceMedium(mediumId)
    }

    override suspend fun getMediumType(mediumId: Int): Int = withContext(ioDispatcher) {
        return@withContext mediumDao.getMediumType(mediumId)
    }

    override suspend fun getReleaseLinks(episodeId: Int): List<String> = withContext(ioDispatcher) {
        return@withContext episodeDao.getReleaseLinks(episodeId)
    }

    override suspend fun clearLocalMediaData() = withContext(ioDispatcher) {
        failedEpisodesDao.clearAll()
        episodeDao.clearAllReleases()
        episodeDao.clearAll()
        partDao.clearAll()
        externalMediaListDao.clearJoins()
        mediaListDao.clearJoins()
        clearMediaInWait()
    }

    override suspend fun getSimpleMedium(mediumId: Int): SimpleMedium = withContext(ioDispatcher) {
        return@withContext mediumDao.getSimpleMedium(mediumId)
    }

    override suspend fun insertEditEvent(event: EditEvent) = withContext(ioDispatcher) {
        val roomEditEvent = event.toRoom()
        editDao.insert(roomEditEvent)
    }

    override suspend fun insertEditEvent(events: Collection<EditEvent>) =
        withContext(ioDispatcher) {
            val roomEditEvent = events.toRoomEditEvent()
            roomEditEvent.doChunked(editDao::insertBulk)
        }

    override suspend fun getReadEpisodes(episodeIds: Collection<Int>, read: Boolean): List<Int> =
        withContext(ioDispatcher) {
            return@withContext episodeIds.mapChunked { episodeDao.getReadEpisodes(it, read) }
        }

    override suspend fun getEditEvents(): MutableList<out EditEvent> = withContext(ioDispatcher) {
        return@withContext editDao.all
    }

    override suspend fun removeEditEvents(editEvents: Collection<EditEvent>) =
        withContext(ioDispatcher) {
            editEvents.toRoomEditEvent().doChunked(editDao::deleteBulk)
        }

    override suspend fun checkReload(parsedStat: ParsedStat): ReloadStat =
        withContext(ioDispatcher) {
            val roomStats = episodeDao.getStat()
            val partStats: MutableMap<Int, Partstat> = HashMap()
            for (value in parsedStat.media.values) {
                partStats.putAll(value)
            }
            val loadEpisode: MutableList<Int> = LinkedList()
            val loadRelease: MutableList<Int> = LinkedList()
            val loadMediumTocs: MutableList<Int> = LinkedList()
            val localStatMap: MutableMap<Int, RoomPartStat> = HashMap()
            for (localStat in roomStats) {
                localStatMap[localStat.partId] = localStat
                val partstat = partStats.remove(localStat.partId)
                if (partstat == null) {
                    println("Local Part ${localStat.partId} does not exist on Server, missing local Part Deletion")
                    continue
                }
                if (partstat.episodeCount != localStat.episodeCount
                    || partstat.episodeSum != localStat.episodeSum
                ) {
                    loadEpisode.add(localStat.partId)
                } else if (partstat.releaseCount != localStat.releaseCount) {
                    loadRelease.add(localStat.partId)
                }
            }
            val loadPart: MutableSet<Int> = HashSet()
            val loadData = this@RoomStorage.getLoadData()
            for ((partId, remotePartStat) in partStats) {
                val localPartStat = localStatMap[partId]
                if (localPartStat == null) {
                    if (!loadData.part.contains(partId)) {
                        loadPart.add(partId)
                    }
                    continue
                }
                if (remotePartStat.episodeCount != localPartStat.episodeCount
                    || remotePartStat.episodeSum != localPartStat.episodeSum
                ) {
                    loadEpisode.add(localPartStat.partId)
                } else if (remotePartStat.releaseCount != localPartStat.releaseCount) {
                    loadRelease.add(localPartStat.partId)
                }
            }
            val roomTocStats = tocDao.getStat()
            val tocStats: MutableMap<Int, Int> = HashMap()
            for (stat in roomTocStats) {
                tocStats[stat.mediumId] = stat.tocCount
            }
            for ((mediumId, stats) in parsedStat.mediaStats) {
                val previousTocCount = tocStats[mediumId]
                if (previousTocCount == null || stats.tocs != previousTocCount) {
                    loadMediumTocs.add(mediumId)
                }
            }
            val loadedMedia = loadData.media
            val missingMedia: MutableSet<Int> = HashSet(parsedStat.media.keys)
            missingMedia.removeAll(loadedMedia)
            loadMediumTocs.addAll(missingMedia)
            val missingExtLists: MutableSet<Int> = HashSet(parsedStat.extLists.keys)
            missingExtLists.removeAll(loadData.externalMediaList)
            val loadUser: MutableSet<String> = HashSet(parsedStat.extUser.keys)
            loadUser.removeAll(loadData.externalUser)
            val missingLists: MutableSet<Int> = HashSet(parsedStat.lists.keys)
            missingLists.removeAll(loadData.mediaList)
            for ((key, value) in parsedStat.extUser) {
                val iterator = missingExtLists.iterator()
                while (iterator.hasNext()) {
                    val listId = iterator.next()
                    if (value.contains(listId)) {
                        loadUser.add(key)
                        iterator.remove()
                    }
                }
            }
            return@withContext ReloadStat(loadEpisode,
                loadRelease,
                loadMediumTocs,
                missingMedia,
                loadPart,
                missingLists,
                loadUser)
        }

    override suspend fun syncProgress() = withContext(ioDispatcher) {
        val all = mediumProgressDao.getComparison()
        for (comparison in all) {
            if (comparison.currentMaxReadIndex != 0.0) {
                mediumProgressDao.update(RoomMediumProgress(
                    comparison.mediumId,
                    comparison.currentMaxReadIndex)
                )
                continue
            }
            val parts = partDao.getPartsNow(comparison.mediumId)

            // TODO: 09.09.2019 check this unused variable
            for (part in parts) {
                val episodeIds = episodeDao.getEpisodeIdsWithLowerIndex(
                    comparison.mediumId,
                    comparison.currentReadIndex,
                    true
                )
                try {
                    episodeIds.doChunked { episodeDao.updateProgress(it, 1f, DateTime.now()) }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private inner class RoomPersister(
        private val loadedData: LoadData,
        private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) : ClientModelPersister {
        private val generator: LoadWorkGenerator = LoadWorkGenerator(loadedData)

        override suspend fun persistEpisodes(episodes: Collection<ClientEpisode>): ClientModelPersister =
            withContext(ioDispatcher) {
                val filteredEpisodes = generator.filterEpisodes(episodes)
                return@withContext this@RoomPersister.persist(filteredEpisodes)
            }

        override suspend fun persistReleases(releases: Collection<ClientRelease>): ClientModelPersister =
            withContext(ioDispatcher) {
                releases.toRoomRelease().doChunked(episodeDao::insertBulkRelease)
                return@withContext this@RoomPersister
            }

        override suspend fun persist(filteredEpisodes: FilteredEpisodes): ClientModelPersister =
            withContext(ioDispatcher) {
                val list = filteredEpisodes.newEpisodes.toRoomEpisode()
                val update = filteredEpisodes.updateEpisodes.toRoomClientEpisode()

                list.doChunked(episodeDao::insertBulk)
                update.doChunked(episodeDao::updateBulkClient)

                for (episode in list) {
                    loadedData.episodes.add(episode.episodeId)
                }
                persistReleases(filteredEpisodes.releases.map { value: ClientEpisodeRelease ->
                    ClientRelease(
                        value.episodeId,
                        value.title,
                        value.url,
                        value.locked,
                        value.releaseDate
                    )
                })
                return@withContext this@RoomPersister
            }

        override suspend fun persistMediaLists(mediaLists: List<ClientMediaList>): ClientModelPersister =
            withContext(ioDispatcher) {
                val filteredMediaList = generator.filterMediaLists(mediaLists)
                return@withContext this@RoomPersister.persist(filteredMediaList)
            }

        override suspend fun persistUserLists(mediaLists: List<ClientUserList>): ClientModelPersister =
            withContext(ioDispatcher) {
                val uuid = getUserNow()!!.uuid
                return@withContext persistMediaLists(mediaLists.map { value: ClientUserList ->
                    ClientMediaList(
                        uuid,
                        value.id,
                        value.name,
                        value.medium,
                        null
                    )
                })
            }

        override suspend fun persist(
            filteredMediaList: FilteredMediaList,
        ): ClientModelPersister = withContext(ioDispatcher) {
            val list = filteredMediaList.newList.listToRoom()
            val update = filteredMediaList.updateList.listToRoom()

            list.doChunked(mediaListDao::insertBulk)
            update.doChunked(mediaListDao::updateBulk)

            for (mediaList in list) {
                loadedData.mediaList.add(mediaList.listId)
            }
            return@withContext this@RoomPersister
        }

        override suspend fun persistExternalMediaLists(externalMediaLists: Collection<ClientExternalMediaList>): ClientModelPersister =
            withContext(ioDispatcher) {
                val filteredExtMediaList = generator.filterExternalMediaLists(externalMediaLists)
                return@withContext this@RoomPersister.persist(filteredExtMediaList)
            }

        override suspend fun persist(filteredExtMediaList: FilteredExtMediaList): ClientModelPersister = withContext(ioDispatcher) {
            val list = filteredExtMediaList.newList.externalListToRoom()
            val update = filteredExtMediaList.updateList.externalListToRoom()

            list.doChunked(externalMediaListDao::insertBulk)
            update.doChunked(externalMediaListDao::updateBulk)

            for (mediaList in list) {
                loadedData.externalMediaList.add(mediaList.externalListId)
            }
            return@withContext this@RoomPersister
        }

        override suspend fun persistExternalUsers(externalUsers: List<ClientExternalUser>): ClientModelPersister =
            withContext(ioDispatcher) {
                val filteredExternalUser = generator.filterExternalUsers(externalUsers)
                return@withContext this@RoomPersister.persist(filteredExternalUser)
            }

        override suspend fun persist(
            filteredExternalUser: FilteredExternalUser
        ): ClientModelPersister = withContext(ioDispatcher) {
            val newUser = filteredExternalUser.newUser.externalUserToRoom()
            val updatedUser = filteredExternalUser.updateUser.externalUserToRoom()

            newUser.doChunked(externalUserDao::insertBulk)
            updatedUser.doChunked(externalUserDao::updateBulk)

            for (user in newUser) {
                loadedData.externalUser.add(user.uuid)
            }
            persistExternalMediaLists(filteredExternalUser.newList)
            persistExternalMediaLists(filteredExternalUser.updateList)
            return@withContext this@RoomPersister
        }

        override suspend fun persistMedia(media: Collection<ClientSimpleMedium>): ClientModelPersister =
            withContext(ioDispatcher) {
                val filteredMedia = generator.filterSimpleMedia(media)
                return@withContext persist(filteredMedia)
            }

        override suspend fun persist(filteredMedia: FilteredMedia): ClientModelPersister =
            withContext(ioDispatcher) {
                val newMedia = filteredMedia.newMedia.simpleToRoomMedium()
                val updatedMedia = filteredMedia.updateMedia.simpleToRoomMedium()

                try {
                    newMedia.doChunked(mediumDao::insertBulk)
                } catch (e: SQLiteConstraintException) {
                    e.printStackTrace()
                    throw e
                }
                updatedMedia.doChunked(mediumDao::updateBulk)

                for (medium in newMedia) {
                    loadedData.media.add(medium.mediumId)
                }
                return@withContext this@RoomPersister
            }

        override suspend fun persistNews(news: Collection<ClientNews>): ClientModelPersister =
            withContext(ioDispatcher) {
                val newNews: MutableList<RoomNews> = ArrayList()
                val updatedNews: MutableList<RoomNews> = ArrayList()
                for (clientNews in news) {
                    val roomNews = clientNews.toRoom()
                    if (loadedData.news.contains(clientNews.id)) {
                        updatedNews.add(roomNews)
                    } else {
                        newNews.add(roomNews)
                    }
                }

                newNews.doChunked(newsDao::insertNews)
                updatedNews.doChunked(newsDao::updateNews)

                for (roomNews in newNews) {
                    loadedData.news.add(roomNews.newsId)
                }
                return@withContext this@RoomPersister
            }

        override suspend fun persistParts(parts: Collection<ClientPart>): ClientModelPersister =
            withContext(ioDispatcher) {
                val filteredParts = generator.filterParts(parts)
                return@withContext persist(filteredParts)
            }

        override suspend fun persist(filteredParts: FilteredParts): ClientModelPersister =
            withContext(ioDispatcher) {
                val newParts = filteredParts.newParts.toRoomPart()
                val updatedParts = filteredParts.updateParts.toRoomPart()

                newParts.doChunked(partDao::insertBulk)
                updatedParts.doChunked(partDao::updateBulk)

                for (part in newParts) {
                    loadedData.part.add(part.partId)
                }
                persistEpisodes(filteredParts.episodes)
                return@withContext this@RoomPersister
            }

        override suspend fun persistReadEpisodes(readEpisodes: Collection<ClientReadEpisode>): ClientModelPersister =
            withContext(ioDispatcher) {
                val filteredReadEpisodes = generator.filterReadEpisodes(readEpisodes)
                return@withContext this@RoomPersister.persist(filteredReadEpisodes)
            }

        override suspend fun persist(filteredReadEpisodes: FilteredReadEpisodes): ClientModelPersister =
            withContext(ioDispatcher) {

                for (readEpisode in filteredReadEpisodes.episodeList) {
                    episodeDao.updateProgress(
                        readEpisode.episodeId,
                        readEpisode.progress,
                        readEpisode.readDate
                    )
                }

                return@withContext this@RoomPersister
            }

        override suspend fun persist(query: ClientListQuery): ClientModelPersister =
            withContext(ioDispatcher) {
                this@RoomPersister.persistMedia(query.media.asList().map(::ClientSimpleMedium))
                this@RoomPersister.persist(query.list)
                return@withContext this@RoomPersister
            }

        override suspend fun persist(query: ClientMultiListQuery): ClientModelPersister =
            withContext(ioDispatcher) {
                this@RoomPersister.persistMedia(query.media.asList().map(::ClientSimpleMedium))
                this@RoomPersister.persist(*query.list)
                return@withContext this@RoomPersister
            }

        override suspend fun persistToDownloads(toDownloads: Collection<ToDownload>): ClientModelPersister =
            withContext(ioDispatcher) {
                val roomToDownloads = toDownloads.toRoomToDownload()

                roomToDownloads.doChunked(toDownloadDao::insertBulk)

                return@withContext this@RoomPersister
            }

        override suspend fun persist(user: ClientUpdateUser): ClientModelPersister =
            withContext(ioDispatcher) {
                val value = userLiveData.value
                    ?: throw IllegalArgumentException("cannot update user if none is stored in the database")
                require(user.uuid == value.uuid) { "cannot update user which do not share the same uuid" }
                // at the moment the only thing that can change for the user on client side is the name
                if (user.name == value.name) {
                    return@withContext this@RoomPersister
                }

                userDao.update(RoomUser(user.name, value.uuid, value.session))

                return@withContext this@RoomPersister
            }

        override suspend fun persist(toDownload: ToDownload): ClientModelPersister =
            withContext(ioDispatcher) {

                toDownloadDao.insert(toDownload.toRoom())

                return@withContext this@RoomPersister
            }

        override suspend fun persistMediaInWait(medium: List<ClientMediumInWait>) =
            withContext(ioDispatcher) {
                medium.clientToRoomInWait().doChunked(mediumInWaitDao::insertBulk)
            }

        override suspend fun persist(user: ClientSimpleUser?): ClientModelPersister =
            withContext(ioDispatcher) {
                // short cut version
                if (user == null) {
                    deleteAllUser()
                    return@withContext this@RoomPersister
                }
                val newRoomUser = user.toRoom()

                val currentUser = userDao.getUserNow()
                if (currentUser != null && newRoomUser.uuid == currentUser.uuid) {
                    // update user, so previous one wont be deleted
                    userDao.update(newRoomUser)
                } else {
                    userDao.deleteAllUser()
                    // persist user
                    userDao.insert(newRoomUser)
                }

                return@withContext this@RoomPersister
            }

        override suspend fun deleteLeftoverEpisodes(partEpisodes: Map<Int, List<Int>>) =
            withContext(ioDispatcher) {
                val episodes = partEpisodes.keys.mapChunked { episodeDao.getEpisodes(it) }
                val deleteEpisodes: List<Int> = episodes.mapNotNull { roomPartEpisode ->
                    val episodeIds = partEpisodes[roomPartEpisode.partId]

                    if (episodeIds == null || !episodeIds.contains(roomPartEpisode.episodeId)) {
                        return@mapNotNull roomPartEpisode.episodeId
                    } else {
                        return@mapNotNull null
                    }
                }
                deleteEpisodes.doChunked(episodeDao::deletePerId)
            }

        override suspend fun deleteLeftoverReleases(partReleases: Map<Int, List<ClientSimpleRelease>>): Collection<Int> =
            withContext(ioDispatcher) {
                val roomReleases = partReleases.keys.mapChunked { episodeDao.getReleases(it) }
                val deleteRelease: MutableList<RoomRelease> = LinkedList()
                val now = DateTime.now()
                val unmatchedReleases: MutableCollection<ClientSimpleRelease> = HashSet()

                for (list in partReleases.values) {
                    unmatchedReleases.addAll(list)
                }

                roomReleases.forEach(Consumer { release: RoomSimpleRelease ->
                    val releases = partReleases[release.partId]
                    var found = false
                    if (releases != null) {
                        for (simpleRelease in releases) {
                            if (simpleRelease.episodeId == release.episodeId && simpleRelease.url == release.url) {
                                found = true
                                unmatchedReleases.remove(simpleRelease)
                                break
                            }
                        }
                    }
                    if (!found) {
                        deleteRelease.add(RoomRelease(release.episodeId,
                            "",
                            release.url,
                            now,
                            false))
                    }
                })
                val episodesToLoad: MutableCollection<Int> = HashSet()
                for (release in unmatchedReleases) {
                    episodesToLoad.add(release.episodeId)
                }
                deleteRelease.doChunked(episodeDao::deleteBulkRelease)
                return@withContext episodesToLoad
            }

        override suspend fun deleteLeftoverTocs(mediaTocs: Map<Int, List<String>>) =
            withContext(ioDispatcher) {
                val previousTocs = mediaTocs.keys.mapChunked { tocDao.getTocs(it) }
                val removeTocs: MutableList<RoomToc> = ArrayList()
                for (entry in previousTocs) {
                    val currentTocLinks = mediaTocs[entry.mediumId]
                    if (currentTocLinks == null || !currentTocLinks.contains(entry.link)) {
                        removeTocs.add(entry)
                    }
                }
                removeTocs.doChunked(tocDao::deleteBulk)
            }

        override suspend fun persistTocs(tocs: Collection<Toc>): ClientModelPersister =
            withContext(ioDispatcher) {
                val roomTocs = tocs.toRoomToc()
                roomTocs.doChunked(tocDao::insertBulk)
                return@withContext this@RoomPersister
            }

        override suspend fun persist(clientUser: ClientUser?): ClientModelPersister =
            withContext(ioDispatcher) {
                // short cut version
                if (clientUser == null) {
                    deleteAllUser()
                    return@withContext this@RoomPersister
                }
                val newRoomUser = clientUser.toRoom()

                val currentUser = userDao.getUserNow()
                if (currentUser != null && newRoomUser.uuid == currentUser.uuid) {
                    // update user, so previous one wont be deleted
                    userDao.update(newRoomUser)
                } else {
                    userDao.deleteAllUser()
                    // persist user
                    userDao.insert(newRoomUser)
                }

                // persist lists
                this@RoomPersister.persist(*clientUser.lists)
                // persist externalUser
                this@RoomPersister.persist(*clientUser.externalUser)
                return@withContext this@RoomPersister
            }

        override suspend fun persist(stat: ParsedStat): ClientModelPersister =
            withContext(ioDispatcher) {
                /*
                 * Remove any Join not defined in stat.lists
                 * Remove any Join not defined in stat.exLists
                 * Remove any ExList not defined for a user in stat.exUser
                 * Remove any ExList which is not a key in stat.exLists
                 * Remove any List which is not a key in stat.Lists
                 * Remove any ExUser which is not a key in stat.exUser
                 * Add any new ListJoin
                 * Add any new ExListJoin
                 */
                val listUser = externalMediaListDao.getListUser()

                val deletedLists: MutableSet<Int> = HashSet()
                val deletedExLists: MutableSet<Int> = HashSet()
                val deletedExUser: MutableSet<String> = HashSet()
                val newInternalJoins: MutableList<MediaListMediaJoin> = LinkedList()
                val toDeleteInternalJoins =
                    filterListMediumJoins(stat, deletedLists, newInternalJoins, false)
                val newExternalJoins: MutableList<ExternalListMediaJoin> = LinkedList()
                val toDeleteExternalJoins =
                    filterListMediumJoins(stat, deletedExLists, newExternalJoins, true)

                for (roomListUser in listUser) {
                    val listIds = stat.extUser[roomListUser.uuid]
                    if (listIds == null) {
                        deletedExUser.add(roomListUser.uuid)
                        deletedExLists.add(roomListUser.listId)
                        break
                    }
                    if (!listIds.contains(roomListUser.listId)) {
                        deletedExLists.add(roomListUser.listId)
                    }
                }
                toDeleteExternalJoins.doChunked(externalMediaListDao::removeJoin)
                toDeleteInternalJoins.doChunked(mediaListDao::removeJoin)
                newExternalJoins.doChunked(externalMediaListDao::addJoin)
                newInternalJoins.doChunked(mediaListDao::addJoin)
                deletedExLists.doChunked(externalMediaListDao::delete)
                deletedLists.doChunked(mediaListDao::delete)
                deletedExUser.doChunked(externalUserDao::delete)
                return@withContext this@RoomPersister
            }

        private suspend fun <T : ListMediaJoin> filterListMediumJoins(
            stat: ParsedStat,
            deletedLists: MutableSet<Int>,
            newJoins: MutableList<T>,
            external: Boolean,
        ): List<T> = withContext(ioDispatcher) {
            val previousListJoins: MutableList<T>
            val currentJoins: Map<Int, List<Int>>
            if (external) {
                currentJoins = stat.extLists
                previousListJoins = externalMediaListDao.getListItems() as MutableList<T>
            } else {
                currentJoins = stat.lists
                previousListJoins = mediaListDao.getListItems() as MutableList<T>
            }
            val previousListJoinMap: MutableMap<Int, MutableSet<Int>> = HashMap()
            previousListJoins.removeIf { join: T ->
                previousListJoinMap.computeIfAbsent(join.listId) { HashSet() }.add(join.mediumId)

                val currentListItems = currentJoins[join.listId]
                if (currentListItems == null) {
                    deletedLists.add(join.listId)
                    return@removeIf false
                }
                currentListItems.contains(join.mediumId)
            }

            // every join that is not in previousListJoin is added to newJoins
            for ((listId, value) in currentJoins) {
                var previousItems: Set<Int>? = previousListJoinMap[listId]
                if (previousItems == null) {
                    previousItems = emptySet()
                }
                for (mediumId in value) {
                    if (!previousItems.contains(mediumId)) {
                        if (external) {
                            newJoins.add(ExternalListMediaJoin(listId, mediumId) as T)
                        } else {
                            newJoins.add(MediaListMediaJoin(listId, mediumId) as T)
                        }
                    }
                }
            }
            return@withContext previousListJoins
        }

        override fun finish() { /* no-op */
        }

    }
}