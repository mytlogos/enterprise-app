package com.mytlogos.enterprise.background;

import com.mytlogos.enterprise.background.api.model.ClientEpisode;
import com.mytlogos.enterprise.background.api.model.ClientExternalMediaList;
import com.mytlogos.enterprise.background.api.model.ClientExternalUser;
import com.mytlogos.enterprise.background.api.model.ClientMediaList;
import com.mytlogos.enterprise.background.api.model.ClientMedium;
import com.mytlogos.enterprise.background.api.model.ClientNews;
import com.mytlogos.enterprise.background.api.model.ClientPart;
import com.mytlogos.enterprise.background.api.model.ClientRelease;
import com.mytlogos.enterprise.background.api.model.ClientUser;
import com.mytlogos.enterprise.background.resourceLoader.LoadWorkGenerator;
import com.mytlogos.enterprise.background.room.model.RoomEpisode;
import com.mytlogos.enterprise.background.room.model.RoomExternalMediaList;
import com.mytlogos.enterprise.background.room.model.RoomExternalUser;
import com.mytlogos.enterprise.background.room.model.RoomMediaList;
import com.mytlogos.enterprise.background.room.model.RoomMedium;
import com.mytlogos.enterprise.background.room.model.RoomNews;
import com.mytlogos.enterprise.background.room.model.RoomPart;
import com.mytlogos.enterprise.background.room.model.RoomRelease;
import com.mytlogos.enterprise.background.room.model.RoomToDownload;
import com.mytlogos.enterprise.background.room.model.RoomUser;
import com.mytlogos.enterprise.model.ToDownload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RoomConverter {

    private final LoadData loadedData;

    public RoomConverter(LoadData loadedData) {
        this.loadedData = loadedData;
    }

    public RoomConverter() {
        this(new LoadData());
    }

    public List<RoomExternalMediaList> convertExternalMediaList(Collection<ClientExternalMediaList> mediaLists) {
        List<RoomExternalMediaList> externalMediaLists = new ArrayList<>(mediaLists.size());

        for (ClientExternalMediaList mediaList : mediaLists) {
            RoomExternalMediaList externalMediaList = this.convert(mediaList);
            externalMediaLists.add(externalMediaList);
        }

        return externalMediaLists;
    }

    public List<RoomMediaList> convertMediaList(Collection<ClientMediaList> mediaLists) {
        List<RoomMediaList> lists = new ArrayList<>(mediaLists.size());

        for (ClientMediaList mediaList : mediaLists) {
            lists.add(this.convert(mediaList));
        }

        return lists;
    }

    public List<RoomExternalUser> convertExternalUser(Collection<ClientExternalUser> mediaLists) {
        List<RoomExternalUser> externalUsers = new ArrayList<>(mediaLists.size());

        for (ClientExternalUser user : mediaLists) {
            externalUsers.add(this.convert(user));
        }

        return externalUsers;
    }

    public List<RoomExternalMediaList.ExternalListMediaJoin> convertExListJoin(Collection<LoadWorkGenerator.ListJoin> mediaLists) {
        List<RoomExternalMediaList.ExternalListMediaJoin> mediaJoins = new ArrayList<>(mediaLists.size());

        for (LoadWorkGenerator.ListJoin listJoin : mediaLists) {
            mediaJoins.add(this.convertToExtListJoin(listJoin));
        }

        return mediaJoins;
    }

    public List<RoomMediaList.MediaListMediaJoin> convertListJoin(Collection<LoadWorkGenerator.ListJoin> joins) {
        List<RoomMediaList.MediaListMediaJoin> externalMediaLists = new ArrayList<>(joins.size());

        for (LoadWorkGenerator.ListJoin mediaList : joins) {
            externalMediaLists.add(this.convertToListJoin(mediaList));
        }
        return externalMediaLists;
    }

    public List<RoomEpisode> convertEpisodes(Collection<ClientEpisode> episodes) {
        List<RoomEpisode> roomEpisodes = new ArrayList<>(episodes.size());
        for (ClientEpisode episode : episodes) {
            roomEpisodes.add(this.convert(episode));
        }
        return roomEpisodes;
    }

    public List<RoomRelease> convertReleases(Collection<ClientRelease> releases) {
        List<RoomRelease> roomEpisodes = new ArrayList<>(releases.size());
        for (ClientRelease release : releases) {
            roomEpisodes.add(this.convert(release));
        }
        return roomEpisodes;
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
        List<RoomPart> roomParts = new ArrayList<>();

        for (ClientPart part : parts) {
            roomParts.add(this.convert(part));
        }
        return roomParts;
    }

    public List<RoomToDownload> convertToDownload(Collection<ToDownload> toDownloads) {
        List<RoomToDownload> roomToDownloads = new ArrayList<>();

        for (ToDownload toDownload : toDownloads) {
            roomToDownloads.add(this.convert(toDownload));
        }
        return roomToDownloads;
    }

    public List<ToDownload> convertRoomToDownload(Collection<RoomToDownload> roomToDownloads) {
        List<ToDownload> toDownloads = new ArrayList<>();

        for (RoomToDownload roomToDownload : roomToDownloads) {
            toDownloads.add(this.convert(roomToDownload));
        }
        return toDownloads;
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
                release.getReleaseDate()
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
}
