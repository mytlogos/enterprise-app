package com.mytlogos.enterprise.background.api.model;

import java.util.Arrays;

public class ClientMediaList {
    private String userUuid;
    private int id;
    private String name;
    private int medium;
    private int[] items;

    public ClientMediaList(String userUuid, int id, String name, int medium, int[] items) {
        this.userUuid = userUuid;
        this.id = id;
        this.name = name;
        this.medium = medium;
        this.items = items;
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

    public int[] getItems() {
        return items;
    }

    public String getUserUuid() {
        return userUuid;
    }

    @Override
    public String toString() {
        return "ClientMediaList{" +
                "userUuid='" + userUuid + '\'' +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", medium=" + medium +
                ", items=" + Arrays.toString(items) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientMediaList that = (ClientMediaList) o;

        if (getId() != that.getId()) return false;
        if (getMedium() != that.getMedium()) return false;
        if (getUserUuid() != null ? !getUserUuid().equals(that.getUserUuid()) : that.getUserUuid() != null)
            return false;
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null)
            return false;
        return Arrays.equals(getItems(), that.getItems());
    }

    @Override
    public int hashCode() {
        int result = getUserUuid() != null ? getUserUuid().hashCode() : 0;
        result = 31 * result + getId();
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + getMedium();
        result = 31 * result + Arrays.hashCode(getItems());
        return result;
    }
}
