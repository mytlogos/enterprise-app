package com.mytlogos.enterprise.background

import com.mytlogos.enterprise.background.api.model.*
import com.mytlogos.enterprise.background.api.model.ClientStat.ParsedStat
import com.mytlogos.enterprise.background.resourceLoader.LoadWorkGenerator.*
import com.mytlogos.enterprise.model.ToDownload
import com.mytlogos.enterprise.model.Toc
import java.util.stream.Collectors

interface ClientModelPersister {
    fun getConsumer(): Collection<ClientConsumer<*>>
    fun persist(vararg episode: ClientEpisode): ClientModelPersister {
        return persistEpisodes(listOf(*episode))
    }

    fun persistEpisodes(episodes: Collection<ClientEpisode>): ClientModelPersister
    fun persistReleases(releases: Collection<ClientRelease>): ClientModelPersister
    fun persist(vararg mediaLists: ClientMediaList): ClientModelPersister {
        return persistMediaLists(listOf(*mediaLists))
    }

    fun persist(filteredEpisodes: FilteredEpisodes): ClientModelPersister
    fun persistMediaLists(mediaLists: List<ClientMediaList>): ClientModelPersister
    fun persistUserLists(mediaLists: List<ClientUserList>): ClientModelPersister
    fun persist(vararg externalMediaLists: ClientExternalMediaList): ClientModelPersister {
        return persistExternalMediaLists(listOf(*externalMediaLists))
    }

    fun persist(filteredMediaList: FilteredMediaList): ClientModelPersister
    fun persistExternalMediaLists(externalMediaLists: Collection<ClientExternalMediaList>): ClientModelPersister
    fun persist(vararg externalUsers: ClientExternalUser): ClientModelPersister {
        return persistExternalUsers(listOf(*externalUsers))
    }

    fun persist(filteredExtMediaList: FilteredExtMediaList): ClientModelPersister
    fun persistExternalUsers(externalUsers: List<ClientExternalUser>): ClientModelPersister
    fun persistExternalUsersPure(externalUsers: List<ClientExternalUserPure>): ClientModelPersister {
        val unpure = externalUsers.stream().map { value: ClientExternalUserPure ->
            ClientExternalUser(
                value.localUuid,
                value.getUuid()!!,
                value.identifier,
                value.type,
                arrayOf()
            )
        }.collect(Collectors.toList())
        return persistExternalUsers(unpure)
    }

    fun persist(vararg media: ClientSimpleMedium): ClientModelPersister {
        return persistMedia(listOf(*media))
    }

    fun persist(filteredExternalUser: FilteredExternalUser): ClientModelPersister
    fun persistMedia(media: Collection<ClientSimpleMedium>): ClientModelPersister
    fun persist(vararg news: ClientNews): ClientModelPersister {
        return persistNews(listOf(*news))
    }

    fun persist(filteredMedia: FilteredMedia): ClientModelPersister
    fun persistNews(news: Collection<ClientNews>): ClientModelPersister
    fun persist(vararg parts: ClientPart): ClientModelPersister {
        return persistParts(listOf(*parts))
    }

    fun persistParts(parts: Collection<ClientPart>): ClientModelPersister
    fun persistPartsPure(parts: Collection<ClientPartPure>): ClientModelPersister {
        val unPureParts = parts
            .stream()
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
            .collect(Collectors.toList())
        persistParts(unPureParts)
        return this
    }

    fun persist(filteredReadEpisodes: FilteredReadEpisodes): ClientModelPersister
    fun persist(query: ClientListQuery): ClientModelPersister
    fun persist(query: ClientMultiListQuery): ClientModelPersister
    fun persist(clientUser: ClientUser?): ClientModelPersister
    fun persist(user: ClientUpdateUser): ClientModelPersister
    fun persistToDownloads(toDownloads: Collection<ToDownload>): ClientModelPersister
    fun persist(vararg readEpisodes: ClientReadEpisode): ClientModelPersister {
        return persistReadEpisodes(listOf(*readEpisodes))
    }

    fun persist(filteredParts: FilteredParts): ClientModelPersister
    fun persistReadEpisodes(readEpisodes: Collection<ClientReadEpisode>): ClientModelPersister
    fun persist(stat: ParsedStat): ClientModelPersister
    fun finish()
    fun persist(toDownload: ToDownload): ClientModelPersister
    fun persistMediaInWait(medium: List<ClientMediumInWait>)
    fun persist(user: ClientSimpleUser?): ClientModelPersister
    fun deleteLeftoverEpisodes(partEpisodes: Map<Int, List<Int>>)
    fun deleteLeftoverReleases(partReleases: Map<Int, List<ClientSimpleRelease>>): Collection<Int>
    fun deleteLeftoverTocs(mediaTocs: Map<Int, List<String>>)
    fun persistTocs(tocs: Collection<Toc>): ClientModelPersister
}