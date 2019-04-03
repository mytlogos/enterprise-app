package com.mytlogos.enterprise.background;

import com.mytlogos.enterprise.background.api.model.ClientEpisode;
import com.mytlogos.enterprise.background.api.model.ClientExternalMediaList;
import com.mytlogos.enterprise.background.api.model.ClientExternalUser;
import com.mytlogos.enterprise.background.api.model.ClientListQuery;
import com.mytlogos.enterprise.background.api.model.ClientMediaList;
import com.mytlogos.enterprise.background.api.model.ClientMedium;
import com.mytlogos.enterprise.background.api.model.ClientMultiListQuery;
import com.mytlogos.enterprise.background.api.model.ClientNews;
import com.mytlogos.enterprise.background.api.model.ClientPart;
import com.mytlogos.enterprise.background.api.model.ClientReadEpisode;
import com.mytlogos.enterprise.background.api.model.ClientUpdateUser;
import com.mytlogos.enterprise.background.api.model.ClientUser;

import java.util.Arrays;
import java.util.Collection;

public interface ClientModelPersister {
    Collection<ClientConsumer<?>> getConsumer();

    default ClientModelPersister persist(ClientEpisode... episode) {
        return this.persistEpisodes(Arrays.asList(episode));
    }

    ClientModelPersister persistEpisodes(Collection<ClientEpisode> episode);

    default ClientModelPersister persist(ClientMediaList... mediaLists) {
        return this.persistMediaLists(Arrays.asList(mediaLists));
    }

    ClientModelPersister persistMediaLists(Collection<ClientMediaList> mediaLists);

    default ClientModelPersister persist(ClientExternalMediaList... externalMediaLists) {
        return this.persistExternalMediaLists(Arrays.asList(externalMediaLists));
    }

    ClientModelPersister persistExternalMediaLists(Collection<ClientExternalMediaList> externalMediaLists);

    default ClientModelPersister persist(ClientExternalUser... externalUsers) {
        return this.persistExternalUsers(Arrays.asList(externalUsers));
    }

    ClientModelPersister persistExternalUsers(Collection<ClientExternalUser> externalUsers);

    default ClientModelPersister persist(ClientMedium... media) {
        return this.persistMedia(Arrays.asList(media));
    }

    ClientModelPersister persistMedia(Collection<ClientMedium> media);

    default ClientModelPersister persist(ClientNews... news) {
        return this.persistNews(Arrays.asList(news));
    }

    ClientModelPersister persistNews(Collection<ClientNews> news);

    default ClientModelPersister persist(ClientPart... parts) {
        return this.persistParts(Arrays.asList(parts));
    }

    ClientModelPersister persistParts(Collection<ClientPart> parts);

    ClientModelPersister persist(ClientListQuery query);

    ClientModelPersister persist(ClientMultiListQuery query);

    ClientModelPersister persist(ClientUser user);

    ClientModelPersister persist(ClientUpdateUser user);

    default ClientModelPersister persist(ClientReadEpisode... readEpisodes) {
        return this.persistReadEpisodes(Arrays.asList(readEpisodes));
    }

    ClientModelPersister persistReadEpisodes(Collection<ClientReadEpisode> readMedia);

    void finish();
}
