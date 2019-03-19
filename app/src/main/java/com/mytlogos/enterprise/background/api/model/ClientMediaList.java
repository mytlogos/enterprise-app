package com.mytlogos.enterprise.background.api.model;

public class ClientMediaList {
    private int id;
    private String name;
    private int medium;
    private int[] items;

    public ClientMediaList(int id, String name, int medium, int[] items) {
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
}
