package com.mytlogos.enterprise.background.api.model;

import org.joda.time.DateTime;

public class ClientNews {
    private String title;
    private String link;
    private DateTime date;
    private int id;
    private boolean read;

    public ClientNews(String title, String link, DateTime date, int id, boolean read) {
        this.title = title;
        this.link = link;
        this.date = date;
        this.id = id;
        this.read = read;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public DateTime getDate() {
        return date;
    }

    public int getId() {
        return id;
    }

    public boolean isRead() {
        return read;
    }
}
