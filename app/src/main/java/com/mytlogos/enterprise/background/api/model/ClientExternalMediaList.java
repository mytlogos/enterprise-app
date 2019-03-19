package com.mytlogos.enterprise.background.api.model;

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
}
