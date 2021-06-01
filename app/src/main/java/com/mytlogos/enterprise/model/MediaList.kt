package com.mytlogos.enterprise.model;

public class MediaList {
    private final String uuid;
    private final int listId;
    private final String name;
    private final int medium;
    private final int size;

    public MediaList(String uuid, int listId, String name, int medium, int size) {
        this.uuid = uuid;
        this.listId = listId;
        this.name = name;
        this.medium = medium;
        this.size = size;
    }

    public String getUuid() {
        return uuid;
    }

    public int getListId() {
        return listId;
    }

    public String getName() {
        return name;
    }

    public int getMedium() {
        return medium;
    }

    public int getSize() {
        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MediaList mediaList = (MediaList) o;

        return listId == mediaList.listId;
    }

    @Override
    public int hashCode() {
        return listId;
    }
}
