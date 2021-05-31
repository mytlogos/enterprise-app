package com.mytlogos.enterprise.background.api.model;

/**
 * API Model for MinList.
 */
public class ClientMinList {
    private final String name;
    private final int medium;

    public ClientMinList(String name, int medium) {
        this.name = name;
        this.medium = medium;
    }

    public String getName() {
        return name;
    }

    public int getMedium() {
        return medium;
    }

    @Override
    public String toString() {
        return "ClientMinList{" +
                "name='" + name + '\'' +
                ", medium=" + medium +
                '}';
    }
}
