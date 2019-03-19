package com.mytlogos.enterprise.background.api.model;

public class ClientListQuery {
    private ClientMediaList list;
    private ClientMedium[] media;

    public ClientListQuery(ClientMediaList list, ClientMedium[] media) {
        this.list = list;
        this.media = media;
    }

    public ClientMedium[] getMedia() {
        return media;
    }

    public ClientMediaList getList() {
        return list;
    }
}
