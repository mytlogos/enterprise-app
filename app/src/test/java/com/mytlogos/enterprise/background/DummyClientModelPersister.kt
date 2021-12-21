package com.mytlogos.enterprise.background

import com.mytlogos.enterprise.background.api.model.*
import com.mytlogos.enterprise.background.api.model.ClientStat.ParsedStat
import com.mytlogos.enterprise.background.resourceLoader.LoadWorkGenerator
import com.mytlogos.enterprise.background.resourceLoader.LoadWorkGenerator.*
import com.mytlogos.enterprise.background.room.model.RoomExternalMediaList.ExternalListMediaJoin
import com.mytlogos.enterprise.background.room.model.RoomMediaList.MediaListMediaJoin
import com.mytlogos.enterprise.background.room.model.RoomNews
import com.mytlogos.enterprise.background.room.model.RoomToDownload
import com.mytlogos.enterprise.model.ToDownload
import com.mytlogos.enterprise.model.Toc
import java.util.*
import java.util.stream.Collectors

class DummyClientModelPersister(val loadedData: LoadData, val repository: Repository) :
    ClientModelPersister {

    val updatedData: LoadData = LoadData()
    val deletedData: LoadData = LoadData()
    val generator: LoadWorkGenerator = LoadWorkGenerator(loadedData)
    var isUserUpdated = false
        private set
    var isUserDeleted = false
        private set
    var isUserInserted = false
        private set
    private val externalListMediaJoins: MutableList<ExternalListMediaJoin> = ArrayList()
    val clearedExternalListMediaJoins: List<ExternalListMediaJoin> = ArrayList()
    private val clearedExternalList: MutableList<Int> = ArrayList()
    private val listMediaJoins: MutableList<MediaListMediaJoin> = ArrayList()
    val clearedListMediaJoins: List<MediaListMediaJoin> = ArrayList()
    private val clearedListMedia: MutableList<Int> = ArrayList()
    private val savedReleases: MutableList<ClientEpisodeRelease> = ArrayList()
    private val savedToDownloads: MutableList<RoomToDownload> = ArrayList()

    fun persistEpisodes(episodes: Collection<ClientEpisode?>?): ClientModelPersister {
        // TODO: implement dummy
        return this
    }

    fun persistReleases(releases: Collection<ClientRelease?>?): ClientModelPersister? {
        return null
    }

    override suspend fun persist(filteredEpisodes: FilteredEpisodes): ClientModelPersister {
        val converter = RoomConverter(loadedData)
        val list = converter.convertEpisodes(filteredEpisodes.newEpisodes)
        val update = converter.convertEpisodes(filteredEpisodes.updateEpisodes)
        for ((episodeId) in list) {
            loadedData.episodes.add(episodeId)
        }
        for ((episodeId) in update) {
            updatedData.episodes.add(episodeId)
        }
        savedReleases.addAll(filteredEpisodes.releases)
        return this
    }

    fun persistMediaLists(mediaLists: List<ClientMediaList>?): ClientModelPersister {
        return this
    }

    override suspend fun persistUserLists(mediaLists: List<ClientUserList>): ClientModelPersister {
        val uuid = repository.user.value!!.uuid
        return this.persistMediaLists(mediaLists.stream().map { value: ClientUserList ->
            ClientMediaList(
                uuid,
                value.id,
                value.name,
                value.medium,
                null
            )
        }.collect(Collectors.toList()))
    }

    override suspend fun persistExternalMediaLists(externalMediaLists: Collection<ClientExternalMediaList>): ClientModelPersister {
        TODO("Not yet implemented")
    }

    override suspend fun persistExternalUsers(externalUsers: List<ClientExternalUser>): ClientModelPersister {
        TODO("Not yet implemented")
    }

    override suspend fun persistMedia(media: Collection<ClientSimpleMedium>): ClientModelPersister {
        TODO("Not yet implemented")
    }

    override suspend fun persist(filteredMediaList: FilteredMediaList): ClientModelPersister {
        val converter = RoomConverter(loadedData)
        val list = converter.convertMediaList(filteredMediaList.newList)
        val update = converter.convertMediaList(filteredMediaList.updateList)
        val joins = converter.convertListJoin(filteredMediaList.joins)
        val clearListJoin: List<Int> = filteredMediaList.clearJoins
        for (mediaList in update) {
            updatedData.mediaList.add(mediaList.listId)
        }
        clearedListMedia.addAll(clearListJoin)
        // then add all up-to-date joins
        listMediaJoins.addAll(joins)
        for (mediaList in list) {
            loadedData.mediaList.add(mediaList.listId)
        }
        return this
    }

    fun persistExternalMediaLists(externalMediaLists: Collection<ClientExternalMediaList?>?): ClientModelPersister {
        return this
    }

    override suspend fun persist(filteredExtMediaList: FilteredExtMediaList): ClientModelPersister {
        val converter = RoomConverter(loadedData)
        val list = converter.convertExternalMediaList(filteredExtMediaList.newList)
        val update = converter.convertExternalMediaList(filteredExtMediaList.updateList)
        val joins = converter.convertExListJoin(filteredExtMediaList.joins)
        val clearListJoin: List<Int> = filteredExtMediaList.clearJoins
        clearedExternalList.addAll(clearListJoin)
        externalListMediaJoins.addAll(joins)
        for ((_, externalListId) in list) {
            loadedData.externalMediaList.add(externalListId)
        }
        for ((_, externalListId) in update) {
            updatedData.externalMediaList.add(externalListId)
        }
        return this
    }

    fun persistExternalUsers(externalUsers: List<ClientExternalUser?>?): ClientModelPersister {
        return this
    }

    override suspend fun persist(filteredExternalUser: FilteredExternalUser): ClientModelPersister {
        val converter = RoomConverter(loadedData)
        val list = converter.convertExternalUser(filteredExternalUser.newUser)
        val update = converter.convertExternalUser(filteredExternalUser.updateUser)
        val externalMediaLists = converter.convertExternalMediaList(filteredExternalUser.newList)
        val updateExternalMediaLists =
            converter.convertExternalMediaList(filteredExternalUser.updateList)
        val extListMediaJoin = converter.convertExListJoin(filteredExternalUser.joins)
        val clearListJoin: List<Int> = filteredExternalUser.clearJoins
        clearedExternalList.addAll(clearListJoin)
        externalListMediaJoins.addAll(extListMediaJoin)
        for ((uuid) in list) {
            loadedData.externalUser.add(uuid)
        }
        for ((uuid) in update) {
            updatedData.externalUser.add(uuid)
        }
        for ((_, externalListId) in externalMediaLists) {
            loadedData.externalMediaList.add(externalListId)
        }
        for ((_, externalListId) in updateExternalMediaLists) {
            updatedData.externalMediaList.add(externalListId)
        }
        return this
    }

    fun persistMedia(media: Collection<ClientSimpleMedium?>?): ClientModelPersister {
        return this
    }

    override suspend fun persist(filteredMedia: FilteredMedia): ClientModelPersister {
        val converter = RoomConverter(loadedData)
        val list = converter.convertSimpleMedia(filteredMedia.newMedia)
        val update = converter.convertSimpleMedia(filteredMedia.updateMedia)
        for (medium in list) {
            loadedData.media.add(medium.mediumId)
        }
        for (medium in update) {
            updatedData.media.add(medium.mediumId)
        }
        return this
    }

    override suspend fun persistNews(news: Collection<ClientNews>): ClientModelPersister {
        val list: MutableList<RoomNews> = ArrayList()
        val update: MutableList<RoomNews> = ArrayList()
        val converter = RoomConverter()
        for (clientNews in news) {
            val roomNews = converter.convert(clientNews)
            if (generator.isNewsLoaded(clientNews.id)) {
                update.add(roomNews)
            } else {
                list.add(roomNews)
            }
        }
        for ((newsId) in list) {
            loadedData.news.add(newsId)
        }
        for ((newsId) in update) {
            updatedData.news.add(newsId)
        }
        return this
    }

    override suspend fun persistParts(parts: Collection<ClientPart>): ClientModelPersister {
        TODO("Not yet implemented")
    }

    override suspend fun persistToDownloads(toDownloads: Collection<ToDownload>): ClientModelPersister {
        TODO("Not yet implemented")
    }

    override suspend fun persistReadEpisodes(readEpisodes: Collection<ClientReadEpisode>): ClientModelPersister {
        TODO("Not yet implemented")
    }

    fun persistParts(parts: Collection<ClientPart?>?): ClientModelPersister {
        return this
    }

    override suspend fun persist(filteredParts: FilteredParts): ClientModelPersister {
        val converter = RoomConverter()
        val list = converter.convertParts(filteredParts.newParts)
        val update = converter.convertParts(filteredParts.updateParts)
        for (part in list) {
            loadedData.part.add(part.partId)
        }
        for (part in update) {
            updatedData.part.add(part.partId)
        }
        this.persistEpisodes(filteredParts.episodes)
        return this
    }

    override suspend fun persist(stat: ParsedStat): ClientModelPersister {
        TODO("Not yet implemented")
    }

    override suspend fun persist(toDownload: ToDownload): ClientModelPersister {
        TODO("Not yet implemented")
    }

    override suspend fun persist(filteredReadEpisodes: FilteredReadEpisodes): ClientModelPersister {
        for ((episodeId) in filteredReadEpisodes.episodeList) {
            updatedData.episodes.add(episodeId)
        }
        return this
    }

    fun persistReadEpisodes(readEpisodes: Collection<ClientReadEpisode?>?): ClientModelPersister {
        return this
    }

    fun persist(stat: ParsedStat?): ClientModelPersister {
        return this
    }

    override suspend fun persist(query: ClientListQuery): ClientModelPersister {
        return this
    }

    override suspend fun persist(query: ClientMultiListQuery): ClientModelPersister {
        return this
    }

    fun persistToDownloads(toDownloads: Collection<ToDownload?>?): ClientModelPersister {
        return this
    }

    fun persist(user: ClientUpdateUser?): ClientModelPersister {
        isUserUpdated = true
        return this
    }

    fun persist(toDownload: ToDownload?): ClientModelPersister {
        savedToDownloads.add(RoomConverter().convert(toDownload!!))
        return this
    }

    fun persistMediaInWait(medium: List<ClientMediumInWait?>?) {}

    override suspend fun persist(user: ClientSimpleUser?): ClientModelPersister {
        return this
    }

    override suspend fun persistEpisodes(episodes: Collection<ClientEpisode>): ClientModelPersister {
        TODO("Not yet implemented")
    }

    override suspend fun persistReleases(releases: Collection<ClientRelease>): ClientModelPersister {
        TODO("Not yet implemented")
    }

    override suspend fun persistMediaLists(mediaLists: List<ClientMediaList>): ClientModelPersister {
        TODO("Not yet implemented")
    }

    fun deleteLeftoverEpisodes(partEpisodes: Map<Int?, List<Int?>?>?) {}
    fun deleteLeftoverReleases(partReleases: Map<Int?, List<ClientSimpleRelease?>?>?): Collection<Int>? {
        return null
    }

    fun deleteLeftoverTocs(mediaTocs: Map<Int?, List<String?>?>?) {}

    fun persistTocs(tocs: Collection<Toc?>?): ClientModelPersister? {
        return null
    }

    override suspend fun persist(clientUser: ClientUser?): ClientModelPersister {
        // short cut version
        if (clientUser == null) {
            isUserDeleted = true
            return this
        }
        isUserInserted = true
        return this
    }

    override suspend fun persist(user: ClientUpdateUser): ClientModelPersister {
        TODO("Not yet implemented")
    }

    override fun finish() {

    }

    override suspend fun persistMediaInWait(medium: List<ClientMediumInWait>) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteLeftoverEpisodes(partEpisodes: Map<Int, List<Int>>) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteLeftoverReleases(partReleases: Map<Int, List<ClientSimpleRelease>>): Collection<Int> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteLeftoverTocs(mediaTocs: Map<Int, List<String>>) {
        TODO("Not yet implemented")
    }

    override suspend fun persistTocs(tocs: Collection<Toc>): ClientModelPersister {
        TODO("Not yet implemented")
    }

    fun getExternalListMediaJoins(): List<ExternalListMediaJoin> {
        return externalListMediaJoins
    }

    fun getClearedExternalList(): List<Int> {
        return clearedExternalList
    }

    fun getListMediaJoins(): List<MediaListMediaJoin> {
        return listMediaJoins
    }

    fun getClearedListMedia(): List<Int> {
        return clearedListMedia
    }

    fun getSavedReleases(): List<ClientEpisodeRelease> {
        return savedReleases
    }

    fun getSavedToDownloads(): List<RoomToDownload> {
        return savedToDownloads
    }

}