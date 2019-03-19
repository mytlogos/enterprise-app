package com.mytlogos.enterprise.background.api.model;

public class ClientPart {
    private int id;
    private String title;
    private int totalIndex;
    private int partialIndex;
    private ClientEpisode[] episodes;

    public ClientPart(int id, String title, int totalIndex, int partialIndex, ClientEpisode[] episodes) {
        this.id = id;
        this.title = title;
        this.totalIndex = totalIndex;
        this.partialIndex = partialIndex;
        this.episodes = episodes;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getTotalIndex() {
        return totalIndex;
    }

    public int getPartialIndex() {
        return partialIndex;
    }

    public ClientEpisode[] getEpisodes() {
        return episodes;
    }
}
