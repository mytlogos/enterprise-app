package com.mytlogos.enterprise.background

import com.mytlogos.enterprise.background.api.model.*
import com.mytlogos.enterprise.background.resourceLoader.LoadWorkGenerator.ListJoin
import com.mytlogos.enterprise.background.room.model.*
import com.mytlogos.enterprise.background.room.model.RoomExternalMediaList.ExternalListMediaJoin
import com.mytlogos.enterprise.background.room.model.RoomMediaList.MediaListMediaJoin
import com.mytlogos.enterprise.model.*
import java.util.*

fun Collection<ClientExternalMediaList>?.externalListToRoom(): List<RoomExternalMediaList> {
    if (this == null) {
        return emptyList()
    }
    return this.map { it.toRoom() }
}

fun Collection<ClientMediaList>?.listToRoom(): List<RoomMediaList> {
    if (this == null) {
        return emptyList()
    }
    return this.map { it.toRoom() }
}

fun Collection<ClientExternalUser>?.externalUserToRoom(): List<RoomExternalUser> {
    if (this == null) {
        return emptyList()
    }
    return this.map { it.toRoom() }
}

fun Collection<ListJoin>?.toRoomExternalJoin(): List<ExternalListMediaJoin> {
    if (this == null) {
        return emptyList()
    }
    return this.map { it.toRoomExternalJoin() }
}

fun Collection<ListJoin>?.toRoomJoin(): List<MediaListMediaJoin> {
    if (this == null) {
        return emptyList()
    }
    return this.map { it.toRoomJoin() }
}

fun Collection<ClientEpisode>?.toRoomEpisode(): List<RoomEpisode> {
    if (this == null) {
        return emptyList()
    }
    return this.map { it.toRoom() }
}

fun Collection<ClientEpisode>?.toRoomClientEpisode(): List<ClientRoomEpisode> {
    if (this == null) {
        return emptyList()
    }
    return this.map { it.toClientRoom() }
}

fun Collection<ClientEpisodeRelease>?.episodeToRoomRelease(): List<RoomRelease> {
    if (this == null) {
        return emptyList()
    }
    return this.map { it.toRoom() }
}

fun Collection<ClientRelease>?.toRoomRelease(): List<RoomRelease> {
    if (this == null) {
        return emptyList()
    }
    return this.map { it.toRoom() }
}

fun Collection<ClientMedium>.toRoomMedium(loadedData: LoadData): List<RoomMedium> {
    return this.map {
        val currentRead = if (loadedData.episodes.contains(it.currentRead)) it.currentRead else null
        it.toRoom(currentRead)
    }
}

fun Collection<ClientSimpleMedium>.simpleToRoomMedium(): List<RoomMedium> {
    return this.map { it.toRoom() }
}

fun Collection<ClientPart>?.toRoomPart(): List<RoomPart> {
    if (this == null) {
        return emptyList()
    }
    return this.map { it.toRoom() }
}

fun Collection<ToDownload>?.toRoomToDownload(): List<RoomToDownload> {
    if (this == null) {
        return emptyList()
    }
    return this.map { it.toRoom() }
}

fun Collection<RoomToDownload>?.fromRoomToDownload(): List<ToDownload> {
    if (this == null) {
        return emptyList()
    }
    return this.map { it.toRoom() }
}

fun Collection<ClientMediumInWait>?.clientToRoomInWait(): Collection<RoomMediumInWait> {
    if (this == null) {
        return emptyList()
    }
    return this.map { it.toRoom() }
}

fun Collection<Int>?.toRoomDangling(): Collection<RoomDanglingMedium> {
    if (this == null) {
        return emptyList()
    }
    return this.map { RoomDanglingMedium(it) }
}

fun Collection<MediumInWait>.toRoomInWait(): Collection<RoomMediumInWait> {
    return this.map { it.toRoom() }
}

fun Collection<EditEvent>.toRoomEditEvent(): Collection<RoomEditEvent> {
    return this.mapNotNull { it.toRoom() }
}

fun Collection<Toc>.toRoomToc(): List<RoomToc> {
    return this.map { it.toRoom() }
}

private fun Toc.toRoom(): RoomToc {
    return if (this is RoomToc) this else RoomToc(
        this.mediumId,
        this.link
    )
}

fun MediumInWait.toRoom(): RoomMediumInWait {
    return RoomMediumInWait(
        this.title,
        this.medium,
        this.link
    )
}

fun RoomDisplayEpisode?.fromRoom(): DisplayEpisode? {
    return this?.fromRoomNonNull()
}

fun RoomDisplayEpisode.fromRoomNonNull(): DisplayEpisode {
    return DisplayEpisode(
        this.episodeId,
        this.mediumId,
        this.mediumTitle,
        this.totalIndex,
        this.partialIndex,
        this.saved,
        this.read,
        ArrayList<Release>(this.releases)
    )
}

fun ListJoin.toRoomExternalJoin(): ExternalListMediaJoin {
    return ExternalListMediaJoin(
        this.listId, this.mediumId
    )
}

fun ListJoin.toRoomJoin(): MediaListMediaJoin {
    return MediaListMediaJoin(
        this.listId, this.mediumId
    )
}

fun ClientEpisode.toRoom(): RoomEpisode {
    return RoomEpisode(
        this.id,
        this.progress,
        this.readDate,
        this.partId,
        this.totalIndex,
        this.partialIndex,
        if (this.combiIndex != 0.0) this.combiIndex else this.toCombiIndex(),
        false
    )
}

fun ClientEpisode.toClientRoom(): ClientRoomEpisode {
    return ClientRoomEpisode(
        this.id,
        this.progress,
        this.partId,
        this.totalIndex,
        this.partialIndex,
        if (this.combiIndex != 0.0) this.combiIndex else this.toCombiIndex(),
        this.readDate
    )
}

fun ClientRelease.toRoom(): RoomRelease {
    return RoomRelease(
        this.episodeId,
        this.title,
        this.url,
        this.releaseDate,
        this.locked
    )
}

fun ClientEpisodeRelease.toRoom(): RoomRelease {
    return RoomRelease(
        this.episodeId,
        this.title,
        this.url,
        this.releaseDate,
        this.locked
    )
}

fun ClientExternalUser.toRoom(): RoomExternalUser {
    return RoomExternalUser(
        this.getUuid(), this.localUuid, this.identifier,
        this.type
    )
}

fun ClientMediaList.toRoom(): RoomMediaList {
    return RoomMediaList(
        this.id,
        this.userUuid,
        this.name,
        this.medium
    )
}

fun ClientExternalMediaList.toRoom(): RoomExternalMediaList {
    return RoomExternalMediaList(
        this.uuid, this.id, this.name,
        this.medium, this.url
    )
}

fun ClientMedium.toRoom(curredRead: Int?): RoomMedium {
    return RoomMedium(
        curredRead, this.id, this.countryOfOrigin ?: "",
        this.languageOfOrigin ?: "", this.author ?: "", this.title,
        this.medium, this.artist ?: "", this.lang ?: "",
        this.stateOrigin, this.stateTL, this.series ?: "",
        this.universe ?: ""
    )
}

fun ClientSimpleMedium.toRoom(): RoomMedium {
    return RoomMedium(
        null, this.id, this.countryOfOrigin ?: "",
        this.languageOfOrigin ?: "", this.author ?: "", this.title,
        this.medium, this.artist ?: "", this.lang ?: "",
        this.stateOrigin, this.stateTL, this.series ?: "",
        this.universe ?: ""
    )
}

fun ClientNews.toRoom(): RoomNews {
    return RoomNews(
        this.id, this.isRead,
        this.title, this.date,
        this.link
    )
}

fun ClientPart.toRoom(): RoomPart {
    return RoomPart(
        this.id,
        this.mediumId,
        this.title,
        this.totalIndex,
        this.partialIndex,
        this.toCombiIndex()
    )
}

fun ToDownload.toRoom(): RoomToDownload {
    return RoomToDownload(
        0, this.isProhibited,
        this.mediumId,
        this.listId,
        this.externalListId
    )
}

fun RoomToDownload.toRoom(): ToDownload {
    return ToDownload(
        this.prohibited,
        this.mediumId,
        this.listId,
        this.externalListId
    )
}

fun ClientUser.toRoom(): RoomUser {
    return RoomUser(
        this.name,
        this.uuid,
        this.session
    )
}

fun ClientMediumInWait.toRoom(): RoomMediumInWait {
    return RoomMediumInWait(
        this.title,
        this.medium,
        this.link
    )
}

fun RoomEpisode.fromRoom(): Episode {
    return Episode(
        this.episodeId,
        this.progress,
        this.partId,
        this.partialIndex,
        this.totalIndex,
        this.readDate,
        this.saved
    )
}

fun RoomMediumInWait.fromRoom(): MediumInWait {
    return MediumInWait(
        this.title,
        this.medium,
        this.link
    )
}

fun RoomTocEpisode.fromRoom(): TocEpisode {
    return TocEpisode(
        this.episodeId,
        this.progress,
        this.partId,
        this.partialIndex,
        this.totalIndex,
        this.readDate,
        this.saved,
        ArrayList<Release>(this.releases)
    )
}

fun ClientSimpleUser.toRoom(): RoomUser {
    return RoomUser(
        this.uuid,
        this.name,
        this.session,
    )
}

fun RoomReadEpisode.fromRoom(): ReadEpisode {
    return ReadEpisode(
        this.episodeId,
        this.mediumId,
        this.mediumTitle,
        this.totalIndex,
        this.partialIndex,
        ArrayList<Release>(this.releases)
    )
}

fun EditEvent?.toRoom(): RoomEditEvent? {
    return when (this) {
        null -> null
        is RoomEditEvent -> this
        else -> RoomEditEvent(
            this.id,
            this.objectType,
            this.eventType,
            this.dateTime,
            this.firstValue,
            this.secondValue
        )
    }
}

fun RoomUser?.fromRoom(): User? {
    return if (this == null) null else User(
        this.uuid,
        this.session,
        this.name
    )
}
