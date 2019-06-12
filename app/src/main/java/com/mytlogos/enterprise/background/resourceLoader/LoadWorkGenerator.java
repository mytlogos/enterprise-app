package com.mytlogos.enterprise.background.resourceLoader;

import com.mytlogos.enterprise.background.LoadData;
import com.mytlogos.enterprise.background.api.model.ClientEpisode;
import com.mytlogos.enterprise.background.api.model.ClientExternalMediaList;
import com.mytlogos.enterprise.background.api.model.ClientExternalUser;
import com.mytlogos.enterprise.background.api.model.ClientMediaList;
import com.mytlogos.enterprise.background.api.model.ClientMedium;
import com.mytlogos.enterprise.background.api.model.ClientPart;
import com.mytlogos.enterprise.background.api.model.ClientReadEpisode;
import com.mytlogos.enterprise.background.api.model.ClientRelease;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LoadWorkGenerator {
    private final LoadData loadedData;

    public LoadWorkGenerator(LoadData loadedData) {
        this.loadedData = loadedData;
    }

    public FilteredReadEpisodes filterReadEpisodes(Collection<ClientReadEpisode> readEpisodes) {
        FilteredReadEpisodes container = new FilteredReadEpisodes();

        for (ClientReadEpisode readEpisode : readEpisodes) {
            int episodeId = readEpisode.getEpisodeId();

            if (this.isEpisodeLoaded(episodeId)) {
                container.episodeList.add(readEpisode);
            } else {
                container.dependencies.add(new IntDependency<>(episodeId, readEpisode));
            }
        }

        return container;
    }

    public FilteredParts filterParts(Collection<ClientPart> parts) {
        List<ClientEpisode> episodes = new ArrayList<>();
        FilteredParts filteredParts = new FilteredParts();

        for (ClientPart part : parts) {
            if (this.isMediumLoaded(part.getMediumId())) {
                if (this.isPartLoaded(part.getId())) {
                    filteredParts.updateParts.add(part);
                } else {
                    filteredParts.newParts.add(part);
                }
                Collections.addAll(episodes, part.getEpisodes());
            } else {
                filteredParts.mediumDependencies.add(new IntDependency<>(part.getMediumId(), part));
            }
        }
        filteredParts.episodes.addAll(episodes);
        return filteredParts;
    }

    public FilteredEpisodes filterEpisodes(Collection<ClientEpisode> episodes) {
        FilteredEpisodes filteredEpisodes = new FilteredEpisodes();

        for (ClientEpisode episode : episodes) {
            int partId = episode.getPartId();

            if (!this.isPartLoaded(partId)) {
                filteredEpisodes.partDependencies.add(new IntDependency<>(partId, episode));
                continue;
            }

            if (this.isEpisodeLoaded(episode.getId())) {
                filteredEpisodes.updateEpisodes.add(episode);
            } else {
                filteredEpisodes.newEpisodes.add(episode);
            }
            Collections.addAll(filteredEpisodes.releases, episode.getReleases());
        }

        return filteredEpisodes;
    }

    public FilteredMedia filterMedia(Collection<ClientMedium> media) {
        FilteredMedia filteredMedia = new FilteredMedia();

        for (ClientMedium medium : media) {
            int currentRead = medium.getCurrentRead();

            // id can never be zero
            if (!this.isEpisodeLoaded(currentRead) && currentRead > 0) {
                filteredMedia.episodeDependencies.add(new IntDependency<>(currentRead, medium));
            }
            if (this.isMediumLoaded(medium.getId())) {
                filteredMedia.updateMedia.add(medium);
            } else {
                filteredMedia.newMedia.add(medium);
            }
            for (int part : medium.getParts()) {
                // todo check if it should be checked that medium is loaded
                if (!this.isPartLoaded(part)) {
                    filteredMedia.unloadedParts.add(part);
                }
            }
        }
        return filteredMedia;
    }

    public FilteredMediaList filterMediaLists(Collection<ClientMediaList> mediaLists) {
        FilteredMediaList filteredMediaList = new FilteredMediaList();

        for (ClientMediaList mediaList : mediaLists) {
            if (this.isMediaListLoaded(mediaList.getId())) {
                filteredMediaList.updateList.add(mediaList);
            } else {
                filteredMediaList.newList.add(mediaList);
            }

            Set<Integer> missingMedia = new HashSet<>();
            List<ListJoin> currentJoins = new ArrayList<>();

            for (int item : mediaList.getItems()) {
                ListJoin join = new ListJoin(mediaList.getId(), item);

                if (!this.isMediumLoaded(item)) {
                    missingMedia.add(item);
                }
                currentJoins.add(join);
            }

            // if none medium is missing, just clear and add like normal
            if (missingMedia.isEmpty()) {
                filteredMediaList.joins.addAll(currentJoins);
                filteredMediaList.clearJoins.add(mediaList.getId());
            } else {
                // else load missing media with worker and clear and add afterwards
                for (Integer mediumId : missingMedia) {
                    filteredMediaList.mediumDependencies.add(new IntDependency<>(mediumId, currentJoins));
                }
            }
        }

        return filteredMediaList;
    }

    public FilteredExtMediaList filterExternalMediaLists(Collection<ClientExternalMediaList> externalMediaLists) {
        FilteredExtMediaList filteredExtMediaList = new FilteredExtMediaList();

        for (ClientExternalMediaList externalMediaList : externalMediaLists) {
            String externalUuid = externalMediaList.getUuid();

            if (!this.isExternalUserLoaded(externalUuid)) {
                filteredExtMediaList.userDependencies.add(new Dependency<>(externalUuid, externalMediaList));
                continue;
            }
            if (this.isExternalMediaListLoaded(externalMediaList.getId())) {
                filteredExtMediaList.updateList.add(externalMediaList);
            } else {
                filteredExtMediaList.newList.add(externalMediaList);
            }


            Set<Integer> missingMedia = new HashSet<>();
            List<ListJoin> currentJoins = new ArrayList<>();

            for (int item : externalMediaList.getItems()) {
                ListJoin join = new ListJoin(externalMediaList.getId(), item);

                if (!this.isMediumLoaded(item)) {
                    missingMedia.add(item);
                }
                currentJoins.add(join);
            }

            // if none medium is missing, just clear and add like normal
            if (missingMedia.isEmpty()) {
                filteredExtMediaList.joins.addAll(currentJoins);
                filteredExtMediaList.clearJoins.add(externalMediaList.getId());
            } else {
                // else load missing media with worker and clear and add afterwards
                for (Integer mediumId : missingMedia) {
                    filteredExtMediaList.mediumDependencies.add(new IntDependency<>(mediumId, currentJoins));
                }
            }

        }

        return filteredExtMediaList;
    }


    public FilteredExternalUser filterExternalUsers(Collection<ClientExternalUser> externalUsers) {
        FilteredExternalUser filteredExternalUser = new FilteredExternalUser();

        for (ClientExternalUser externalUser : externalUsers) {
            if (this.isExternalUserLoaded(externalUser.getUuid())) {
                filteredExternalUser.updateUser.add(externalUser);
            } else {
                filteredExternalUser.newUser.add(externalUser);
            }

            for (ClientExternalMediaList userList : externalUser.getLists()) {
                if (this.isExternalMediaListLoaded(userList.getId())) {
                    filteredExternalUser.updateList.add(userList);
                } else {
                    filteredExternalUser.newList.add(userList);
                }

                Set<Integer> missingMedia = new HashSet<>();
                List<ListJoin> currentJoins = new ArrayList<>();

                for (int item : userList.getItems()) {
                    ListJoin join = new ListJoin(userList.getId(), item);

                    if (!this.isMediumLoaded(item)) {
                        missingMedia.add(item);
                    }
                    currentJoins.add(join);
                }
                // if none medium is missing, just clear and add like normal
                if (missingMedia.isEmpty()) {
                    filteredExternalUser.joins.addAll(currentJoins);
                    filteredExternalUser.clearJoins.add(userList.getId());
                } else {
                    for (Integer mediumId : missingMedia) {
                        filteredExternalUser.mediumDependencies.add(new IntDependency<>(mediumId, currentJoins));
                    }
                }
            }
        }
        return filteredExternalUser;
    }

    public boolean isEpisodeLoaded(int id) {
        return this.loadedData.getEpisodes().contains(id);
    }

    public boolean isPartLoaded(int id) {
        return this.loadedData.getPart().contains(id);
    }

    public boolean isMediumLoaded(int id) {
        return this.loadedData.getMedia().contains(id);
    }

    public boolean isMediaListLoaded(int id) {
        return this.loadedData.getMediaList().contains(id);
    }

    public boolean isExternalMediaListLoaded(int id) {
        return this.loadedData.getExternalMediaList().contains(id);
    }

    public boolean isExternalUserLoaded(String uuid) {
        return this.loadedData.getExternalUser().contains(uuid);
    }

    public boolean isNewsLoaded(int id) {
        return this.loadedData.getNews().contains(id);
    }

    public static class FilteredExternalUser {
        public final List<ClientExternalUser> newUser = new ArrayList<>();
        public final List<ClientExternalUser> updateUser = new ArrayList<>();
        public final List<ClientExternalMediaList> newList = new ArrayList<>();
        public final List<ClientExternalMediaList> updateList = new ArrayList<>();
        public final List<ListJoin> joins = new ArrayList<>();
        public final List<Integer> clearJoins = new ArrayList<>();
        public final List<IntDependency<List<LoadWorkGenerator.ListJoin>>> mediumDependencies = new ArrayList<>();
    }

    public static class FilteredExtMediaList {
        public final List<ClientExternalMediaList> newList = new ArrayList<>();
        public final List<ClientExternalMediaList> updateList = new ArrayList<>();
        public final List<ListJoin> joins = new ArrayList<>();
        public final List<Integer> clearJoins = new ArrayList<>();
        public final List<IntDependency<List<LoadWorkGenerator.ListJoin>>> mediumDependencies = new ArrayList<>();
        public final List<Dependency<String, ClientExternalMediaList>> userDependencies = new ArrayList<>();
    }

    public static class FilteredMediaList {
        public final List<ClientMediaList> newList = new ArrayList<>();
        public final List<ClientMediaList> updateList = new ArrayList<>();
        public final List<ListJoin> joins = new ArrayList<>();
        public final List<Integer> clearJoins = new ArrayList<>();
        public final List<IntDependency<List<LoadWorkGenerator.ListJoin>>> mediumDependencies = new ArrayList<>();
    }

    public static class ListJoin {
        public final int listId;
        public final int mediumId;

        ListJoin(int listId, int mediumId) {
            this.listId = listId;
            this.mediumId = mediumId;
        }
    }

    public static class FilteredMedia {
        public final List<ClientMedium> newMedia = new ArrayList<>();
        public final List<ClientMedium> updateMedia = new ArrayList<>();
        public final List<Integer> unloadedParts = new ArrayList<>();
        public final List<IntDependency<ClientMedium>> episodeDependencies = new ArrayList<>();
    }

    public static class FilteredParts {
        public final List<ClientPart> newParts = new ArrayList<>();
        public final List<ClientPart> updateParts = new ArrayList<>();
        public final List<IntDependency<ClientPart>> mediumDependencies = new ArrayList<>();
        public final List<ClientEpisode> episodes = new ArrayList<>();
    }

    public static class FilteredEpisodes {
        public final List<ClientEpisode> newEpisodes = new ArrayList<>();
        public final List<ClientEpisode> updateEpisodes = new ArrayList<>();
        public final List<IntDependency<ClientEpisode>> partDependencies = new ArrayList<>();
        public final List<ClientRelease> releases = new ArrayList<>();
    }

    public static class FilteredReadEpisodes {
        public final List<ClientReadEpisode> episodeList = new ArrayList<>();
        public final List<IntDependency<ClientReadEpisode>> dependencies = new ArrayList<>();
    }

    public static class IntDependency<T> {
        public final int id;
        public final T dependency;

        IntDependency(int id, T dependency) {
            this.id = id;
            this.dependency = dependency;
        }
    }

    public static class Dependency<V, T> {
        public final V id;
        public final T dependency;

        Dependency(V id, T dependency) {
            this.id = id;
            this.dependency = dependency;
        }
    }


}

