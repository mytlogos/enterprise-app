package com.mytlogos.enterprise.background.api.model;

import java.util.Arrays;

/**
 * API Model for DisplayExternalUser.
 */
public class ClientExternalUserPure {
    private final String localUuid;
    private final String uuid;
    private final String identifier;
    private final int type;

    public ClientExternalUserPure(String localUuid, String uuid, String identifier, int type) {
        this.localUuid = localUuid;
        this.uuid = uuid;
        this.identifier = identifier;
        this.type = type;
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
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientExternalUserPure that = (ClientExternalUserPure) o;

        return getUuid() != null ? getUuid().equals(that.getUuid()) : that.getUuid() == null;
    }

    @Override
    public int hashCode() {
        return getUuid() != null ? getUuid().hashCode() : 0;
    }
}
