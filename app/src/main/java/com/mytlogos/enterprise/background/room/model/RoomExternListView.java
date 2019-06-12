package com.mytlogos.enterprise.background.room.model;

import androidx.room.Embedded;

public class RoomExternListView {
    @Embedded
    private RoomExternalMediaList mediaList;

    private int size;

    public RoomExternListView(RoomExternalMediaList mediaList, int size) {
        this.mediaList = mediaList;
        this.size = size;
    }

    public RoomExternalMediaList getMediaList() {
        return mediaList;
    }

    public int getSize() {
        return size;
    }
}
