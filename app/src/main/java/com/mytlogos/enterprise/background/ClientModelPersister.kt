package com.mytlogos.enterprise.background

import com.mytlogos.enterprise.background.api.model.*
import com.mytlogos.enterprise.background.api.model.ClientStat.ParsedStat
import com.mytlogos.enterprise.background.resourceLoader.LoadWorkGenerator.*
import com.mytlogos.enterprise.model.ToDownload
import com.mytlogos.enterprise.model.Toc

interface ClientModelPersister {
    fun getConsumer(): Collection<ClientConsumer<*>>
    suspend fun persist(vararg episode: ClientEpisode): ClientModelPersister {
        return persistEpisodes(listOf(*episode))
    }

    suspend fun persistEpisodes(episodes: Collection<ClientEpisode>): ClientModelPersister
    suspend fun persistReleases(releases: Collection<ClientRelease>): ClientModelPersister
    suspend fun persist(vararg mediaLists: ClientMediaList): ClientModelPersister {
        return persistMediaLists(listOf(*mediaLists))
    }

    suspend fun persist(filteredEpisodes: FilteredEpisodes): ClientModelPersister
    suspend fun persistMediaLists(mediaLists: List<ClientMediaList>): ClientModelPersister
    suspend fun persistUserLists(mediaLists: List<ClientUserList>): ClientModelPersister
    suspend fun persist(vararg externalMediaLists: ClientExternalMediaList): ClientModelPersister {
        return persistExternalMediaLists(listOf(*externalMediaLists))
    }

    suspend fun persist(filteredMediaList: FilteredMediaList): ClientModelPersister
    suspend fun persistExternalMediaLists(externalMediaLists: Collection<ClientExternalMediaList>): ClientModelPersister
    suspend fun persist(vararg externalUsers: ClientExternalUser): ClientModelPersister {
        return persistExternalUsers(listOf(*externalUsers))
    }

    suspend fun persist(filteredExtMediaList: FilteredExtMediaList): ClientModelPersister
    suspend fun persistExternalUsers(externalUsers: List<ClientExternalUser>): ClientModelPersister
    suspend fun persistExternalUsersPure(externalUsers: List<ClientExternalUserPure>): ClientModelPersister {
        val unpure = externalUsers.map { value: ClientExternalUserPure ->
            ClientExternalUser(
                value.localUuid,
                value.getUuid(),
                value.identifier,
                value.type,
                arrayOf()
            )
        }
        return persistExternalUsers(unpure)
    }

    suspend fun persist(vararg media: ClientSimpleMedium): ClientModelPersister {
        return persistMedia(listOf(*media))
    }

    suspend fun persist(filteredExternalUser: FilteredExternalUser): ClientModelPersister
    suspend fun persistMedia(media: Collection<ClientSimpleMedium>): ClientModelPersister
    suspend fun persist(vararg news: ClientNews): ClientModelPersister {
        return persistNews(listOf(*news))
    }

    suspend fun persist(filteredMedia: FilteredMedia): ClientModelPersister
    suspend fun persistNews(news: Collection<ClientNews>): ClientModelPersister
    suspend fun persist(vararg parts: ClientPart): ClientModelPersister {
        return persistParts(listOf(*parts))
    }

    suspend fun persistParts(parts: Collection<ClientPart>): ClientModelPersister
    suspend fun persistPartsPure(parts: Collection<ClientPartPure>): ClientModelPersister {
        val unPureParts = parts
            .map { part: ClientPartPure ->
                ClientPart(
                    part.mediumId,
                    part.id,
                    part.title,
                    part.totalIndex,
                    part.partialIndex,
                    null
                )
            }
        persistParts(unPureParts)
        return this
    }

    suspend fun persist(filteredReadEpisodes: FilteredReadEpisodes): ClientModelPersister
    suspend fun persist(query: ClientListQuery): ClientModelPersister
    suspend fun persist(query: ClientMultiListQuery): ClientModelPersister
    suspend fun persist(clientUser: ClientUser?): ClientModelPersister
    suspend fun persist(user: ClientUpdateUser): ClientModelPersister
    suspend fun persistToDownloads(toDownloads: Collection<ToDownload>): ClientModelPersister
    suspend fun persist(vararg readEpisodes: ClientReadEpisode): ClientModelPersister {
        return persistReadEpisodes(listOf(*readEpisodes))
    }

    suspend fun persist(filteredParts: FilteredParts): ClientModelPersister
    suspend fun persistReadEpisodes(readEpisodes: Collection<ClientReadEpisode>): ClientModelPersister
    suspend fun persist(stat: ParsedStat): ClientModelPersister
    fun finish()
    suspend fun persist(toDownload: ToDownload): ClientModelPersister
    suspend fun persistMediaInWait(medium: List<ClientMediumInWait>)
    suspend fun persist(user: ClientSimpleUser?): ClientModelPersister
    suspend fun deleteLeftoverEpisodes(partEpisodes: Map<Int, List<Int>>)
    suspend fun deleteLeftoverReleases(partReleases: Map<Int, List<ClientSimpleRelease>>): Collection<Int>
    suspend fun deleteLeftoverTocs(mediaTocs: Map<Int, List<String>>)
    suspend fun persistTocs(tocs: Collection<Toc>): ClientModelPersister
}