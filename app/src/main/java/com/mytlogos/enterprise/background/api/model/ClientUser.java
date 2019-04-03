package com.mytlogos.enterprise.background.api.model;

import java.util.Arrays;

public class ClientUser {
    private String uuid;
    private String session;
    private String name;
    private ClientExternalUser[] externalUser;
    private ClientMediaList[] lists;
    private ClientNews[] unreadNews;
    private int[] unreadChapter;
    private ClientReadEpisode[] readToday;

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

        if (getUuid() != null ? !getUuid().equals(that.getUuid()) : that.getUuid() != null)
            return false;
        if (getSession() != null ? !getSession().equals(that.getSession()) : that.getSession() != null)
            return false;
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null)
            return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(getExternalUser(), that.getExternalUser())) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(getLists(), that.getLists())) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(getUnreadNews(), that.getUnreadNews())) return false;
        if (!Arrays.equals(getUnreadChapter(), that.getUnreadChapter())) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(getReadToday(), that.getReadToday());
    }

    @Override
    public int hashCode() {
        int result = getUuid() != null ? getUuid().hashCode() : 0;
        result = 31 * result + (getSession() != null ? getSession().hashCode() : 0);
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + Arrays.hashCode(getExternalUser());
        result = 31 * result + Arrays.hashCode(getLists());
        result = 31 * result + Arrays.hashCode(getUnreadNews());
        result = 31 * result + Arrays.hashCode(getUnreadChapter());
        result = 31 * result + Arrays.hashCode(getReadToday());
        return result;
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
