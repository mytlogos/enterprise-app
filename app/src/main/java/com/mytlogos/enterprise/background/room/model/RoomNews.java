package com.mytlogos.enterprise.background.room.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import org.joda.time.DateTime;

@Entity
public class RoomNews {

    private String title;
    private DateTime timeStamp;
    @PrimaryKey
    private int newsId;
    private boolean read;
    // fixme remove the ignore as soon as i have internet again
    @Ignore
    private String link;

    public RoomNews(int newsId, boolean read, String title, DateTime timeStamp, String link) {
        this.newsId = newsId;
        this.read = read;
        this.title = title;
        this.timeStamp = timeStamp;
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public int getNewsId() {
        return newsId;
    }

    public DateTime getTimeStamp() {
        return timeStamp;
    }

    public boolean isRead() {
        return read;
    }

    public String getLink() {
        return link;
    }

    @Override
    public String toString() {
        return "RoomNews{" +
                "title='" + title + '\'' +
                ", timeStamp=" + timeStamp +
                ", newsId=" + newsId +
                ", read=" + read +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomNews roomNews = (RoomNews) o;

        if (getNewsId() != roomNews.getNewsId()) return false;
        if (isRead() != roomNews.isRead()) return false;
        if (getTitle() != null ? !getTitle().equals(roomNews.getTitle()) : roomNews.getTitle() != null)
            return false;
        return getTimeStamp() != null ? getTimeStamp().equals(roomNews.getTimeStamp()) : roomNews.getTimeStamp() == null;
    }

    @Override
    public int hashCode() {
        int result = getTitle() != null ? getTitle().hashCode() : 0;
        result = 31 * result + (getTimeStamp() != null ? getTimeStamp().hashCode() : 0);
        result = 31 * result + getNewsId();
        result = 31 * result + (isRead() ? 1 : 0);
        return result;
    }
}
