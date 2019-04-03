package com.mytlogos.enterprise.background.api.model;

import java.util.Arrays;

public class ClientExternalMediaList {
    private String uuid;
    private int id;
    private String name;
    private int medium;
    private String url;
    private int[] items;

    public ClientExternalMediaList(String uuid, int id, String name, int medium, String url,
                                   int[] items) {
        this.uuid = uuid;
        this.id = id;
        this.name = name;
        this.medium = medium;
        this.url = url;
        this.items = items;
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

    public int[] getItems() {
        return items;
    }

    @Override
    public String toString() {
        return "ClientExternalMediaList{" +
                "uuid='" + uuid + '\'' +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", medium=" + medium +
                ", url='" + url + '\'' +
                ", items=" + Arrays.toString(items) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientExternalMediaList that = (ClientExternalMediaList) o;

        if (getId() != that.getId()) return false;
        if (getMedium() != that.getMedium()) return false;
        if (getUuid() != null ? !getUuid().equals(that.getUuid()) : that.getUuid() != null)
            return false;
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null)
            return false;
        if (getUrl() != null ? !getUrl().equals(that.getUrl()) : that.getUrl() != null)
            return false;
        return Arrays.equals(getItems(), that.getItems());
    }

    @Override
    public int hashCode() {
        int result = getUuid() != null ? getUuid().hashCode() : 0;
        result = 31 * result + getId();
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + getMedium();
        result = 31 * result + (getUrl() != null ? getUrl().hashCode() : 0);
        result = 31 * result + Arrays.hashCode(getItems());
        return result;
    }
}
