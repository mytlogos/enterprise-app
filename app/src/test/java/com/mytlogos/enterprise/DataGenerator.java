package com.mytlogos.enterprise;

import com.mytlogos.enterprise.background.api.model.ClientEpisode;
import com.mytlogos.enterprise.background.api.model.ClientExternalMediaList;
import com.mytlogos.enterprise.background.api.model.ClientExternalUser;
import com.mytlogos.enterprise.background.api.model.ClientMediaList;
import com.mytlogos.enterprise.background.api.model.ClientMedium;
import com.mytlogos.enterprise.background.api.model.ClientNews;
import com.mytlogos.enterprise.background.api.model.ClientPart;
import com.mytlogos.enterprise.background.api.model.ClientReadEpisode;
import com.mytlogos.enterprise.background.api.model.ClientRelease;
import com.mytlogos.enterprise.background.api.model.ClientUser;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DataGenerator {
    private ClientUser clientUser;
    private List<ClientPart> parts;
    private List<ClientEpisode> episodes;
    private List<ClientMediaList> mediaLists;
    private List<ClientExternalUser> externalUser;
    private List<ClientMedium> media;
    private List<ClientNews> news;
    private List<ClientReadEpisode> readEpisodes;
    private List<ClientExternalMediaList> extMediaLists;

    public DataGenerator() {
        this.externalUser = new ArrayList<>();
        this.mediaLists = new ArrayList<>();
        this.extMediaLists = new ArrayList<>();
        this.episodes = new ArrayList<>();
        this.parts = new ArrayList<>();
        this.media = new ArrayList<>();
        this.news = new ArrayList<>();
        this.readEpisodes = new ArrayList<>();
    }

    public ClientUser getClientUser() {
        return clientUser;
    }

    public List<ClientPart> getParts() {
        return parts;
    }

    public List<ClientEpisode> getEpisodes() {
        return episodes;
    }

    public List<ClientMediaList> getMediaLists() {
        return mediaLists;
    }

    public List<ClientExternalUser> getExternalUser() {
        return externalUser;
    }

    public List<ClientMedium> getMedia() {
        return media;
    }

    public List<ClientNews> getNews() {
        return news;
    }

    public List<ClientReadEpisode> getReadEpisodes() {
        return readEpisodes;
    }

    public List<ClientExternalMediaList> getExtMediaLists() {
        return extMediaLists;
    }

    public void generateCompleteTestData() {
        int currentPartId = 2000;
        int currentEpisodeId = 100000;

        for (int mediumId = 1; mediumId < 50; mediumId++) {
            List<Integer> mediumParts = new ArrayList<>();

            for (int pLimit = currentPartId + 20; currentPartId < pLimit; currentPartId++) {
                List<ClientEpisode> partEpisodes = new ArrayList<>();
                mediumParts.add(currentPartId);

                for (int eLimit = currentEpisodeId + 30; currentEpisodeId < eLimit; currentEpisodeId++) {
                    ClientEpisode episode = generateEpisode(currentEpisodeId, currentPartId);
                    partEpisodes.add(episode);
                    episodes.add(episode);
                }

                ClientPart part = generatePart(mediumId, currentPartId, partEpisodes);
                this.parts.add(part);
            }

            ClientMedium medium = generateMedium(mediumId, mediumParts, currentEpisodeId - 1);
            this.media.add(medium);
        }

        String uuid = "hello";
        String session = "my session id";

        int currentExListId = 1000;
        for (int i = 1; i < 6; i++) {
            List<ClientExternalMediaList> extMediaLists = new ArrayList<>();
            String exUuid = i + "";

            for (int limit = currentExListId + 6; currentExListId < limit; currentExListId++) {
                List<Integer> mediaIds = new ArrayList<>();

                for (int index = 0; index < media.size(); index += 3) {
                    mediaIds.add(media.get(index).getId());
                }
                extMediaLists.add(this.generateExternalMediaList(exUuid, currentExListId, mediaIds));
            }
            this.extMediaLists.addAll(extMediaLists);
            this.externalUser.add(this.generateExternalUser(uuid, exUuid, extMediaLists));
        }
        for (int i = 1; i < 6; i++) {
            List<Integer> mediaIds = new ArrayList<>();

            for (int index = 0; index < media.size(); index += 3) {
                ClientMedium medium = media.get(index);
                mediaIds.add(medium.getId());
            }
            this.mediaLists.add(this.generateMediaList(uuid, i, mediaIds));
        }

        for (int i = 0; i < 50 && i < episodes.size(); i++) {
            this.readEpisodes.add(new ClientReadEpisode(episodes.get(i).getId(), DateTime.now(), 1));
        }

        this.clientUser = this.generateUser(
                uuid,
                session,
                this.externalUser,
                this.mediaLists,
                this.news,
                this.readEpisodes
        );
    }

    public ClientMedium generateMedium(int id, List<Integer> partList, int currentRead) {
        return new ClientMedium(
                toIntArray(partList),
                new int[0],
                currentRead,
                new int[0],
                id,
                null,
                null,
                null,
                null,
                0,
                null,
                null,
                0,
                0,
                null,
                null
        );
    }

    public ClientUser generateUser(
            String uuid, String session,
            Collection<ClientExternalUser> externalUsers,
            Collection<ClientMediaList> mediaLists, Collection<ClientNews> news,
            Collection<ClientReadEpisode> readEpisodes
    ) {
        return new ClientUser(
                uuid,
                session,
                null,
                externalUsers.toArray(new ClientExternalUser[0]),
                mediaLists.toArray(new ClientMediaList[0]),
                news.toArray(new ClientNews[0]),
                new int[0],
                readEpisodes.toArray(new ClientReadEpisode[0])
        );
    }

    public ClientPart generatePart(int mediumId, int id, Collection<ClientEpisode> episodes) {
        return new ClientPart(
                mediumId,
                id,
                null,
                0,
                0,
                episodes.toArray(new ClientEpisode[0])
        );
    }

    public ClientEpisode generateEpisode(int id, int partId) {
        return new ClientEpisode(
                id,
                0,
                partId,
                0,
                0,
                0,
                null,
                new ClientRelease[0]
        );
    }

    public ClientExternalUser generateExternalUser(String localUuid, String uuid, Collection<ClientExternalMediaList> collection) {
        return new ClientExternalUser(
                localUuid,
                uuid,
                null,
                0,
                collection.toArray(new ClientExternalMediaList[0])
        );
    }

    public ClientExternalMediaList generateExternalMediaList(String uuid, int id, Collection<Integer> integers) {
        return new ClientExternalMediaList(
                uuid,
                id,
                null,
                0,
                null,
                toIntArray(integers)
        );
    }

    public ClientMediaList generateMediaList(String userUuid, int id, Collection<Integer> integers) {
        return new ClientMediaList(
                userUuid,
                id,
                null,
                0,
                toIntArray(integers)
        );
    }

    private int[] toIntArray(Collection<Integer> integers) {
        int[] ints = new int[integers.size()];
        int index = 0;
        for (Integer integer : integers) {
            ints[index] = integer;
            index++;
        }
        return ints;
    }
}
