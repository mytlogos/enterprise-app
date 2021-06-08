package com.mytlogos.enterprise.background

import com.mytlogos.enterprise.background.api.model.*
import com.mytlogos.enterprise.background.resourceLoader.LoadWorkGenerator.ListJoin
import com.mytlogos.enterprise.background.room.model.*
import com.mytlogos.enterprise.background.room.model.RoomExternalMediaList.ExternalListMediaJoin
import com.mytlogos.enterprise.background.room.model.RoomMediaList.MediaListMediaJoin
import com.mytlogos.enterprise.model.*
import java.util.*
import java.util.function.Function

class RoomConverter @JvmOverloads constructor(private val loadedData: LoadData = LoadData()) {
    fun convertExternalMediaList(mediaLists: Collection<ClientExternalMediaList>?): List<RoomExternalMediaList> {
        return this.convert(mediaLists) { mediaList: ClientExternalMediaList ->
            this.convert(
                mediaList
            )
        }
    }

    fun convertMediaList(mediaLists: Collection<ClientMediaList>?): List<RoomMediaList> {
        return this.convert(mediaLists) { mediaList: ClientMediaList -> this.convert(mediaList) }
    }

    fun convertExternalUser(mediaLists: Collection<ClientExternalUser>?): List<RoomExternalUser> {
        return this.convert(mediaLists) { user: ClientExternalUser -> this.convert(user) }
    }

    fun convertExListJoin(mediaLists: Collection<ListJoin>?): List<ExternalListMediaJoin> {
        return this.convert(mediaLists) { join: ListJoin -> convertToExtListJoin(join) }
    }

    fun convertListJoin(joins: Collection<ListJoin>?): List<MediaListMediaJoin> {
        return this.convert(joins) { mediaList: ListJoin -> convertToListJoin(mediaList) }
    }

    fun convertEpisodes(episodes: Collection<ClientEpisode>?): List<RoomEpisode> {
        return this.convert(episodes) { episode: ClientEpisode -> this.convert(episode) }
    }

    fun convertEpisodesClient(episodes: Collection<ClientEpisode>?): List<ClientRoomEpisode> {
        return this.convert(episodes) { episode: ClientEpisode -> convertClient(episode) }
    }

    fun convertEpisodeReleases(releases: Collection<ClientEpisodeRelease>?): List<RoomRelease> {
        return this.convert(releases) { release: ClientEpisodeRelease -> this.convert(release) }
    }

    fun convertReleases(releases: Collection<ClientRelease>?): List<RoomRelease> {
        return this.convert(releases) { release: ClientRelease -> this.convert(release) }
    }

    fun convertMedia(media: Collection<ClientMedium>): List<RoomMedium> {
        val mediumList: MutableList<RoomMedium> = ArrayList(media.size)
        for (medium in media) {
            val currentRead = medium.currentRead
            val curredRead = if (loadedData.episodes.contains(currentRead)) currentRead else null
            mediumList.add(this.convert(medium, curredRead))
        }
        return mediumList
    }

    fun convertSimpleMedia(media: Collection<ClientSimpleMedium>): List<RoomMedium> {
        val mediumList: MutableList<RoomMedium> = ArrayList(media.size)
        for (medium in media) {
            mediumList.add(this.convert(medium))
        }
        return mediumList
    }

    fun convertParts(parts: Collection<ClientPart>?): List<RoomPart> {
        return this.convert(parts) { part: ClientPart -> this.convert(part) }
    }

    fun convertToDownload(toDownloads: Collection<ToDownload>?): List<RoomToDownload> {
        return this.convert(toDownloads) { toDownload: ToDownload -> this.convert(toDownload) }
    }

    fun convertRoomToDownload(roomToDownloads: Collection<RoomToDownload>?): List<ToDownload> {
        return this.convert(roomToDownloads) { roomToDownload: RoomToDownload ->
            this.convert(
                roomToDownload
            )
        }
    }

    fun convertClientMediaInWait(media: Collection<ClientMediumInWait>?): Collection<RoomMediumInWait> {
        return this.convert(media) { medium: ClientMediumInWait -> this.convert(medium) }
    }

    fun convertToDangling(mediaIds: Collection<Int>?): Collection<RoomDanglingMedium> {
        return this.convert(mediaIds) { mediumId: Int? ->
            RoomDanglingMedium(
                mediumId!!
            )
        }
    }

    fun convertMediaInWait(medium: Collection<MediumInWait>): Collection<RoomMediumInWait> {
        return this.convert(medium) { inWait: MediumInWait -> this.convert(inWait) }
    }

    fun convertEditEvents(events: Collection<EditEvent>?): Collection<RoomEditEvent?> {
        return this.convert(events) { event: EditEvent? -> this.convert(event) }
    }

    fun convertToc(tocs: Collection<Toc>): List<RoomToc> {
        return this.convert(tocs) { toc: Toc -> this.convert(toc) }
    }

    private fun <R, T> convert(values: Collection<T>?, converter: Function<T, R>): List<R> {
        val list: MutableList<R> = ArrayList()
        if (values == null) {
            return list
        }
        for (t in values) {
            list.add(converter.apply(t))
        }
        return list
    }

    private fun convert(toc: Toc): RoomToc {
        return if (toc is RoomToc) toc else RoomToc(
            toc.mediumId,
            toc.link
        )
    }

    fun convert(inWait: MediumInWait): RoomMediumInWait {
        return RoomMediumInWait(
            inWait.title,
            inWait.medium,
            inWait.link
        )
    }

    fun convertRoomEpisode(episode: RoomDisplayEpisode?): DisplayEpisode? {
        return if (episode == null) null else this.convertRoomEpisodeNonNull(episode)
    }

    fun convertRoomEpisodeNonNull(episode: RoomDisplayEpisode): DisplayEpisode {
        return DisplayEpisode(
            episode.episodeId,
            episode.mediumId,
            episode.mediumTitle,
            episode.totalIndex,
            episode.partialIndex,
            episode.saved,
            episode.read,
            ArrayList<Release>(episode.releases)
        )
    }

    fun convertToExtListJoin(join: ListJoin): ExternalListMediaJoin {
        return ExternalListMediaJoin(
            join.listId, join.mediumId
        )
    }

    fun convertToListJoin(mediaList: ListJoin): MediaListMediaJoin {
        return MediaListMediaJoin(
            mediaList.listId, mediaList.mediumId
        )
    }

    fun convert(episode: ClientEpisode): RoomEpisode {
        return RoomEpisode(
            episode.id,
            episode.progress,
            episode.readDate,
            episode.partId,
            episode.totalIndex,
            episode.partialIndex,
            String.format("%s.%s", episode.totalIndex, episode.partialIndex).toDouble(),
            false
        )
    }

    fun convertClient(episode: ClientEpisode): ClientRoomEpisode {
        return ClientRoomEpisode(
            episode.id,
            episode.progress,
            episode.partId,
            episode.totalIndex,
            episode.partialIndex,
            if (episode.combiIndex != 0.0) episode.combiIndex else String.format(
                "%s.%s",
                episode.totalIndex,
                episode.partialIndex
            ).toDouble(),
            episode.readDate
        )
    }

    fun convert(release: ClientRelease): RoomRelease {
        return RoomRelease(
            release.episodeId,
            release.getTitle(),
            release.getUrl(),
            release.getReleaseDate(),
            release.isLocked
        )
    }

    fun convert(release: ClientEpisodeRelease): RoomRelease {
        return RoomRelease(
            release.episodeId,
            release.title,
            release.url,
            release.releaseDate,
            release.isLocked
        )
    }

    fun convert(user: ClientExternalUser): RoomExternalUser {
        return RoomExternalUser(
            user.getUuid(), user.localUuid, user.identifier,
            user.type
        )
    }

    fun convert(mediaList: ClientMediaList): RoomMediaList {
        return RoomMediaList(
            mediaList.id, mediaList.userUuid, mediaList.name,
            mediaList.medium
        )
    }

    fun convert(mediaList: ClientExternalMediaList): RoomExternalMediaList {
        return RoomExternalMediaList(
            mediaList.uuid, mediaList.id, mediaList.name,
            mediaList.medium, mediaList.url
        )
    }

    fun convert(medium: ClientMedium, curredRead: Int?): RoomMedium {
        return RoomMedium(
            curredRead, medium.id, medium.countryOfOrigin ?: "",
            medium.languageOfOrigin ?: "", medium.author ?: "", medium.title,
            medium.medium, medium.artist ?: "", medium.lang ?: "",
            medium.stateOrigin, medium.stateTL, medium.series ?: "",
            medium.universe ?: ""
        )
    }

    fun convert(medium: ClientSimpleMedium): RoomMedium {
        return RoomMedium(
            null, medium.id, medium.countryOfOrigin ?: "",
            medium.languageOfOrigin ?: "", medium.author ?: "", medium.title,
            medium.medium, medium.artist ?: "", medium.lang ?: "",
            medium.stateOrigin, medium.stateTL, medium.series ?: "",
            medium.universe ?: ""
        )
    }

    fun convert(news: ClientNews): RoomNews {
        return RoomNews(
            news.id, news.isRead,
            news.title, news.date,
            news.link
        )
    }

    fun convert(part: ClientPart): RoomPart {
        return RoomPart(
            part.id,
            part.mediumId,
            part.title,
            part.totalIndex,
            part.partialIndex, String.format("%s.%s", part.totalIndex, part.partialIndex).toDouble()
        )
    }

    fun convert(toDownload: ToDownload): RoomToDownload {
        return RoomToDownload(
            0, toDownload.isProhibited,
            toDownload.mediumId,
            toDownload.listId,
            toDownload.externalListId
        )
    }

    fun convert(roomToDownload: RoomToDownload): ToDownload {
        return ToDownload(
            roomToDownload.prohibited,
            roomToDownload.mediumId,
            roomToDownload.listId,
            roomToDownload.externalListId
        )
    }

    fun convert(user: ClientUser): RoomUser {
        return RoomUser(
            user.name,
            user.uuid,
            user.session
        )
    }

    fun convert(medium: ClientMediumInWait): RoomMediumInWait {
        return RoomMediumInWait(
            medium.title,
            medium.medium,
            medium.link
        )
    }

    fun convert(roomEpisode: RoomEpisode): Episode {
        return Episode(
            roomEpisode.episodeId,
            roomEpisode.progress,
            roomEpisode.partId,
            roomEpisode.partialIndex,
            roomEpisode.totalIndex,
            roomEpisode.readDate,
            roomEpisode.saved
        )
    }

    fun convert(input: RoomMediumInWait): MediumInWait {
        return MediumInWait(
            input.title,
            input.medium,
            input.link
        )
    }

    fun convertTocEpisode(roomTocEpisode: RoomTocEpisode): TocEpisode {
        return TocEpisode(
            roomTocEpisode.episodeId,
            roomTocEpisode.progress,
            roomTocEpisode.partId,
            roomTocEpisode.partialIndex,
            roomTocEpisode.totalIndex,
            roomTocEpisode.readDate,
            roomTocEpisode.saved,
            ArrayList<Release>(roomTocEpisode.releases)
        )
    }

    fun convert(user: ClientSimpleUser): RoomUser {
        return RoomUser(
            user.getName(),
            user.getUuid(),
            user.getSession()
        )
    }

    fun convert(input: RoomReadEpisode): ReadEpisode {
        return ReadEpisode(
            input.episodeId,
            input.mediumId,
            input.mediumTitle,
            input.totalIndex,
            input.partialIndex,
            ArrayList<Release>(input.releases)
        )
    }

    fun convert(event: EditEvent?): RoomEditEvent? {
        return when (event) {
            null -> null
            is RoomEditEvent -> event
            else -> RoomEditEvent(
                event.id,
                event.objectType,
                event.eventType,
                event.dateTime,
                event.firstValue,
                event.secondValue
            )
        }
    }

    fun convert(user: RoomUser?): User? {
        return if (user == null) null else User(
            user.uuid,
            user.session,
            user.name
        )
    }
}