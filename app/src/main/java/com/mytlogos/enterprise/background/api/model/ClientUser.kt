package com.mytlogos.enterprise.background.api.model;

/**
 * API Model for User.
 */
public class ClientUser {
    private final String uuid;
    private final String session;
    private final String name;
    private final ClientExternalUser[] externalUser;
    private final ClientMediaList[] lists;
    private final ClientNews[] unreadNews;
    private final int[] unreadChapter;
    private final ClientReadEpisode[] readToday;

    public ClientUser(String uuid, String session, String name, ClientExternalUser[] externalUser, ClientMediaList[] lists, ClientNews[] unreadNews, int[] unreadChapter, ClientReadEpisode[] readToday) {
        this.uuid = uuid;
        this.session = session;
        this.name = name;
        this.externalUser = externalUser;
        this.lists = lists;
        this.unreadNews = unreadNews;
        this.unreadChapter = unreadChapter;
        this.readToday = readToday;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientUser that = (ClientUser) o;

        return getUuid().equals(that.getUuid());
    }

    @Override
    public int hashCode() {
        return getUuid().hashCode();
    }

    public String getUuid() {
        return uuid;
    }

    public String getSession() {
        return session;
    }

    public String getName() {
        return name;
    }

    public ClientExternalUser[] getExternalUser() {
        return externalUser;
    }

    public ClientMediaList[] getLists() {
        return lists;
    }

    public ClientNews[] getUnreadNews() {
        return unreadNews;
    }

    public int[] getUnreadChapter() {
        return unreadChapter;
    }

    public ClientReadEpisode[] getReadToday() {
        return readToday;
    }
}
