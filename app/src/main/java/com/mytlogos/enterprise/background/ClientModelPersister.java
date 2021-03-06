package com.mytlogos.enterprise.background;

import com.mytlogos.enterprise.background.api.model.ClientEpisode;
import com.mytlogos.enterprise.background.api.model.ClientExternalMediaList;
import com.mytlogos.enterprise.background.api.model.ClientExternalUser;
import com.mytlogos.enterprise.background.api.model.ClientExternalUserPure;
import com.mytlogos.enterprise.background.api.model.ClientListQuery;
import com.mytlogos.enterprise.background.api.model.ClientMediaList;
import com.mytlogos.enterprise.background.api.model.ClientMedium;
import com.mytlogos.enterprise.background.api.model.ClientMediumInWait;
import com.mytlogos.enterprise.background.api.model.ClientMultiListQuery;
import com.mytlogos.enterprise.background.api.model.ClientNews;
import com.mytlogos.enterprise.background.api.model.ClientPart;
import com.mytlogos.enterprise.background.api.model.ClientPartPure;
import com.mytlogos.enterprise.background.api.model.ClientReadEpisode;
import com.mytlogos.enterprise.background.api.model.ClientRelease;
import com.mytlogos.enterprise.background.api.model.ClientSimpleMedium;
import com.mytlogos.enterprise.background.api.model.ClientSimpleRelease;
import com.mytlogos.enterprise.background.api.model.ClientSimpleUser;
import com.mytlogos.enterprise.background.api.model.ClientStat;
import com.mytlogos.enterprise.background.api.model.ClientUpdateUser;
import com.mytlogos.enterprise.background.api.model.ClientUser;
import com.mytlogos.enterprise.background.api.model.ClientUserList;
import com.mytlogos.enterprise.background.resourceLoader.LoadWorkGenerator;
import com.mytlogos.enterprise.model.ToDownload;
import com.mytlogos.enterprise.model.Toc;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface ClientModelPersister {
    Collection<ClientConsumer<?>> getConsumer();

    default ClientModelPersister persist(ClientEpisode... episode) {
        return this.persistEpisodes(Arrays.asList(episode));
    }

    ClientModelPersister persistEpisodes(Collection<ClientEpisode> episode);

    ClientModelPersister persistReleases(Collection<ClientRelease> releases);

    default ClientModelPersister persist(ClientMediaList... mediaLists) {
        return this.persistMediaLists(Arrays.asList(mediaLists));
    }

    ClientModelPersister persist(LoadWorkGenerator.FilteredEpisodes filteredEpisodes);

    ClientModelPersister persistMediaLists(List<ClientMediaList> mediaLists);

    ClientModelPersister persistUserLists(List<ClientUserList> mediaLists);

    default ClientModelPersister persist(ClientExternalMediaList... externalMediaLists) {
        return this.persistExternalMediaLists(Arrays.asList(externalMediaLists));
    }

    ClientModelPersister persist(LoadWorkGenerator.FilteredMediaList filteredMediaList);

    ClientModelPersister persistExternalMediaLists(Collection<ClientExternalMediaList> externalMediaLists);

    default ClientModelPersister persist(ClientExternalUser... externalUsers) {
        return this.persistExternalUsers(Arrays.asList(externalUsers));
    }

    ClientModelPersister persist(LoadWorkGenerator.FilteredExtMediaList filteredExtMediaList);

    ClientModelPersister persistExternalUsers(List<ClientExternalUser> externalUsers);

    default ClientModelPersister persistExternalUsersPure(List<ClientExternalUserPure> externalUsers) {
        List<ClientExternalUser> unpure = externalUsers.stream().map(value -> new ClientExternalUser(
                value.getLocalUuid(),
                value.getUuid(),
                value.getIdentifier(),
                value.getType(),
                new ClientExternalMediaList[0]
        )).collect(Collectors.toList());
        return this.persistExternalUsers(unpure);
    }

    default ClientModelPersister persist(ClientSimpleMedium... media) {
        return this.persistMedia(Arrays.asList(media));
    }

    ClientModelPersister persist(LoadWorkGenerator.FilteredExternalUser filteredExternalUser);

    ClientModelPersister persistMedia(Collection<ClientSimpleMedium> media);

    default ClientModelPersister persist(ClientNews... news) {
        return this.persistNews(Arrays.asList(news));
    }

    ClientModelPersister persist(LoadWorkGenerator.FilteredMedia filteredMedia);

    ClientModelPersister persistNews(Collection<ClientNews> news);

    default ClientModelPersister persist(ClientPart... parts) {
        return this.persistParts(Arrays.asList(parts));
    }

    ClientModelPersister persistParts(Collection<ClientPart> parts);

    default ClientModelPersister persistPartsPure(Collection<ClientPartPure> parts) {
        List<ClientPart> unPureParts = parts
                .stream()
                .map(part -> new ClientPart(
                        part.getMediumId(),
                        part.getId(),
                        part.getTitle(),
                        part.getTotalIndex(),
                        part.getPartialIndex(),
                        null
                ))
                .collect(Collectors.toList());
        this.persistParts(unPureParts);
        return this;
    }

    ClientModelPersister persist(LoadWorkGenerator.FilteredReadEpisodes filteredReadEpisodes);

    ClientModelPersister persist(ClientListQuery query);

    ClientModelPersister persist(ClientMultiListQuery query);

    ClientModelPersister persist(ClientUser user);

    ClientModelPersister persist(ClientUpdateUser user);

    ClientModelPersister persistToDownloads(Collection<ToDownload> toDownloads);

    default ClientModelPersister persist(ClientReadEpisode... readEpisodes) {
        return this.persistReadEpisodes(Arrays.asList(readEpisodes));
    }

    ClientModelPersister persist(LoadWorkGenerator.FilteredParts filteredParts);

    ClientModelPersister persistReadEpisodes(Collection<ClientReadEpisode> readMedia);

    ClientModelPersister persist(ClientStat.ParsedStat stat);

    void finish();

    ClientModelPersister persist(ToDownload toDownload);

    void persistMediaInWait(List<ClientMediumInWait> medium);

    ClientModelPersister persist(ClientSimpleUser user);

    void deleteLeftoverEpisodes(Map<Integer, List<Integer>> partEpisodes);

    Collection<Integer> deleteLeftoverReleases(Map<Integer, List<ClientSimpleRelease>> partReleases);

    void deleteLeftoverTocs(Map<Integer, List<String>> mediaTocs);

    ClientModelPersister persistTocs(Collection<? extends Toc> tocs);
}
