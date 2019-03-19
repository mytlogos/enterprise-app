package com.mytlogos.enterprise.background.api.model;

public class ClientExternalUser {
    private String uuid;
    private String identifier;
    private int type;
    private ClientExternalMediaList[] lists;

    public ClientExternalUser(String uuid, String identifier, int type, ClientExternalMediaList[] lists) {
        this.uuid = uuid;
        this.identifier = identifier;
        this.type = type;
        this.lists = lists;
    }

    public String getUuid() {
        return uuid;
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getType() {
        return type;
    }

    public ClientExternalMediaList[] getLists() {
        return lists;
    }
}
