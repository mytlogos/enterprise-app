package com.mytlogos.enterprise.background.api.model;

import java.util.Arrays;

/**
 * API Model for DisplayExternalUser.
 */
public class ClientExternalUser {
    private final String localUuid;
    private final String uuid;
    private final String identifier;
    private final int type;
    private final ClientExternalMediaList[] lists;

    public ClientExternalUser(String localUuid, String uuid, String identifier, int type, ClientExternalMediaList[] lists) {
        this.localUuid = localUuid;
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

    public String getLocalUuid() {
        return localUuid;
    }

    @Override
    public String toString() {
        return "ClientExternalUser{" +
                "localUuid='" + localUuid + '\'' +
                ", uuid='" + uuid + '\'' +
                ", identifier='" + identifier + '\'' +
                ", type=" + type +
                ", lists=" + Arrays.toString(lists) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientExternalUser that = (ClientExternalUser) o;

        return getUuid() != null ? getUuid().equals(that.getUuid()) : that.getUuid() == null;
    }

    @Override
    public int hashCode() {
        return getUuid() != null ? getUuid().hashCode() : 0;
    }
}
