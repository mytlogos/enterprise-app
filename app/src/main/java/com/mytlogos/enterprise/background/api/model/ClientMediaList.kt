package com.mytlogos.enterprise.background.api.model;

import java.util.Arrays;

/**
 * API Model for List.
 */
public class ClientMediaList {
    private final String userUuid;
    private final int id;
    private final String name;
    private final int medium;
    private final int[] items;

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

        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return getId();
    }
}
