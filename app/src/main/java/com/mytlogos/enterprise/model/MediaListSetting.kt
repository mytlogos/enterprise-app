package com.mytlogos.enterprise.model;

public class MediaListSetting {
    private final int listId;
    private final String uuid;
    private final String name;
    private final int medium;
    private final int size;
    private final boolean toDownload;

    @SuppressWarnings("WeakerAccess")
    public MediaListSetting(int listId, String uuid, String name, int medium, int size, boolean toDownload) {
        this.listId = listId;
        this.uuid = uuid;
        this.name = name;
        this.medium = medium;
        this.size = size;
        this.toDownload = toDownload;
    }

    public String getUuid() {
        return uuid;
    }

    public boolean isToDownload() {
        return toDownload;
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

    public boolean isToDownloadMutable() {
        return true;
    }

    public boolean isNameMutable() {
        return true;
    }

    public boolean isMediumMutable() {
        return true;
    }
}
