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
import com.mytlogos.enterprise.tools.doPartitionedExSuspend
import com.mytlogos.enterprise.tools.doPartitionedSuspend
import com.mytlogos.enterprise.tools.transformFlow
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import org.joda.time.DateTime
import java.util.*
import java.util.function.Consumer

class RoomStorage(application: Application) : DatabaseStorage {
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

    override suspend fun getUserNow(): User? {
        val converter = RoomConverter()
        return converter.convert(userDao.getUserNow())
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

    override suspend fun getLoadData(): LoadData {
        // todo maybe load this asynchronous?
        val data = LoadData()
        data.episodes.addAll(episodeDao.loaded())
        data.part.addAll(partDao.loaded())
        data.news.addAll(newsDao.loaded())
        data.media.addAll(mediumDao.loaded())
        data.externalMediaList.addAll(externalMediaListDao.loaded())
        data.externalUser.addAll(externalUserDao.loaded())
        data.mediaList.addAll(mediaListDao.loaded())
        return data
    }

    override suspend fun getListItems(listId: Int): Collection<Int> {
        return mediaListDao.getListItems(listId)
    }

    override suspend fun getExternalListItems(externalListId: Int): Collection<Int> {
        return externalMediaListDao.getExternalListItems(externalListId)
    }

    override suspend fun insertDanglingMedia(mediaIds: MutableCollection<Int>) {
        val listMedia = mediaListDao.getAllLinkedMedia()
        val externalListMedia = externalMediaListDao.getAllLinkedMedia()
        mediaIds.removeAll(listMedia)
        mediaIds.removeAll(externalListMedia)
        if (mediaIds.isEmpty()) {
            return
        }
        val converter = RoomConverter()
        roomDanglingDao.insertBulk(converter.convertToDangling(mediaIds))
    }

    override suspend fun getListSettingNow(id: Int, isExternal: Boolean): MediaListSetting {
        return if (isExternal) {
            externalMediaListDao.getExternalListSettingNow(id)
        } else mediaListDao.getListSettingsNow(id)
    }

    override suspend fun getMediumSettingsNow(mediumId: Int): MediumSetting {
        return mediumDao.getMediumSettingsNow(mediumId)
    }

    override suspend fun getSimpleEpisodes(ids: Collection<Int>): List<SimpleEpisode> {
        return episodeDao.getSimpleEpisodes(ids)
    }

    override suspend fun updateProgress(episodeIds: Collection<Int>, progress: Float) {
        episodeDao.updateProgress(episodeIds, progress, DateTime.now())
    }

    override fun getReadTodayEpisodes(): Flow<PagingData<ReadEpisode>> {
        val converter = RoomConverter()
        return episodeDao.readTodayEpisodes
            .map(converter::convert)
            .transformFlow()
    }

    override suspend fun addItemsToList(listId: Int, ids: Collection<Int>) {
        val joins: MutableList<MediaListMediaJoin> = ArrayList()
        for (id in ids) {
            joins.add(MediaListMediaJoin(listId, id))
        }
        mediaListDao.addJoin(joins)
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

    override suspend fun removeItemFromList(listId: Int, mediumId: Collection<Int>) {
        mediaListDao.removeJoin(listId, mediumId)
    }

    override suspend fun moveItemsToList(oldListId: Int, newListId: Int, ids: Collection<Int>) {
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

    override suspend fun getSpaceMedium(mediumId: Int): SpaceMedium {
        return mediumDao.getSpaceMedium(mediumId)
    }

    override suspend fun getMediumType(mediumId: Int): Int {
        return mediumDao.getMediumType(mediumId)
    }

    override suspend fun getReleaseLinks(episodeId: Int): List<String> {
        return episodeDao.getReleaseLinks(episodeId)
    }

    override suspend fun clearLocalMediaData() {
        failedEpisodesDao.clearAll()
        episodeDao.clearAllReleases()
        episodeDao.clearAll()
        partDao.clearAll()
        externalMediaListDao.clearJoins()
        mediaListDao.clearJoins()
        clearMediaInWait()
    }

    override suspend fun getSimpleMedium(mediumId: Int): SimpleMedium {
        return mediumDao.getSimpleMedium(mediumId)
    }

    override suspend fun insertEditEvent(event: EditEvent) {
        val converter = RoomConverter()
        val roomEditEvent = converter.convert(event)
        editDao.insert(roomEditEvent)
    }

    override suspend fun insertEditEvent(events: Collection<EditEvent>) {
        val converter = RoomConverter()
        val roomEditEvent = converter.convertEditEvents(events)
        editDao.insertBulk(roomEditEvent)
    }

    override suspend fun getReadEpisodes(episodeIds: Collection<Int>, read: Boolean): List<Int> {
        return episodeDao.getReadEpisodes(episodeIds, read)
    }

    override suspend fun getEditEvents(): MutableList<out EditEvent> {
        return editDao.all
    }

    override suspend fun removeEditEvents(editEvents: Collection<EditEvent>) {
        val converter = RoomConverter()
        editDao.deleteBulk(converter.convertEditEvents(editEvents))
    }

    override suspend fun checkReload(parsedStat: ParsedStat): ReloadStat {
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
                println(String.format(
                    "Local Part %s does not exist on Server, missing local Part Deletion",
                    localStat.partId
                ))
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
        val loadData = this.getLoadData()
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
        return ReloadStat(loadEpisode,
            loadRelease,
            loadMediumTocs,
            missingMedia,
            loadPart,
            missingLists,
            loadUser)
    }

    override suspend fun syncProgress() = coroutineScope {
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
                    episodeIds.doPartitionedExSuspend { ids: List<Int> ->
                        async {
                            episodeDao.updateProgress(ids, 1f, DateTime.now())
                            false
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private inner class RoomPersister(private val loadedData: LoadData) : ClientModelPersister {
        private val generator: LoadWorkGenerator = LoadWorkGenerator(loadedData)

        override fun getConsumer(): Collection<ClientConsumer<*>> {
            return emptyList()
        }

        override fun persistEpisodes(episodes: Collection<ClientEpisode>): ClientModelPersister {
            val filteredEpisodes = generator.filterEpisodes(episodes)
            return this.persist(filteredEpisodes)
        }

        override fun persistReleases(releases: Collection<ClientRelease>): ClientModelPersister {
            val converter = RoomConverter(loadedData)
            episodeDao.insertBulkRelease(converter.convertReleases(releases))
            return this
        }

        override fun persist(filteredEpisodes: FilteredEpisodes): ClientModelPersister {
            val converter = RoomConverter(loadedData)
            val list = converter.convertEpisodes(filteredEpisodes.newEpisodes)
            val update = converter.convertEpisodesClient(filteredEpisodes.updateEpisodes)

            episodeDao.insertBulk(list)
            episodeDao.updateBulkClient(update)

            for (episode in list) {
                loadedData.episodes.add(episode.episodeId)
            }
            persistReleases(filteredEpisodes.releases.map { value: ClientEpisodeRelease ->
                ClientRelease(
                    value.episodeId,
                    value.title,
                    value.url,
                    value.isLocked,
                    value.releaseDate
                )
            })
            return this
        }

        override fun persistMediaLists(mediaLists: List<ClientMediaList>): ClientModelPersister {
            val filteredMediaList = generator.filterMediaLists(mediaLists)
            val converter = RoomConverter(loadedData)
            return this.persist(filteredMediaList, converter)
        }

        override fun persistUserLists(mediaLists: List<ClientUserList>): ClientModelPersister {
            val uuid = runBlocking { getUserNow()!!.uuid }
            return persistMediaLists(mediaLists.map { value: ClientUserList ->
                ClientMediaList(
                    uuid,
                    value.id,
                    value.name,
                    value.medium,
                    null
                )
            })
        }

        override fun persist(filteredMediaList: FilteredMediaList): ClientModelPersister {
            return this.persist(filteredMediaList, RoomConverter(loadedData))
        }

        private fun persist(
            filteredMediaList: FilteredMediaList,
            converter: RoomConverter,
        ): ClientModelPersister {
            val list = converter.convertMediaList(filteredMediaList.newList)
            val update = converter.convertMediaList(filteredMediaList.updateList)

            mediaListDao.insertBulk(list)
            mediaListDao.updateBulk(update)

            for (mediaList in list) {
                loadedData.mediaList.add(mediaList.listId)
            }
            return this
        }

        override fun persistExternalMediaLists(externalMediaLists: Collection<ClientExternalMediaList>): ClientModelPersister {
            val filteredExtMediaList = generator.filterExternalMediaLists(externalMediaLists)
            val converter = RoomConverter(loadedData)
            return this.persist(filteredExtMediaList, converter)
        }

        override fun persist(filteredExtMediaList: FilteredExtMediaList): ClientModelPersister {
            return this.persist(filteredExtMediaList, RoomConverter(loadedData))
        }

        private fun persist(
            filteredExtMediaList: FilteredExtMediaList,
            converter: RoomConverter,
        ): ClientModelPersister {
            val list = converter.convertExternalMediaList(filteredExtMediaList.newList)
            val update = converter.convertExternalMediaList(filteredExtMediaList.updateList)

            externalMediaListDao.insertBulk(list)
            externalMediaListDao.updateBulk(update)

            for (mediaList in list) {
                loadedData.externalMediaList.add(mediaList.externalListId)
            }
            return this
        }

        override fun persistExternalUsers(externalUsers: List<ClientExternalUser>): ClientModelPersister {
            val filteredExternalUser = generator.filterExternalUsers(externalUsers)
            return this.persist(filteredExternalUser)
        }

        override fun persist(filteredExternalUser: FilteredExternalUser): ClientModelPersister {
            val converter = RoomConverter(loadedData)
            return this.persist(filteredExternalUser, converter)
        }

        private fun persist(
            filteredExternalUser: FilteredExternalUser,
            converter: RoomConverter,
        ): ClientModelPersister {
            val newUser = converter.convertExternalUser(filteredExternalUser.newUser)
            val updatedUser = converter.convertExternalUser(filteredExternalUser.updateUser)

            externalUserDao.insertBulk(newUser)
            externalUserDao.updateBulk(updatedUser)

            for (user in newUser) {
                loadedData.externalUser.add(user.uuid)
            }
            persistExternalMediaLists(filteredExternalUser.newList)
            persistExternalMediaLists(filteredExternalUser.updateList)
            return this
        }

        override fun persistMedia(media: Collection<ClientSimpleMedium>): ClientModelPersister {
            val filteredMedia = generator.filterSimpleMedia(media)
            return persist(filteredMedia)
        }

        override fun persist(filteredMedia: FilteredMedia): ClientModelPersister {
            val converter = RoomConverter(loadedData)
            val newMedia = converter.convertSimpleMedia(filteredMedia.newMedia)
            val updatedMedia = converter.convertSimpleMedia(filteredMedia.updateMedia)

            try {
                mediumDao.insertBulk(newMedia)
            } catch (e: SQLiteConstraintException) {
                e.printStackTrace()
                throw e
            }
            mediumDao.updateBulk(updatedMedia)

            for (medium in newMedia) {
                loadedData.media.add(medium.mediumId)
            }
            return this
        }

        override fun persistNews(news: Collection<ClientNews>): ClientModelPersister {
            val newNews: MutableList<RoomNews> = ArrayList()
            val updatedNews: MutableList<RoomNews> = ArrayList()
            val converter = RoomConverter()
            for (clientNews in news) {
                val roomNews = converter.convert(clientNews)
                if (loadedData.news.contains(clientNews.id)) {
                    updatedNews.add(roomNews)
                } else {
                    newNews.add(roomNews)
                }
            }

            newsDao.insertNews(newNews)
            newsDao.updateNews(updatedNews)

            for (roomNews in newNews) {
                loadedData.news.add(roomNews.newsId)
            }
            return this
        }

        override fun persistParts(parts: Collection<ClientPart>): ClientModelPersister {
            val filteredParts = generator.filterParts(parts)
            return persist(filteredParts)
        }

        override fun persist(filteredParts: FilteredParts): ClientModelPersister {
            val converter = RoomConverter()
            val newParts = converter.convertParts(filteredParts.newParts)
            val updatedParts = converter.convertParts(filteredParts.updateParts)

            partDao.insertBulk(newParts)
            partDao.updateBulk(updatedParts)

            for (part in newParts) {
                loadedData.part.add(part.partId)
            }
            persistEpisodes(filteredParts.episodes)
            return this
        }

        override fun persistReadEpisodes(readEpisodes: Collection<ClientReadEpisode>): ClientModelPersister {
            val filteredReadEpisodes = generator.filterReadEpisodes(readEpisodes)
            return this.persist(filteredReadEpisodes)
        }

        override suspend fun persist(filteredReadEpisodes: FilteredReadEpisodes): ClientModelPersister {

            for (readEpisode in filteredReadEpisodes.episodeList) {
                episodeDao.updateProgress(
                    readEpisode.episodeId,
                    readEpisode.progress,
                    readEpisode.readDate
                )
            }

            return this
        }

        override fun persist(query: ClientListQuery): ClientModelPersister {
            this.persistMedia(query.media.asList()
                .map { medium: ClientMedium -> ClientSimpleMedium(medium) })
            this.persist(query.list)
            return this
        }

        override fun persist(query: ClientMultiListQuery): ClientModelPersister {
            this.persistMedia(query.media.asList()
                .map { medium: ClientMedium -> ClientSimpleMedium(medium) })
            this.persist(*query.list)
            return this
        }

        override fun persistToDownloads(toDownloads: Collection<ToDownload>): ClientModelPersister {
            val roomToDownloads = RoomConverter().convertToDownload(toDownloads)

            toDownloadDao.insertBulk(roomToDownloads)

            return this
        }

        override fun persist(user: ClientUpdateUser): ClientModelPersister {
            val value = userLiveData.value
                ?: throw IllegalArgumentException("cannot update user if none is stored in the database")
            require(user.uuid == value.uuid) { "cannot update user which do not share the same uuid" }
            // at the moment the only thing that can change for the user on client side is the name
            if (user.name == value.name) {
                return this
            }

            userDao.update(RoomUser(user.name, value.uuid, value.session))

            return this
        }

        override suspend fun persist(toDownload: ToDownload): ClientModelPersister {

            toDownloadDao.insert(RoomConverter().convert(toDownload))

            return this
        }

        override fun persistMediaInWait(medium: List<ClientMediumInWait>) = runBlocking {
            mediumInWaitDao.insertBulk(RoomConverter().convertClientMediaInWait(medium))
        }

        override fun persist(user: ClientSimpleUser?): ClientModelPersister {
            // short cut version
            if (user == null) {
                deleteAllUser()
                return this
            }
            val converter = RoomConverter()
            val newRoomUser = converter.convert(user)

            val currentUser = userDao.getUserNow()
            if (currentUser != null && newRoomUser.uuid == currentUser.uuid) {
                // update user, so previous one wont be deleted
                userDao.update(newRoomUser)
            } else {
                userDao.deleteAllUser()
                // persist user
                userDao.insert(newRoomUser)
            }

            return this
        }

        override suspend fun deleteLeftoverEpisodes(partEpisodes: Map<Int, List<Int>>) {
            val partIds = partEpisodes.keys
            val episodes = episodeDao.getEpisodes(partIds)
            val deleteEpisodes: List<Int> = episodes.mapNotNull { roomPartEpisode ->
                val episodeIds = partEpisodes[roomPartEpisode.partId]

                if (episodeIds == null || !episodeIds.contains(roomPartEpisode.episodeId)) {
                    return@mapNotNull roomPartEpisode.episodeId
                } else {
                    return@mapNotNull null
                }
            }
            coroutineScope {
                deleteEpisodes.doPartitionedSuspend { ids: List<Int> ->
                    async {
                        episodeDao.deletePerId(ids)
                        false
                    }
                }
            }
        }

        override suspend fun deleteLeftoverReleases(partReleases: Map<Int, List<ClientSimpleRelease>>): Collection<Int> {
            val roomReleases = episodeDao.getReleases(partReleases.keys)
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
                        if (simpleRelease.id == release.episodeId && simpleRelease.url == release.url) {
                            found = true
                            unmatchedReleases.remove(simpleRelease)
                            break
                        }
                    }
                }
                if (!found) {
                    deleteRelease.add(RoomRelease(release.episodeId, "", release.url, now, false))
                }
            })
            val episodesToLoad: MutableCollection<Int> = HashSet()
            for (release in unmatchedReleases) {
                episodesToLoad.add(release.id)
            }
            episodeDao.deleteBulkRelease(deleteRelease)
            return episodesToLoad
        }

        override suspend fun deleteLeftoverTocs(mediaTocs: Map<Int, List<String>>) {
            val previousTocs = tocDao.getTocs(mediaTocs.keys)
            val removeTocs: MutableList<RoomToc> = ArrayList()
            for (entry in previousTocs) {
                val currentTocLinks = mediaTocs[entry.mediumId]
                if (currentTocLinks == null || !currentTocLinks.contains(entry.link)) {
                    removeTocs.add(entry)
                }
            }
            tocDao.deleteBulk(removeTocs)
        }

        override fun persistTocs(tocs: Collection<Toc>): ClientModelPersister {
            val roomTocs = RoomConverter().convertToc(tocs)

            tocDao.insertBulk(roomTocs)

            return this
        }

        override fun persist(clientUser: ClientUser?): ClientModelPersister {
            // short cut version
            if (clientUser == null) {
                deleteAllUser()
                return this
            }
            val converter = RoomConverter()
            val newRoomUser = converter.convert(clientUser)

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
            this.persist(*clientUser.lists)
            // persist externalUser
            this.persist(*clientUser.externalUser)
            return this
        }

        override fun persist(stat: ParsedStat): ClientModelPersister {
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
            externalMediaListDao.removeJoin(toDeleteExternalJoins)
            mediaListDao.removeJoin(toDeleteInternalJoins)
            externalMediaListDao.addJoin(newExternalJoins)
            mediaListDao.addJoin(newInternalJoins)
            externalMediaListDao.delete(deletedExLists)
            mediaListDao.delete(deletedLists)
            externalUserDao.delete(deletedExUser)
            return this
        }

        private suspend fun <T : ListMediaJoin?> filterListMediumJoins(
            stat: ParsedStat,
            deletedLists: MutableSet<Int>,
            newJoins: MutableList<T>,
            external: Boolean,
        ): List<T> {
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
                previousListJoinMap.computeIfAbsent(join!!.listId) { integer: Int? -> HashSet() }
                    .add(join.mediumId)
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
            return previousListJoins
        }

        override fun finish() {}

    }
}