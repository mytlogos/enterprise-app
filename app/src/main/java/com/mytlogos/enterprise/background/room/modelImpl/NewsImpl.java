package com.mytlogos.enterprise.background.room.modelImpl;

import com.mytlogos.enterprise.Formatter;
import com.mytlogos.enterprise.model.News;

import org.joda.time.DateTime;

public class NewsImpl implements News {
    private String title;
    private DateTime timeStamp;
    private int id;
    private boolean read;

    public NewsImpl(String title, DateTime timeStamp, int id, boolean read) {
        this.title = title;
        this.timeStamp = timeStamp;
        this.id = id;
        this.read = read;
    }

    @Override
    public String getTimeStampString() {
        return Formatter.formatDateTime(this.getTimeStamp());
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public int getNewsId() {
        return this.id;
    }

    @Override
    public boolean isRead() {
        return this.read;
    }

    @Override
    public DateTime getTimeStamp() {
        return this.timeStamp;
    }

    @Override
    public String toString() {
        return "NewsImpl{" +
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

        NewsImpl news = (NewsImpl) o;

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
