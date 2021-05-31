package com.mytlogos.enterprise.background.api.model;

/**
 * API Model for UserList.
 */
public class ClientUserList {
    private final int id;
    private final String name;
    private final int medium;

    public ClientUserList(int id, String name, int medium) {
        this.id = id;
        this.name = name;
        this.medium = medium;
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

    @Override
    public String toString() {
        return "ClientMediaList{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", medium=" + medium +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientUserList that = (ClientUserList) o;

        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return getId();
    }
}
