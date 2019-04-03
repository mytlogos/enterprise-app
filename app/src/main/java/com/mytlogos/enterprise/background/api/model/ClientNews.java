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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientNews that = (ClientNews) o;

        if (getId() != that.getId()) return false;
        if (isRead() != that.isRead()) return false;
        if (getTitle() != null ? !getTitle().equals(that.getTitle()) : that.getTitle() != null)
            return false;
        if (getLink() != null ? !getLink().equals(that.getLink()) : that.getLink() != null)
            return false;
        return getDate() != null ? getDate().equals(that.getDate()) : that.getDate() == null;
    }

    @Override
    public int hashCode() {
        int result = getTitle() != null ? getTitle().hashCode() : 0;
        result = 31 * result + (getLink() != null ? getLink().hashCode() : 0);
        result = 31 * result + (getDate() != null ? getDate().hashCode() : 0);
        result = 31 * result + getId();
        result = 31 * result + (isRead() ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientNews{" +
                "title='" + title + '\'' +
                ", link='" + link + '\'' +
                ", date=" + date +
                ", id=" + id +
                ", read=" + read +
                '}';
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
