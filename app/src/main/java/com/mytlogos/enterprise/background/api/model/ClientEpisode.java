package com.mytlogos.enterprise.background.api.model;

import org.joda.time.DateTime;

public class ClientEpisode {
    private int id;
    private float progress;
    private DateTime readDate;
    private int partId;
    private String title;
    private int totalIndex;
    private int partialIndex;
    private String url;
    private DateTime releaseDate;

    public ClientEpisode(int id, float progress, DateTime readDate, int partId, String title, int totalIndex, int partialIndex,
                         String url, DateTime releaseDate) {
        this.id = id;
        this.progress = progress;
        this.readDate = readDate;
        this.partId = partId;
        this.title = title;
        this.totalIndex = totalIndex;
        this.partialIndex = partialIndex;
        this.url = url;
        this.releaseDate = releaseDate;
    }

    public int getId() {
        return id;
    }

    public int getPartId() {
        return partId;
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

    public String getUrl() {
        return url;
    }

    public DateTime getReleaseDate() {
        return releaseDate;
    }

    public float getProgress() {
        return progress;
    }

    public DateTime getReadDate() {
        return readDate;
    }
}
