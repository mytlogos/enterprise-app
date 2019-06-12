package com.mytlogos.enterprise.background.room.model;

import androidx.room.Embedded;

public class RoomListView {
    @Embedded
    private RoomMediaList mediaList;

    private int size;

    public RoomListView(RoomMediaList mediaList, int size) {
        this.mediaList = mediaList;
        this.size = size;
    }

    public RoomMediaList getMediaList() {
        return mediaList;
    }

    public int getSize() {
        return size;
    }
}
