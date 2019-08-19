package com.mytlogos.enterprise.background;

import com.mytlogos.enterprise.background.api.model.ClientEpisode;
import com.mytlogos.enterprise.background.api.model.ClientExternalMediaList;
import com.mytlogos.enterprise.background.api.model.ClientExternalUser;
import com.mytlogos.enterprise.background.api.model.ClientMediaList;
import com.mytlogos.enterprise.background.api.model.ClientMedium;
import com.mytlogos.enterprise.background.api.model.ClientMediumInWait;
import com.mytlogos.enterprise.background.api.model.ClientNews;
import com.mytlogos.enterprise.background.api.model.ClientPart;
import com.mytlogos.enterprise.background.api.model.ClientRelease;
import com.mytlogos.enterprise.background.api.model.ClientSimpleUser;
import com.mytlogos.enterprise.background.api.model.ClientUser;
import com.mytlogos.enterprise.background.resourceLoader.LoadWorkGenerator;
import com.mytlogos.enterprise.background.room.model.RoomDanglingMedium;
import com.mytlogos.enterprise.background.room.model.RoomEpisode;
import com.mytlogos.enterprise.background.room.model.RoomExternalMediaList;
import com.mytlogos.enterprise.background.room.model.RoomExternalUser;
import com.mytlogos.enterprise.background.room.model.RoomMediaList;
import com.mytlogos.enterprise.background.room.model.RoomMedium;
import com.mytlogos.enterprise.background.room.model.RoomMediumInWait;
import com.mytlogos.enterprise.background.room.model.RoomNews;
import com.mytlogos.enterprise.background.room.model.RoomPart;
import com.mytlogos.enterprise.background.room.model.RoomReadEpisode;
import com.mytlogos.enterprise.background.room.model.RoomRelease;
import com.mytlogos.enterprise.background.room.model.RoomToDownload;
import com.mytlogos.enterprise.background.room.model.RoomTocEpisode;
import com.mytlogos.enterprise.background.room.model.RoomUnReadEpisode;
import com.mytlogos.enterprise.background.room.model.RoomUser;
import com.mytlogos.enterprise.model.DisplayUnreadEpisode;
import com.mytlogos.enterprise.model.Episode;
import com.mytlogos.enterprise.model.HomeStats;
import com.mytlogos.enterprise.model.MediumInWait;
import com.mytlogos.enterprise.model.ReadEpisode;
import com.mytlogos.enterprise.model.ToDownload;
import com.mytlogos.enterprise.model.TocEpisode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class RoomConverter {

    private final LoadData loadedData;

    public RoomConverter(LoadData loadedData) {
        this.loadedData = loadedData;
    }

    public RoomConverter() {
        this(new LoadData());
    }

    public List<RoomExternalMediaList> convertExternalMediaList(Collection<ClientExternalMediaList> mediaLists) {
        return this.convert(mediaLists, this::convert);
    }

    public List<RoomMediaList> convertMediaList(Collection<ClientMediaList> mediaLists) {
        return this.convert(mediaLists, this::convert);
    }

    public List<RoomExternalUser> convertExternalUser(Collection<ClientExternalUser> mediaLists) {
        return this.convert(mediaLists, this::convert);
    }

    public List<RoomExternalMediaList.ExternalListMediaJoin> convertExListJoin(Collection<LoadWorkGenerator.ListJoin> mediaLists) {
        return this.convert(mediaLists, this::convertToExtListJoin);
    }

    public List<RoomMediaList.MediaListMediaJoin> convertListJoin(Collection<LoadWorkGenerator.ListJoin> joins) {
        return this.convert(joins, this::convertToListJoin);
    }

    public List<RoomEpisode> convertEpisodes(Collection<ClientEpisode> episodes) {
        return this.convert(episodes, this::convert);
    }

    public List<RoomRelease> convertReleases(Collection<ClientRelease> releases) {
        return this.convert(releases, this::convert);
    }

    public List<RoomMedium> convertMedia(Collection<ClientMedium> media) {
        List<RoomMedium> mediumList = new ArrayList<>(media.size());
        for (ClientMedium medium : media) {
            int currentRead = medium.getCurrentRead();
            Integer curredRead = this.loadedData.getEpisodes().contains(currentRead) ? currentRead : null;
            mediumList.add(this.convert(medium, curredRead));
        }
        return mediumList;
    }

    public List<RoomPart> convertParts(Collection<ClientPart> parts) {
        return this.convert(parts, this::convert);
    }

    public List<RoomToDownload> convertToDownload(Collection<ToDownload> toDownloads) {
        return this.convert(toDownloads, this::convert);
    }

    public List<ToDownload> convertRoomToDownload(Collection<RoomToDownload> roomToDownloads) {
        return this.convert(roomToDownloads, this::convert);
    }

    public Collection<RoomMediumInWait> convertClientMediaInWait(Collection<ClientMediumInWait> medium) {
        return this.convert(medium, this::convert);
    }

    public Collection<RoomDanglingMedium> convertToDangling(Collection<Integer> mediaIds) {
        return this.convert(mediaIds, RoomDanglingMedium::new);
    }

    public Collection<RoomMediumInWait> convertMediaInWait(Collection<MediumInWait> medium) {
        return this.convert(medium, this::convert);
    }

    private <R, T> List<R> convert(Collection<T> values, Function<T, R> converter) {
        List<R> list = new ArrayList<>();

        for (T t : values) {
            list.add(converter.apply(t));
        }
        return list;
    }

    public RoomMediumInWait convert(MediumInWait inWait) {
        return inWait == null ? null : new RoomMediumInWait(
                inWait.getTitle(),
                inWait.getMedium(),
                inWait.getLink()
        );
    }

    public DisplayUnreadEpisode convertRoomEpisode(RoomUnReadEpisode episode) {
        return episode == null ? null : new DisplayUnreadEpisode(
                episode.getEpisodeId(),
                episode.getMediumId(),
                episode.getMediumTitle(),
                episode.getTotalIndex(),
                episode.getPartialIndex(),
                episode.isSaved(),
                episode.isRead(),
                new ArrayList<>(episode.getReleases())
        );
    }

    public RoomExternalMediaList.ExternalListMediaJoin convertToExtListJoin(LoadWorkGenerator.ListJoin join) {
        return new RoomExternalMediaList.ExternalListMediaJoin(
                join.listId, join.mediumId
        );
    }

    public RoomMediaList.MediaListMediaJoin convertToListJoin(LoadWorkGenerator.ListJoin mediaList) {
        return new RoomMediaList.MediaListMediaJoin(
                mediaList.listId, mediaList.mediumId
        );
    }

    public RoomEpisode convert(ClientEpisode episode) {
        return new RoomEpisode(
                episode.getId(), episode.getProgress(), episode.getReadDate(), episode.getPartId(),
                episode.getTotalIndex(), episode.getPartialIndex(), false
        );
    }

    public RoomRelease convert(ClientRelease release) {
        return new RoomRelease(
                release.getEpisodeId(),
                release.getTitle(),
                release.getUrl(),
                release.getReleaseDate(),
                release.isLocked()
        );
    }

    public RoomExternalUser convert(ClientExternalUser user) {
        return new RoomExternalUser(
                user.getUuid(), user.getLocalUuid(), user.getIdentifier(),
                user.getType()
        );
    }

    public RoomMediaList convert(ClientMediaList mediaList) {
        return new RoomMediaList(
                mediaList.getId(), mediaList.getUserUuid(), mediaList.getName(),
                mediaList.getMedium()
        );
    }

    public RoomExternalMediaList convert(ClientExternalMediaList mediaList) {
        return new RoomExternalMediaList(
                mediaList.getUuid(), mediaList.getId(), mediaList.getName(),
                mediaList.getMedium(), mediaList.getUrl()
        );
    }

    public RoomMedium convert(ClientMedium medium, Integer curredRead) {
        return new RoomMedium(
                curredRead, medium.getId(), medium.getCountryOfOrigin(),
                medium.getLanguageOfOrigin(), medium.getAuthor(), medium.getTitle(),
                medium.getMedium(), medium.getArtist(), medium.getLang(),
                medium.getStateOrigin(), medium.getStateTL(), medium.getSeries(),
                medium.getUniverse()
        );
    }

    public RoomNews convert(ClientNews news) {
        return new RoomNews(
                news.getId(), news.isRead(),
                news.getTitle(), news.getDate(),
                news.getLink()
        );
    }

    public RoomPart convert(ClientPart part) {
        return new RoomPart(
                part.getId(), part.getMediumId(), part.getTitle(), part.getTotalIndex(),
                part.getPartialIndex()
        );
    }

    public RoomToDownload convert(ToDownload toDownload) {
        return new RoomToDownload(
                0, toDownload.isProhibited(),
                toDownload.getMediumId(),
                toDownload.getListId(),
                toDownload.getExternalListId()
        );
    }

    public ToDownload convert(RoomToDownload roomToDownload) {
        return new ToDownload(
                roomToDownload.isProhibited(),
                roomToDownload.getMediumId(),
                roomToDownload.getListId(),
                roomToDownload.getExternalListId()
        );
    }

    public RoomUser convert(ClientUser user) {
        return new RoomUser(
                user.getName(),
                user.getUuid(),
                user.getSession()
        );
    }

    public RoomMediumInWait convert(ClientMediumInWait medium) {
        return new RoomMediumInWait(
                medium.getTitle(),
                medium.getMedium(),
                medium.getLink()
        );
    }

    public Episode convert(RoomEpisode roomEpisode) {
        return roomEpisode == null ? null : new Episode(
                roomEpisode.getEpisodeId(),
                roomEpisode.getProgress(),
                roomEpisode.getPartId(),
                roomEpisode.getPartialIndex(),
                roomEpisode.getTotalIndex(),
                roomEpisode.getReadDate(),
                roomEpisode.isSaved()
        );
    }

    public HomeStats toUser(RoomUser user, Integer countReadToday, Integer countUnreadChapter, Integer countUnreadNews) {
        return null;
    }

    public MediumInWait convert(RoomMediumInWait input) {
        return input == null ? null : new MediumInWait(
                input.getTitle(),
                input.getMedium(),
                input.getLink()
        );
    }

    public TocEpisode convertTocEpisode(RoomTocEpisode roomTocEpisode) {
        return roomTocEpisode == null ? null : new TocEpisode(
                roomTocEpisode.getEpisodeId(),
                roomTocEpisode.getProgress(),
                roomTocEpisode.getPartId(),
                roomTocEpisode.getPartialIndex(),
                roomTocEpisode.getTotalIndex(),
                roomTocEpisode.getReadDate(),
                roomTocEpisode.isSaved(),
                new ArrayList<>(roomTocEpisode.getReleases())
        );
    }

    public RoomUser convert(ClientSimpleUser user) {
        return user == null ? null : new RoomUser(
                user.getName(),
                user.getUuid(),
                user.getSession()
        );
    }

    public ReadEpisode convert(RoomReadEpisode input) {
        return input == null ? null : new ReadEpisode(
                input.getEpisodeId(),
                input.getMediumId(),
                input.getMediumTitle(),
                input.getTotalIndex(),
                input.getPartialIndex(),
                new ArrayList<>(input.getReleases())
        );
    }
}
