package com.mytlogos.enterprise.background.resourceLoader

import com.mytlogos.enterprise.background.LoadData
import com.mytlogos.enterprise.background.api.model.*
import java.util.*

class LoadWorkGenerator(private val loadedData: LoadData) {
    fun filterReadEpisodes(readEpisodes: Collection<ClientReadEpisode>): FilteredReadEpisodes {
        val container = FilteredReadEpisodes()
        for (readEpisode in readEpisodes) {
            val episodeId = readEpisode.episodeId
            if (isEpisodeLoaded(episodeId)) {
                container.episodeList.add(readEpisode)
            } else {
                container.dependencies.add(IntDependency(episodeId, readEpisode))
            }
        }
        return container
    }

    fun filterParts(parts: Collection<ClientPart>): FilteredParts {
        val episodes: MutableList<ClientEpisode> = ArrayList()
        val filteredParts = FilteredParts()
        for (part in parts) {
            if (isMediumLoaded(part.mediumId)) {
                if (isPartLoaded(part.id)) {
                    filteredParts.updateParts.add(part)
                } else {
                    filteredParts.newParts.add(part)
                }
                if (part.episodes != null) {
                    Collections.addAll(episodes, *part.episodes)
                }
            } else {
                filteredParts.mediumDependencies.add(IntDependency(part.mediumId, part))
            }
        }
        filteredParts.episodes.addAll(episodes)
        return filteredParts
    }

    fun filterEpisodes(episodes: Collection<ClientEpisode>): FilteredEpisodes {
        val filteredEpisodes = FilteredEpisodes()
        for (episode in episodes) {
            val partId = episode.partId
            if (!isPartLoaded(partId)) {
                filteredEpisodes.partDependencies.add(IntDependency(partId, episode))
                continue
            }
            if (isEpisodeLoaded(episode.id)) {
                filteredEpisodes.updateEpisodes.add(episode)
            } else {
                filteredEpisodes.newEpisodes.add(episode)
            }
            if (episode.releases != null) {
                Collections.addAll(filteredEpisodes.releases, *episode.releases)
            }
        }
        return filteredEpisodes
    }

    fun filterMedia(media: Collection<ClientMedium?>): FilteredMedia {
        val filteredMedia = FilteredMedia()
        for (medium in media) {
            val currentRead = medium!!.currentRead

            // id can never be zero
            if (!isEpisodeLoaded(currentRead) && currentRead > 0) {
                filteredMedia.episodeDependencies.add(IntDependency(currentRead, medium))
            }
            if (isMediumLoaded(medium.id)) {
                filteredMedia.updateMedia.add(ClientSimpleMedium(medium))
            } else {
                filteredMedia.newMedia.add(ClientSimpleMedium(medium))
            }
            if (medium.parts != null) {
                for (part in medium.parts!!) {
                    // todo check if it should be checked that medium is loaded
                    if (!isPartLoaded(part)) {
                        filteredMedia.unloadedParts.add(part)
                    }
                }
            }
        }
        return filteredMedia
    }

    fun filterSimpleMedia(media: Collection<ClientSimpleMedium>): FilteredMedia {
        val filteredMedia = FilteredMedia()
        for (medium in media) {
            if (isMediumLoaded(medium.id)) {
                filteredMedia.updateMedia.add(medium)
            } else {
                filteredMedia.newMedia.add(medium)
            }
        }
        return filteredMedia
    }

    fun filterMediaLists(mediaLists: Collection<ClientMediaList>): FilteredMediaList {
        val filteredMediaList = FilteredMediaList()
        for (mediaList in mediaLists) {
            if (isMediaListLoaded(mediaList.id)) {
                filteredMediaList.updateList.add(mediaList)
            } else {
                filteredMediaList.newList.add(mediaList)
            }
            val missingMedia: MutableSet<Int> = HashSet()
            val currentJoins: MutableList<ListJoin> = ArrayList()
            if (mediaList.items != null) {
                for (item in mediaList.items) {
                    val join = ListJoin(mediaList.id, item)
                    if (!isMediumLoaded(item)) {
                        missingMedia.add(item)
                    }
                    currentJoins.add(join)
                }
            }

            // if none medium is missing, just clear and add like normal
            if (missingMedia.isEmpty()) {
                filteredMediaList.joins.addAll(currentJoins)
                filteredMediaList.clearJoins.add(mediaList.id)
            } else {
                // else load missing media with worker and clear and add afterwards
                for (mediumId in missingMedia) {
                    filteredMediaList.mediumDependencies.add(IntDependency(mediumId, currentJoins))
                }
            }
        }
        return filteredMediaList
    }

    fun filterExternalMediaLists(externalMediaLists: Collection<ClientExternalMediaList?>): FilteredExtMediaList {
        val filteredExtMediaList = FilteredExtMediaList()
        for (externalMediaList in externalMediaLists) {
            val externalUuid = externalMediaList!!.uuid
            if (!isExternalUserLoaded(externalUuid)) {
                filteredExtMediaList.userDependencies.add(
                    Dependency(
                        externalUuid,
                        externalMediaList
                    )
                )
                continue
            }
            if (isExternalMediaListLoaded(externalMediaList.id)) {
                filteredExtMediaList.updateList.add(externalMediaList)
            } else {
                filteredExtMediaList.newList.add(externalMediaList)
            }
            val missingMedia: MutableSet<Int> = HashSet()
            val currentJoins: MutableList<ListJoin> = ArrayList()
            if (externalMediaList.items != null) {
                for (item in externalMediaList.items) {
                    val join = ListJoin(externalMediaList.id, item)
                    if (!isMediumLoaded(item)) {
                        missingMedia.add(item)
                    }
                    currentJoins.add(join)
                }
            }

            // if none medium is missing, just clear and add like normal
            if (missingMedia.isEmpty()) {
                filteredExtMediaList.joins.addAll(currentJoins)
                filteredExtMediaList.clearJoins.add(externalMediaList.id)
            } else {
                // else load missing media with worker and clear and add afterwards
                for (mediumId in missingMedia) {
                    filteredExtMediaList.mediumDependencies.add(
                        IntDependency(
                            mediumId,
                            currentJoins
                        )
                    )
                }
            }
        }
        return filteredExtMediaList
    }

    fun filterExternalUsers(externalUsers: Collection<ClientExternalUser?>): FilteredExternalUser {
        val filteredExternalUser = FilteredExternalUser()
        for (externalUser in externalUsers) {
            if (isExternalUserLoaded(externalUser!!.getUuid())) {
                filteredExternalUser.updateUser.add(externalUser)
            } else {
                filteredExternalUser.newUser.add(externalUser)
            }
            if (externalUser.lists == null) {
                continue
            }
            for (userList in externalUser.lists) {
                if (isExternalMediaListLoaded(userList.id)) {
                    filteredExternalUser.updateList.add(userList)
                } else {
                    filteredExternalUser.newList.add(userList)
                }
                val missingMedia: MutableSet<Int> = HashSet()
                val currentJoins: MutableList<ListJoin> = ArrayList()
                for (item in userList.items) {
                    val join = ListJoin(userList.id, item)
                    if (!isMediumLoaded(item)) {
                        missingMedia.add(item)
                    }
                    currentJoins.add(join)
                }
                // if none medium is missing, just clear and add like normal
                if (missingMedia.isEmpty()) {
                    filteredExternalUser.joins.addAll(currentJoins)
                    filteredExternalUser.clearJoins.add(userList.id)
                } else {
                    for (mediumId in missingMedia) {
                        filteredExternalUser.mediumDependencies.add(
                            IntDependency(
                                mediumId,
                                currentJoins
                            )
                        )
                    }
                }
            }
        }
        return filteredExternalUser
    }

    fun isEpisodeLoaded(id: Int): Boolean {
        return loadedData.episodes.contains(id)
    }

    fun isPartLoaded(id: Int): Boolean {
        return loadedData.part.contains(id)
    }

    fun isMediumLoaded(id: Int): Boolean {
        return loadedData.media.contains(id)
    }

    fun isMediaListLoaded(id: Int): Boolean {
        return loadedData.mediaList.contains(id)
    }

    fun isExternalMediaListLoaded(id: Int): Boolean {
        return loadedData.externalMediaList.contains(id)
    }

    fun isExternalUserLoaded(uuid: String?): Boolean {
        return loadedData.externalUser.contains(uuid)
    }

    fun isNewsLoaded(id: Int): Boolean {
        return loadedData.news.contains(id)
    }

    class FilteredExternalUser {
        @kotlin.jvm.JvmField
        val newUser: MutableList<ClientExternalUser> = ArrayList()
        @kotlin.jvm.JvmField
        val updateUser: MutableList<ClientExternalUser> = ArrayList()
        @kotlin.jvm.JvmField
        val newList: MutableList<ClientExternalMediaList> = ArrayList()
        @kotlin.jvm.JvmField
        val updateList: MutableList<ClientExternalMediaList> = ArrayList()
        @kotlin.jvm.JvmField
        val joins: MutableList<ListJoin> = ArrayList()
        @kotlin.jvm.JvmField
        val clearJoins: MutableList<Int> = ArrayList()
        @kotlin.jvm.JvmField
        val mediumDependencies: MutableList<IntDependency<List<ListJoin>>> = ArrayList()
    }

    class FilteredExtMediaList {
        @kotlin.jvm.JvmField
        val newList: MutableList<ClientExternalMediaList> = ArrayList()
        @kotlin.jvm.JvmField
        val updateList: MutableList<ClientExternalMediaList> = ArrayList()
        @kotlin.jvm.JvmField
        val joins: MutableList<ListJoin> = ArrayList()
        @kotlin.jvm.JvmField
        val clearJoins: MutableList<Int> = ArrayList()
        @kotlin.jvm.JvmField
        val mediumDependencies: MutableList<IntDependency<List<ListJoin>>> = ArrayList()
        @kotlin.jvm.JvmField
        val userDependencies: MutableList<Dependency<String, ClientExternalMediaList>> =
            ArrayList()
    }

    class FilteredMediaList {
        @kotlin.jvm.JvmField
        val newList: MutableList<ClientMediaList> = ArrayList()
        @kotlin.jvm.JvmField
        val updateList: MutableList<ClientMediaList> = ArrayList()
        @kotlin.jvm.JvmField
        val joins: MutableList<ListJoin> = ArrayList()
        @kotlin.jvm.JvmField
        val clearJoins: MutableList<Int> = ArrayList()
        @kotlin.jvm.JvmField
        val mediumDependencies: MutableList<IntDependency<List<ListJoin>>> = ArrayList()
    }

    class ListJoin internal constructor(val listId: Int, val mediumId: Int)
    class FilteredMedia {
        @kotlin.jvm.JvmField
        val newMedia: MutableList<ClientSimpleMedium> = ArrayList()
        @kotlin.jvm.JvmField
        val updateMedia: MutableList<ClientSimpleMedium> = ArrayList()
        @kotlin.jvm.JvmField
        val unloadedParts: MutableList<Int> = ArrayList()
        @kotlin.jvm.JvmField
        val episodeDependencies: MutableList<IntDependency<ClientMedium>> = ArrayList()
    }

    class FilteredParts {
        @kotlin.jvm.JvmField
        val newParts: MutableList<ClientPart> = ArrayList()
        @kotlin.jvm.JvmField
        val updateParts: MutableList<ClientPart> = ArrayList()
        @kotlin.jvm.JvmField
        val mediumDependencies: MutableList<IntDependency<ClientPart>> = ArrayList()
        @kotlin.jvm.JvmField
        val episodes: MutableList<ClientEpisode> = ArrayList()
    }

    class FilteredEpisodes {
        @kotlin.jvm.JvmField
        val newEpisodes: MutableList<ClientEpisode> = ArrayList()
        @kotlin.jvm.JvmField
        val updateEpisodes: MutableList<ClientEpisode> = ArrayList()
        @kotlin.jvm.JvmField
        val partDependencies: MutableList<IntDependency<ClientEpisode>> = ArrayList()
        @kotlin.jvm.JvmField
        val releases: MutableList<ClientEpisodeRelease> = ArrayList()
    }

    class FilteredReadEpisodes {
        @kotlin.jvm.JvmField
        val episodeList: MutableList<ClientReadEpisode> = ArrayList()
        @kotlin.jvm.JvmField
        val dependencies: MutableList<IntDependency<ClientReadEpisode>> = ArrayList()
    }

    class IntDependency<T> internal constructor(val id: Int, val dependency: T)
    class Dependency<V, T> internal constructor(val id: V, val dependency: T)
}