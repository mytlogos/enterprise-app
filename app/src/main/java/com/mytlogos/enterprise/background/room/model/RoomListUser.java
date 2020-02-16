package com.mytlogos.enterprise.background.room.model;

public class RoomListUser {
    private final String uuid;
    private final int listId;

    public RoomListUser(String uuid, int listId) {
        this.uuid = uuid;
        this.listId = listId;
    }

    public String getUuid() {
        return uuid;
    }

    public int getListId() {
        return listId;
    }

    @Override
    public String toString() {
        return "RoomListUser{" +
                "uuid='" + uuid + '\'' +
                ", listId=" + listId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomListUser that = (RoomListUser) o;

        if (getListId() != that.getListId()) return false;
        return getUuid().equals(that.getUuid());
    }

    @Override
    public int hashCode() {
        int result = getUuid().hashCode();
        result = 31 * result + getListId();
        return result;
    }
}
