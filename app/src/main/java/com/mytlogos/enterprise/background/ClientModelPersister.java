package com.mytlogos.enterprise.background;

import com.mytlogos.enterprise.background.api.model.ClientEpisode;
import com.mytlogos.enterprise.background.api.model.ClientExternalMediaList;
import com.mytlogos.enterprise.background.api.model.ClientExternalUser;
import com.mytlogos.enterprise.background.api.model.ClientListQuery;
import com.mytlogos.enterprise.background.api.model.ClientMediaList;
import com.mytlogos.enterprise.background.api.model.ClientMedium;
import com.mytlogos.enterprise.background.api.model.ClientNews;
import com.mytlogos.enterprise.background.api.model.ClientPart;
import com.mytlogos.enterprise.background.api.model.ClientReadEpisode;
import com.mytlogos.enterprise.background.api.model.ClientUser;
import com.mytlogos.enterprise.model.User;

public interface ClientModelPersister {
    void persist(ClientEpisode... episode);

    void persist(String uuid, ClientMediaList... mediaLists);

    void persist(String externalUuid, ClientExternalMediaList... externalMediaLists);

    void persist(String uuid, ClientExternalUser... externalUsers);

    void persist(String uuid, ClientListQuery... listQueries);

    void persist(ClientMedium... media);

    void persist(ClientNews... news);

    void persist(int mediumId, ClientPart... parts);

    User persist(ClientUser user);

    void persist(ClientReadEpisode[] readMedia);

    void finish();
}
