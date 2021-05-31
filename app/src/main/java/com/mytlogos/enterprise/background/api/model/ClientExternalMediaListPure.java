package com.mytlogos.enterprise.background.api.model;

/**
 * API Model for PureExternalList.
 */
public class ClientExternalMediaListPure {
    private final String uuid;
    private final int id;
    private final String name;
    private final int medium;
    private final String url;

    public ClientExternalMediaListPure(String uuid, int id, String name, int medium, String url) {
        this.uuid = uuid;
        this.id = id;
        this.name = name;
        this.medium = medium;
        this.url = url;
    }

    public String getUuid() {
        return uuid;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMedium() {
        return medium;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "ClientExternalMediaList{" +
                "uuid='" + uuid + '\'' +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", medium=" + medium +
                ", url='" + url + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientExternalMediaListPure that = (ClientExternalMediaListPure) o;

        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return getId();
    }
}
