package com.mytlogos.enterprise.background.api.model;

import org.joda.time.DateTime;

public class ClientReadEpisode {
    private int episodeId;
    private DateTime readDate;
    private float progress;

    public ClientReadEpisode(int episodeId, DateTime readDate, float progress) {
        this.episodeId = episodeId;
        this.readDate = readDate;
        this.progress = progress;
    }


    public int getEpisodeId() {
        return episodeId;
    }

    public DateTime getReadDate() {
        return readDate;
    }

    public float getProgress() {
        return progress;
    }
}
