package com.mytlogos.enterprise.background.api.model;

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
