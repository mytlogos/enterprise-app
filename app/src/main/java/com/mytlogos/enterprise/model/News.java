package com.mytlogos.enterprise.model;

import com.mytlogos.enterprise.Formatter;

import org.joda.time.DateTime;

public class News {
    private final String title;
    private final DateTime timeStamp;
    private final int id;
    private final boolean read;
    private final String url;
    private final int mediumType;

    public News(String title, DateTime timeStamp, int id, boolean read, String url) {
        this(title, timeStamp, id, read, url, MediumType.ALL);
    }

    public News(String title, DateTime timeStamp, int id, boolean read, String url, int mediumType) {
        this.title = title;
        this.timeStamp = timeStamp;
        this.id = id;
        this.read = read;
        this.url = url;
        this.mediumType = mediumType;
    }

    public int getMediumType() {
        return mediumType;
    }

    public String getTimeStampString() {
        return Formatter.formatDateTime(this.getTimeStamp());
    }


    public String getTitle() {
        return this.title;
    }


    public int getNewsId() {
        return this.id;
    }


    public boolean isRead() {
        return this.read;
    }


    public DateTime getTimeStamp() {
        return this.timeStamp;
    }


    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "News{" +
                "title='" + title + '\'' +
                ", timeStamp=" + timeStamp +
                ", id=" + id +
                ", read=" + read +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        News news = (News) o;

        if (id != news.id) return false;
        if (isRead() != news.isRead()) return false;
        if (getTitle() != null ? !getTitle().equals(news.getTitle()) : news.getTitle() != null)
            return false;
        return getTimeStamp() != null ? getTimeStamp().equals(news.getTimeStamp()) : news.getTimeStamp() == null;
    }

    @Override
    public int hashCode() {
        int result = getTitle() != null ? getTitle().hashCode() : 0;
        result = 31 * result + (getTimeStamp() != null ? getTimeStamp().hashCode() : 0);
        result = 31 * result + id;
        result = 31 * result + (isRead() ? 1 : 0);
        return result;
    }
}
