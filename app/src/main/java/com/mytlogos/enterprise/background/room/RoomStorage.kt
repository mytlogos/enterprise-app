package com.mytlogos.enterprise.background.room

import android.app.Application
import androidx.arch.core.util.Function
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.mytlogos.enterprise.background.*
import com.mytlogos.enterprise.background.api.model.*
import com.mytlogos.enterprise.background.api.model.ClientStat.ParsedStat
import com.mytlogos.enterprise.background.api.model.ClientStat.Partstat
import com.mytlogos.enterprise.background.resourceLoader.DependantValue
import com.mytlogos.enterprise.background.resourceLoader.DependencyTask
import com.mytlogos.enterprise.background.resourceLoader.LoadWorkGenerator
import com.mytlogos.enterprise.background.resourceLoader.LoadWorkGenerator.*
import com.mytlogos.enterprise.background.resourceLoader.LoadWorker
import com.mytlogos.enterprise.background.room.model.*
import com.mytlogos.enterprise.background.room.model.RoomExternalMediaList.ExternalListMediaJoin
import com.mytlogos.enterprise.background.room.model.RoomMediaList.MediaListMediaJoin
import com.mytlogos.enterprise.model.*
import com.mytlogos.enterprise.tools.Sortings
import com.mytlogos.enterprise.tools.Utils
import com.mytlogos.enterprise.viewmodel.EpisodeViewModel
import org.joda.time.DateTime
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors

class RoomStorage(application: Application?) : DatabaseStorage {
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
    private val userLiveData: LiveData<User>
    private val mediumInWaitDao: RoomMediumInWaitDao
    private val roomDanglingDao: RoomDanglingDao
    private val mediumProgressDao: MediumProgressDao
    private val dataStructureDao: DataStructureDao
    private val toDownloadDao: ToDownloadDao
    private val editDao: EditDao
    private var loading = false
    private val tocDao: TocDao
    override fun getUser(): LiveData<User> {
        return userLiveData
    }

    override fun getUserNow(): User {
        val converter = RoomConverter()
        return converter.convert(userDao.userNow)
    }

    override fun getHomeStats(): LiveData<HomeStats> {
        return userDao.homeStats
    }

    override fun deleteAllUser() {
        TaskManager.runTask { userDao.deleteAllUser() }
    }

    override fun getPersister(repository: Repository, loadedData: LoadData): ClientModelPersister {
        return RoomPersister(loadedData)
    }

    override fun getDependantGenerator(loadedData: LoadData): DependantGenerator {
        return RoomDependantGenerator(loadedData)
    }

    override fun deleteOldNews() {
        TaskManager.runTask { newsDao.deleteOldNews() }
    }

    override fun isLoading(): Boolean {
        return !loading
    }

    override fun setLoading(loading: Boolean) {
        this.loading = loading
    }

    override fun getLoadData(): LoadData {
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

    override fun getNews(): LiveData<PagedList<News>> {
        return LivePagedListBuilder(newsDao.news, 50).build()
    }

    override fun getSavedEpisodes(): List<Int> {
        return episodeDao.allSavedEpisodes
    }

    override fun getToDeleteEpisodes(): List<Int> {
        return episodeDao.allToDeleteLocalEpisodes
    }

    override fun updateSaved(episodeId: Int, saved: Boolean) {
        episodeDao.updateSaved(episodeId, saved)
    }

    override fun updateSaved(episodeIds: Collection<Int>, saved: Boolean) {
        episodeDao.updateSaved(episodeIds, saved)
        if (saved) {
            failedEpisodesDao.deleteBulkPerId(episodeIds)
        }
    }

    override fun getAllToDownloads(): List<ToDownload> {
        return RoomConverter().convertRoomToDownload(toDownloadDao.all)
    }

    override fun removeToDownloads(toDownloads: Collection<ToDownload>) {
        for (toDownload in RoomConverter().convertToDownload(toDownloads)) {
            toDownloadDao.deleteToDownload(toDownload.mediumId, toDownload.listId, toDownload.externalListId)
        }
    }

    override fun getListItems(listId: Int): Collection<Int> {
        return mediaListDao.getListItems(listId)
    }

    override fun getLiveListItems(listId: Int): LiveData<List<Int>> {
        return mediaListDao.getLiveListItems(listId)
    }

    override fun getExternalListItems(externalListId: Int): Collection<Int> {
        return externalMediaListDao.getExternalListItems(externalListId)
    }

    override fun getLiveExternalListItems(externalListId: Int): LiveData<List<Int>> {
        return externalMediaListDao.getLiveExternalListItems(externalListId)
    }

    override fun getDownloadableEpisodes(mediumId: Int, limit: Int): List<Int> {
        return episodeDao.getDownloadableEpisodes(mediumId, limit)
    }

    override fun getDownloadableEpisodes(mediaIds: Collection<Int>): List<Int> {
        return episodeDao.getDownloadableEpisodes(mediaIds)
    }

    override fun getDisplayEpisodes(filter: EpisodeViewModel.Filter): LiveData<PagedList<DisplayRelease>> {
        val factory: DataSource.Factory<Int, DisplayRelease> = if (filter.latestOnly) episodeDao.getDisplayEpisodesLatestOnly(filter.saved, filter.read, filter.medium, filter.minIndex, filter.maxIndex, filter.filterListIds, filter.filterListIds.isEmpty()) else episodeDao.getDisplayEpisodes(filter.saved, filter.read, filter.medium, filter.minIndex, filter.maxIndex, filter.filterListIds, filter.filterListIds.isEmpty())
        return LivePagedListBuilder(factory, 50).build()
    }

    override fun getDisplayEpisodesGrouped(saved: Int, medium: Int): LiveData<PagedList<DisplayEpisode>> {
        val converter = RoomConverter()
        return LivePagedListBuilder(
                episodeDao.getDisplayEpisodesGrouped(saved, medium).map(Function { episode: RoomDisplayEpisode? -> converter.convertRoomEpisode(episode) }),
                50
        ).build()
    }

    override fun getLists(): LiveData<List<MediaList>> {
        val liveData = MediatorLiveData<List<MediaList>>()
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

    override fun insertDanglingMedia(mediaIds: MutableCollection<Int>) {
        val listMedia = mediaListDao.allLinkedMedia
        val externalListMedia = externalMediaListDao.allLinkedMedia
        mediaIds.removeAll(listMedia)
        mediaIds.removeAll(externalListMedia)
        if (mediaIds.isEmpty()) {
            return
        }
        val converter = RoomConverter()
        roomDanglingDao.insertBulk(converter.convertToDangling(mediaIds))
    }

    override fun removeDanglingMedia(mediaIds: Collection<Int>) {
        if (mediaIds.isEmpty()) {
            return
        }
        val converter = RoomConverter()
        roomDanglingDao.deleteBulk(converter.convertToDangling(mediaIds))
    }

    override fun getListSetting(id: Int, isExternal: Boolean): LiveData<out MediaListSetting> {
        return if (isExternal) {
            externalMediaListDao.getExternalListSetting(id)
        } else mediaListDao.getListSettings(id)
    }

    override fun getListSettingNow(id: Int, isExternal: Boolean): MediaListSetting {
        return if (isExternal) {
            externalMediaListDao.getExternalListSettingNow(id)
        } else mediaListDao.getListSettingsNow(id)
    }

    override fun updateToDownload(add: Boolean, toDownload: ToDownload) {
        if (add) {
            toDownloadDao.insert(RoomConverter().convert(toDownload))
        } else {
            toDownloadDao.deleteToDownload(
                    toDownload.mediumId,
                    toDownload.listId,
                    toDownload.externalListId
            )
        }
    }

    override fun getAllMedia(sortings: Sortings, title: String, medium: Int, author: String, lastUpdate: DateTime, minCountEpisodes: Int, minCountReadEpisodes: Int): LiveData<PagedList<MediumItem>> {
        var sortValue = sortings.sortValue
        return if (sortValue > 0) {
            LivePagedListBuilder(mediumDao.getAllAsc(sortValue, title, medium, author, lastUpdate, minCountEpisodes, minCountReadEpisodes), 50).build()
        } else {
            sortValue = -sortValue
            LivePagedListBuilder(mediumDao.getAllDesc(sortValue, title, medium, author, lastUpdate, minCountEpisodes, minCountReadEpisodes), 50).build()
        }
    }

    override fun getMediumSettings(mediumId: Int): LiveData<MediumSetting> {
        return mediumDao.getMediumSettings(mediumId)
    }

    override fun getMediumSettingsNow(mediumId: Int): MediumSetting {
        return mediumDao.getMediumSettingsNow(mediumId)
    }

    override fun getToc(mediumId: Int, sortings: Sortings, read: Byte, saved: Byte): LiveData<PagedList<TocEpisode>> {
        val episodes: DataSource.Factory<Int, RoomTocEpisode>
        episodes = if (sortings.sortValue > 0) {
            episodeDao.getTocEpisodesAsc(mediumId, read, saved)
        } else {
            episodeDao.getTocEpisodesDesc(mediumId, read, saved)
        }
        val converter = RoomConverter()
        return LivePagedListBuilder(episodes.map<TocEpisode>(Function { roomTocEpisode: RoomTocEpisode? -> converter.convertTocEpisode(roomTocEpisode) }), 50).build()
    }

    override fun getMediumItems(listId: Int, isExternal: Boolean): LiveData<List<MediumItem>> {
        return if (isExternal) {
            mediumDao.getExternalListMedia(listId)
        } else {
            mediumDao.getListMedia(listId)
        }
    }

    override fun listExists(listName: String): Boolean {
        return mediaListDao.listExists(listName)
    }

    override fun countSavedEpisodes(mediumId: Int): Int {
        return episodeDao.countSavedEpisodes(mediumId)
    }

    override fun getSavedEpisodes(mediumId: Int): List<Int> {
        return episodeDao.getSavedEpisodes(mediumId)
    }

    override fun getEpisode(episodeId: Int): Episode {
        val converter = RoomConverter()
        val roomEpisode = episodeDao.getEpisode(episodeId)
        return converter.convert(roomEpisode)
    }

    override fun getSimpleEpisodes(ids: Collection<Int>): List<SimpleEpisode> {
        return episodeDao.getSimpleEpisodes(ids)
    }

    override fun updateProgress(episodeIds: Collection<Int>, progress: Float) {
        episodeDao.updateProgress(episodeIds, progress, DateTime.now())
    }

    override fun getMediaInWaitBy(filter: String, mediumFilter: Int, hostFilter: String, sortings: Sortings): LiveData<PagedList<MediumInWait>> {
        val factory: DataSource.Factory<Int, RoomMediumInWait>
        var sortValue = sortings.sortValue
        if (sortValue < 0) {
            sortValue = -sortValue
            factory = mediumInWaitDao.getByDesc(sortValue, filter, mediumFilter, hostFilter)
        } else {
            factory = mediumInWaitDao.getByAsc(sortValue, filter, mediumFilter, hostFilter)
        }
        val converter = RoomConverter()
        return LivePagedListBuilder<Int, MediumInWait>(
                factory.map<MediumInWait>(Function { input: RoomMediumInWait? -> converter.convert(input) }),
                50
        ).build()
    }

    override fun getReadTodayEpisodes(): LiveData<PagedList<ReadEpisode>> {
        val converter = RoomConverter()
        return LivePagedListBuilder(
                episodeDao.readTodayEpisodes.map(Function { input: RoomReadEpisode? -> converter.convert(input) }),
                50
        ).build()
    }

    override fun getInternLists(): LiveData<List<MediaList>> {
        return mediaListDao.listViews
    }

    override fun addItemsToList(listId: Int, ids: Collection<Int>) {
        val joins: MutableList<MediaListMediaJoin> = ArrayList()
        for (id in ids) {
            joins.add(MediaListMediaJoin(listId, id))
        }
        mediaListDao.addJoin(joins)
    }

    override fun getSimilarMediaInWait(mediumInWait: MediumInWait): LiveData<List<MediumInWait>> {
        return mediumInWaitDao.getSimilar(mediumInWait.title, mediumInWait.medium)
    }

    override fun getMediaSuggestions(title: String, medium: Int): LiveData<List<SimpleMedium>> {
        return mediumDao.getSuggestions(title, medium)
    }

    override fun getMediaInWaitSuggestions(title: String, medium: Int): LiveData<List<MediumInWait>> {
        return mediumInWaitDao.getSuggestions(title, medium)
    }

    override fun getListSuggestion(name: String): LiveData<List<MediaList>> {
        return mediaListDao.getSuggestion(name)
    }

    override fun onDownloadAble(): LiveData<Boolean> {
        val previousDownloadCount = MutableLiveData<Int>()
        val previousEpisodeCount = MutableLiveData<Int>()
        val toDownloadCount = toDownloadDao.countMediaRows()
        val downloadableEpisodeCount = episodeDao.countDownloadableRows()
        val downloadAbles = MediatorLiveData<Boolean>()
        downloadAbles.addSource(toDownloadCount) { input: Int ->
            val previous = getOr(previousDownloadCount.value, 0)
            val current = getOr(input, 0)
            previousDownloadCount.value = current
            downloadAbles.postValue(current > previous)
        }
        downloadAbles.addSource(downloadableEpisodeCount) { input: Int ->
            val previous = getOr(previousEpisodeCount.value, 0)
            val current = getOr(input, 0)
            previousEpisodeCount.value = current
            downloadAbles.postValue(current > previous)
        }
        return downloadAbles
    }

    override fun clearMediaInWait() {
        mediumInWaitDao.clear()
    }

    override fun deleteMediaInWait(toDelete: Collection<MediumInWait>) {
        val converter = RoomConverter()
        mediumInWaitDao.deleteBulk(converter.convertMediaInWait(toDelete))
    }

    override fun getAllDanglingMedia(): LiveData<List<MediumItem>> {
        return roomDanglingDao.all
    }

    override fun removeItemFromList(listId: Int, mediumId: Int) {
        mediaListDao.removeJoin(MediaListMediaJoin(listId, mediumId))
    }

    override fun removeItemFromList(listId: Int, mediumId: Collection<Int>) {
        mediaListDao.removeJoin(listId, mediumId)
    }

    override fun moveItemsToList(oldListId: Int, newListId: Int, ids: Collection<Int>) {
        val oldJoins: MutableCollection<MediaListMediaJoin> = ArrayList()
        val newJoins: MutableCollection<MediaListMediaJoin> = ArrayList()
        for (id in ids) {
            oldJoins.add(MediaListMediaJoin(oldListId, id))
            newJoins.add(MediaListMediaJoin(newListId, id))
        }
        mediaListDao.moveJoins(oldJoins, newJoins)
    }

    override fun getExternalUser(): LiveData<PagedList<ExternalUser>> {
        return LivePagedListBuilder(externalUserDao.all, 50).build()
    }

    override fun getSpaceMedium(mediumId: Int): SpaceMedium {
        return mediumDao.getSpaceMedium(mediumId)
    }

    override fun getMediumType(mediumId: Int): Int {
        return mediumDao.getMediumType(mediumId)
    }

    override fun getReleaseLinks(episodeId: Int): List<String> {
        return episodeDao.getReleaseLinks(episodeId)
    }

    override fun clearLocalMediaData() {
        failedEpisodesDao.clearAll()
        episodeDao.clearAllReleases()
        episodeDao.clearAll()
        partDao.clearAll()
        externalMediaListDao.clearJoins()
        mediaListDao.clearJoins()
        clearMediaInWait()
    }

    override fun getNotifications(): LiveData<PagedList<NotificationItem>> {
        return LivePagedListBuilder(notificationDao.notifications, 50).build()
    }

    override fun updateFailedDownload(episodeId: Int) {
        val failedEpisode = failedEpisodesDao.getFailedEpisode(episodeId)
        var failedCount = 0
        if (failedEpisode != null) {
            failedCount = failedEpisode.failCount
        }
        failedCount++
        failedEpisodesDao.insert(RoomFailedEpisode(episodeId, failedCount))
    }

    override fun getFailedEpisodes(episodeIds: Collection<Int>): List<FailedEpisode> {
        return failedEpisodesDao.getFailedEpisodes(episodeIds)
    }

    override fun addNotification(notification: NotificationItem) {
        if (notification == null) {
            return
        }
        notificationDao.insert(RoomNotification(
                notification.title,
                notification.description,
                notification.dateTime
        ))
    }

    override fun getSimpleEpisode(episodeId: Int): SimpleEpisode {
        return episodeDao.getSimpleEpisode(episodeId)
    }

    override fun getSimpleMedium(mediumId: Int): SimpleMedium {
        return mediumDao.getSimpleMedium(mediumId)
    }

    override fun clearNotifications() {
        notificationDao.deleteAll()
    }

    override fun clearFailEpisodes() {
        failedEpisodesDao.clearAll()
    }

    override fun getAllEpisodes(mediumId: Int): Collection<Int> {
        return episodeDao.getAllEpisodes(mediumId)
    }

    override fun getSavedEpisodeIdsWithLowerIndex(combiIndex: Double, mediumId: Int): Collection<Int> {
        return episodeDao.getSavedEpisodeIdsWithLowerIndex(mediumId, combiIndex)
    }

    override fun removeEpisodes(episodeIds: List<Int>) {
        episodeDao.deletePerId(episodeIds)
    }

    override fun removeParts(partIds: Collection<Int>) {
        partDao.deletePerId(partIds)
    }

    override fun insertEditEvent(event: EditEvent) {
        val converter = RoomConverter()
        val roomEditEvent = converter.convert(event)
        editDao.insert(roomEditEvent)
    }

    override fun insertEditEvent(events: Collection<EditEvent>) {
        val converter = RoomConverter()
        val roomEditEvent = converter.convertEditEvents(events)
        editDao.insertBulk(roomEditEvent)
    }

    override fun getReadEpisodes(episodeIds: Collection<Int>, read: Boolean): List<Int> {
        return episodeDao.getReadEpisodes(episodeIds, read)
    }

    override fun getEditEvents(): List<EditEvent> {
        return editDao.all
    }

    override fun removeEditEvents(editEvents: Collection<EditEvent>) {
        val converter = RoomConverter()
        editDao.deleteBulk(converter.convertEditEvents(editEvents))
    }

    override fun checkReload(parsedStat: ParsedStat): ReloadStat {
        val roomStats = episodeDao.stat
        val partStats: MutableMap<Int, Partstat> = HashMap()
        for (value in parsedStat.media.values) {
            partStats.putAll(value!!)
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
                    || partstat.episodeSum != localStat.episodeSum) {
                loadEpisode.add(localStat.partId)
            } else if (partstat.releaseCount != localStat.releaseCount) {
                loadRelease.add(localStat.partId)
            }
        }
        val loadPart: MutableSet<Int> = HashSet()
        val loadData = this.loadData
        for ((partId, remotePartStat) in partStats) {
            val localPartStat = localStatMap[partId]
            if (localPartStat == null) {
                if (!loadData.part.contains(partId)) {
                    loadPart.add(partId)
                }
                continue
            }
            if (remotePartStat.episodeCount != localPartStat.episodeCount
                    || remotePartStat.episodeSum != localPartStat.episodeSum) {
                loadEpisode.add(localPartStat.partId)
            } else if (remotePartStat.releaseCount != localPartStat.releaseCount) {
                loadRelease.add(localPartStat.partId)
            }
        }
        val roomTocStats = tocDao.stat
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
        return ReloadStat(loadEpisode, loadRelease, loadMediumTocs, missingMedia, loadPart, missingLists, loadUser)
    }

    override fun syncProgress() {
        val all = mediumProgressDao.comparison
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
                    Utils.doPartitionedEx(episodeIds) { ids: List<Int?>? ->
                        episodeDao.updateProgress(ids, 1f, DateTime.now())
                        false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun updateDataStructure(mediaIds: List<Int>, partIds: List<Int>) {
        for (mediumId in mediaIds) {
            val mediumPartIds = dataStructureDao.getPartJoin(mediumId)
            val availablePartIds = partDao.getPartsIds(mediumId)
            availablePartIds.removeAll(mediumPartIds)
            if (!availablePartIds.isEmpty()) {
                partDao.deletePerId(availablePartIds)
            }
        }
        for (partId in partIds) {
            val episodePartIds = dataStructureDao.getEpisodeJoin(partId)
            val availableEpisodeIds = episodeDao.getEpisodeIds(partId)
            availableEpisodeIds.removeAll(episodePartIds)
            if (!availableEpisodeIds.isEmpty()) {
                try {
                    Utils.doPartitionedEx(availableEpisodeIds) { ids: List<Int>? ->
                        episodeDao.deletePerId(ids)
                        true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun getEpisodeIdsWithHigherIndex(combiIndex: Double, mediumId: Int, read: Boolean): List<Int> {
        return episodeDao.getEpisodeIdsWithHigherIndex(mediumId, combiIndex, read)
    }

    override fun getEpisodeIdsWithHigherIndex(combiIndex: Double, mediumId: Int): List<Int> {
        return episodeDao.getEpisodeIdsWithHigherIndex(mediumId, combiIndex)
    }

    override fun getEpisodeIdsWithLowerIndex(combiIndex: Double, mediumId: Int, read: Boolean): List<Int> {
        return episodeDao.getEpisodeIdsWithLowerIndex(mediumId, combiIndex, read)
    }

    override fun getEpisodeIdsWithLowerIndex(combiIndex: Double, mediumId: Int): List<Int> {
        return episodeDao.getEpisodeIdsWithLowerIndex(mediumId, combiIndex)
    }

    override fun getSavedEpisodeIdsWithHigherIndex(combiIndex: Double, mediumId: Int): Collection<Int> {
        return episodeDao.getSavedEpisodeIdsWithHigherIndex(mediumId, combiIndex)
    }

    private fun <E> getOr(value: E?, defaultValue: E): E {
        return value ?: defaultValue
    }

    private inner class RoomDependantGenerator(private val loadedData: LoadData) : DependantGenerator {
        override fun generateReadEpisodesDependant(readEpisodes: FilteredReadEpisodes): Collection<DependencyTask<*>> {
            val tasks: MutableSet<DependencyTask<*>> = HashSet()
            val worker = LoadWorker.getWorker()
            for (dependency in readEpisodes.dependencies) {
                tasks.add(DependencyTask(
                        dependency.id,
                        DependantValue(dependency.dependency),
                        worker.EPISODE_LOADER
                ))
            }
            return tasks
        }

        override fun generatePartsDependant(parts: FilteredParts): Collection<DependencyTask<*>> {
            val tasks: MutableSet<DependencyTask<*>> = HashSet()
            val worker = LoadWorker.getWorker()
            for (dependency in parts.mediumDependencies) {
                tasks.add(DependencyTask(
                        dependency.id,
                        DependantValue(
                                dependency.dependency,
                                dependency.dependency.id,
                                worker.PART_LOADER
                        ),
                        worker.MEDIUM_LOADER
                ))
            }
            return tasks
        }

        override fun generateEpisodesDependant(episodes: FilteredEpisodes): Collection<DependencyTask<*>> {
            val tasks: MutableSet<DependencyTask<*>> = HashSet()
            val worker = LoadWorker.getWorker()
            for (dependency in episodes.partDependencies) {
                tasks.add(DependencyTask(
                        dependency.id,
                        DependantValue(
                                dependency.dependency,
                                dependency.dependency.id,
                                worker.EPISODE_LOADER
                        ),
                        worker.PART_LOADER
                ))
            }
            return tasks
        }

        override fun generateMediaDependant(media: FilteredMedia): Collection<DependencyTask<*>> {
            val tasks: MutableSet<DependencyTask<*>> = HashSet()
            val worker = LoadWorker.getWorker()
            for (dependency in media.episodeDependencies) {
                tasks.add(DependencyTask(
                        dependency.id,
                        DependantValue(
                                dependency.dependency,
                                dependency.dependency.id,
                                worker.MEDIUM_LOADER
                        ),
                        worker.EPISODE_LOADER
                ))
            }
            for (unloadedPart in media.unloadedParts) {
                tasks.add(DependencyTask(unloadedPart, null, worker.PART_LOADER))
            }
            return tasks
        }

        override fun generateMediaListsDependant(mediaLists: FilteredMediaList): Collection<DependencyTask<*>> {
            val tasks: MutableSet<DependencyTask<*>> = HashSet()
            val worker = LoadWorker.getWorker()
            val converter = RoomConverter(loadedData)
            for (dependency in mediaLists.mediumDependencies) {
                var tmpListId = 0
                if (!dependency.dependency.isEmpty()) {
                    tmpListId = dependency.dependency[0].listId
                }
                val listId = tmpListId
                tasks.add(DependencyTask(
                        dependency.id,
                        DependantValue(
                                converter.convertListJoin(dependency.dependency)
                        ) { mediaListDao.clearJoin(listId) },
                        worker.MEDIUM_LOADER
                ))
            }
            return tasks
        }

        override fun generateExternalMediaListsDependant(externalMediaLists: FilteredExtMediaList): Collection<DependencyTask<*>> {
            val tasks: MutableSet<DependencyTask<*>> = HashSet()
            val worker = LoadWorker.getWorker()
            val converter = RoomConverter(loadedData)
            for (dependency in externalMediaLists.mediumDependencies) {
                var tmpListId = 0
                if (!dependency.dependency.isEmpty()) {
                    tmpListId = dependency.dependency[0].listId
                }
                val listId = tmpListId
                tasks.add(DependencyTask(
                        dependency.id,
                        DependantValue(
                                converter.convertExListJoin(dependency.dependency)
                        ) { externalMediaListDao.clearJoin(listId) },
                        worker.MEDIUM_LOADER
                ))
            }
            for (dependency in externalMediaLists.userDependencies) {
                tasks.add(DependencyTask(
                        dependency.id,
                        DependantValue(
                                converter.convert(dependency.dependency),
                                dependency.dependency.id,
                                worker.EXTERNAL_MEDIALIST_LOADER
                        ),
                        worker.EXTERNAL_USER_LOADER
                ))
            }
            return tasks
        }

        override fun generateExternalUsersDependant(externalUsers: FilteredExternalUser): Collection<DependencyTask<*>> {
            val tasks: MutableSet<DependencyTask<*>> = HashSet()
            val worker = LoadWorker.getWorker()
            val converter = RoomConverter(loadedData)
            for (dependency in externalUsers.mediumDependencies) {
                var tmpListId = 0
                if (!dependency.dependency.isEmpty()) {
                    tmpListId = dependency.dependency[0].listId
                }
                val listId = tmpListId
                tasks.add(DependencyTask(
                        dependency.id,
                        DependantValue(
                                converter.convertExListJoin(dependency.dependency)
                        ) { externalMediaListDao.clearJoin(listId) },
                        worker.MEDIUM_LOADER
                ))
            }
            return tasks
        }
    }

    private inner class RoomPersister internal constructor(private val loadedData: LoadData) : ClientModelPersister {
        private val generator: LoadWorkGenerator
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
            persistReleases(filteredEpisodes.releases.stream().map { value: ClientEpisodeRelease ->
                ClientRelease(
                        value.episodeId,
                        value.title,
                        value.url,
                        value.isLocked,
                        value.releaseDate
                )
            }.collect(Collectors.toList()))
            return this
        }

        override fun persistMediaLists(mediaLists: List<ClientMediaList>): ClientModelPersister {
            val filteredMediaList = generator.filterMediaLists(mediaLists)
            val converter = RoomConverter(loadedData)
            return this.persist(filteredMediaList, converter)
        }

        override fun persistUserLists(mediaLists: List<ClientUserList>): ClientModelPersister {
            val uuid = userNow.uuid
            return persistMediaLists(mediaLists.stream().map { value: ClientUserList ->
                ClientMediaList(
                        uuid,
                        value.id,
                        value.name,
                        value.medium,
                        null
                )
            }.collect(Collectors.toList()))
        }

        override fun persist(filteredMediaList: FilteredMediaList): ClientModelPersister {
            return this.persist(filteredMediaList, RoomConverter(loadedData))
        }

        private fun persist(filteredMediaList: FilteredMediaList, converter: RoomConverter): ClientModelPersister {
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

        private fun persist(filteredExtMediaList: FilteredExtMediaList, converter: RoomConverter): ClientModelPersister {
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

        private fun persist(filteredExternalUser: FilteredExternalUser, converter: RoomConverter): ClientModelPersister {
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
            mediumDao.insertBulk(newMedia)
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

        override fun persist(filteredReadEpisodes: FilteredReadEpisodes): ClientModelPersister {
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
            this.persistMedia(query.media.asList().mapNotNull { medium: ClientMedium? -> ClientSimpleMedium(medium) })
            this.persist(query.list)
            return this
        }

        override fun persist(query: ClientMultiListQuery): ClientModelPersister {
            this.persistMedia(query.media.asList().mapNotNull { medium: ClientMedium? -> ClientSimpleMedium(medium) })
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

        override fun persist(toDownload: ToDownload): ClientModelPersister {
            toDownloadDao.insert(RoomConverter().convert(toDownload))
            return this
        }

        override fun persistMediaInWait(medium: List<ClientMediumInWait>) {
            mediumInWaitDao.insertBulk(RoomConverter().convertClientMediaInWait(medium))
        }

        override fun persist(user: ClientSimpleUser): ClientModelPersister {
            // short cut version
            if (user == null) {
                deleteAllUser()
                return this
            }
            val converter = RoomConverter()
            val newRoomUser = converter.convert(user)
            val currentUser = userDao.userNow
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

        override fun deleteLeftoverEpisodes(partEpisodes: Map<Int, List<Int>>) {
            val partIds = partEpisodes.keys
            val episodes = episodeDao.getEpisodes(partIds)
            val deleteEpisodes: MutableList<Int> = LinkedList()
            episodes.forEach(Consumer { roomPartEpisode: RoomPartEpisode ->
                val episodeIds = partEpisodes[roomPartEpisode.partId]
                if (episodeIds == null || !episodeIds.contains(roomPartEpisode.episodeId)) {
                    deleteEpisodes.add(roomPartEpisode.episodeId)
                }
            })
            Utils.doPartitioned(deleteEpisodes) { ids: List<Int>? ->
                episodeDao.deletePerId(ids)
                false
            }
        }

        override fun deleteLeftoverReleases(partReleases: Map<Int, List<ClientSimpleRelease>>): Collection<Int> {
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

        override fun deleteLeftoverTocs(mediaTocs: Map<Int, List<String>>) {
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

        override fun persist(clientUser: ClientUser): ClientModelPersister {
            // short cut version
            if (clientUser == null) {
                deleteAllUser()
                return this
            }
            val converter = RoomConverter()
            val newRoomUser = converter.convert(clientUser)
            val currentUser = userDao.userNow
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
            val listUser = externalMediaListDao.listUser
            val deletedLists: MutableSet<Int> = HashSet()
            val deletedExLists: MutableSet<Int> = HashSet()
            val deletedExUser: MutableSet<String> = HashSet()
            val newInternalJoins: MutableList<MediaListMediaJoin> = LinkedList()
            val toDeleteInternalJoins = filterListMediumJoins(stat, deletedLists, newInternalJoins, false)
            val newExternalJoins: MutableList<ExternalListMediaJoin> = LinkedList()
            val toDeleteExternalJoins = filterListMediumJoins(stat, deletedExLists, newExternalJoins, true)

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

        private fun <T : ListMediaJoin?> filterListMediumJoins(stat: ParsedStat, deletedLists: MutableSet<Int>, newJoins: MutableList<T>, external: Boolean): List<T> {
            val previousListJoins: MutableList<T>
            val currentJoins: Map<Int, List<Int>>
            if (external) {
                currentJoins = stat.extLists
                previousListJoins = externalMediaListDao.listItems as MutableList<T>
            } else {
                currentJoins = stat.lists
                previousListJoins = mediaListDao.listItems as MutableList<T>
            }
            val previousListJoinMap: MutableMap<Int, MutableSet<Int>> = HashMap()
            previousListJoins.removeIf { join: T ->
                previousListJoinMap.computeIfAbsent(join!!.listId) { integer: Int? -> HashSet() }.add(join.mediumId)
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

        init {
            generator = LoadWorkGenerator(loadedData)
        }
    }

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
}