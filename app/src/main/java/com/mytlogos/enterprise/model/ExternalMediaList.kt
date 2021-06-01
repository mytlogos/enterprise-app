package com.mytlogos.enterprise.model;

public class ExternalMediaList extends MediaList {
    private final String url;

    public ExternalMediaList(String uuid, int listId, String name, int medium, String url, int size) {
        super(uuid, listId, name, medium, size);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

}
