package com.mytlogos.enterprise.background.api.model;

import java.util.Arrays;

public class ClientExternalUser {
    private String localUuid;
    private String uuid;
    private String identifier;
    private int type;
    private ClientExternalMediaList[] lists;

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

        if (getType() != that.getType()) return false;
        if (getLocalUuid() != null ? !getLocalUuid().equals(that.getLocalUuid()) : that.getLocalUuid() != null)
            return false;
        if (getUuid() != null ? !getUuid().equals(that.getUuid()) : that.getUuid() != null)
            return false;
        if (getIdentifier() != null ? !getIdentifier().equals(that.getIdentifier()) : that.getIdentifier() != null)
            return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(getLists(), that.getLists());
    }

    @Override
    public int hashCode() {
        int result = getLocalUuid() != null ? getLocalUuid().hashCode() : 0;
        result = 31 * result + (getUuid() != null ? getUuid().hashCode() : 0);
        result = 31 * result + (getIdentifier() != null ? getIdentifier().hashCode() : 0);
        result = 31 * result + getType();
        result = 31 * result + Arrays.hashCode(getLists());
        return result;
    }
}
