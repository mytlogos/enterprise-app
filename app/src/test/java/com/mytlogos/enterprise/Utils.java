package com.mytlogos.enterprise;

import com.mytlogos.enterprise.background.api.model.ClientEpisode;
import com.mytlogos.enterprise.background.api.model.ClientExternalMediaList;
import com.mytlogos.enterprise.background.api.model.ClientExternalUser;
import com.mytlogos.enterprise.background.api.model.ClientMediaList;
import com.mytlogos.enterprise.background.api.model.ClientMedium;
import com.mytlogos.enterprise.background.api.model.ClientNews;
import com.mytlogos.enterprise.background.api.model.ClientPart;

import java.util.Collection;

public abstract class Utils {
    private Utils() {
        //no instance
    }

    public static ClientNews getNews(Collection<ClientNews> media, int id) {
        for (ClientNews clientNews : media) {
            if (clientNews.getId() == id) {
                return clientNews;
            }
        }
        throw new IllegalArgumentException("no item exists for id '" + id + "'");
    }

    public static boolean containsNewsId(Collection<ClientNews> media, int id) {
        for (ClientNews clientNews : media) {
            if (clientNews.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public static ClientMedium getMedium(Collection<ClientMedium> media, int id) {
        for (ClientMedium clientMedium : media) {
            if (clientMedium.getId() == id) {
                return clientMedium;
            }
        }
        throw new IllegalArgumentException("no item exists for id '" + id + "'");
    }

    public static boolean containsMediumId(Collection<ClientMedium> media, int id) {
        for (ClientMedium clientMedium : media) {
            if (clientMedium.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public static ClientPart getPart(Collection<ClientPart> parts, int id) {
        for (ClientPart part : parts) {
            if (part.getId() == id) {
                return part;
            }
        }
        throw new IllegalArgumentException("no item exists for id '" + id + "'");
    }

    public static boolean containsPartId(Collection<ClientPart> parts, int id) {
        for (ClientPart part : parts) {
            if (part.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public static ClientEpisode getEpisode(Collection<ClientEpisode> episodes, int id) {
        for (ClientEpisode episode : episodes) {
            if (episode.getId() == id) {
                return episode;
            }
        }
        throw new IllegalArgumentException("no item exists for id '" + id + "'");
    }

    public static boolean containsEpisodeId(Collection<ClientEpisode> episodes, int id) {
        for (ClientEpisode episode : episodes) {
            if (episode.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public static ClientMediaList getMediaList(Collection<ClientMediaList> mediaLists, int id) {
        for (ClientMediaList mediaList : mediaLists) {
            if (mediaList.getId() == id) {
                return mediaList;
            }
        }
        throw new IllegalArgumentException("no item exists for id '" + id + "'");
    }

    public static boolean containsMediaListId(Collection<ClientMediaList> mediaLists, int id) {
        for (ClientMediaList mediaList : mediaLists) {
            if (mediaList.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public static ClientExternalMediaList getExtMediaList(Collection<ClientExternalMediaList> lists, int id) {
        for (ClientExternalMediaList externalMediaList : lists) {
            if (externalMediaList.getId() == id) {
                return externalMediaList;
            }
        }
        throw new IllegalArgumentException("no item exists for id '" + id + "'");
    }

    public static boolean containsExtMediaListId(Collection<ClientExternalMediaList> lists, int id) {
        for (ClientExternalMediaList externalMediaList : lists) {
            if (externalMediaList.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public static ClientExternalUser getExtUser(Collection<ClientExternalUser> media, String id) {
        for (ClientExternalUser externalUser : media) {
            if (externalUser.getUuid().equals(id)) {
                return externalUser;
            }
        }
        throw new IllegalArgumentException("no item exists for id '" + id + "'");
    }

    public static boolean containsExtUserId(Collection<ClientExternalUser> media, String id) {
        for (ClientExternalUser externalUser : media) {
            if (externalUser.getUuid().equals(id)) {
                return true;
            }
        }
        return false;
    }
}
