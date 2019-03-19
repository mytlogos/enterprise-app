package com.mytlogos.enterprise.background.room.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.mytlogos.enterprise.Formatter;
import com.mytlogos.enterprise.model.News;

import org.joda.time.DateTime;

@Entity
public class RoomNews implements News {

    private String title;
    private DateTime timeStamp;
    @PrimaryKey
    private int newsId;
    private boolean read;

    public RoomNews(int newsId, boolean read, String title, DateTime timeStamp) {
        this.newsId = newsId;
        this.read = read;
        this.title = title;
        this.timeStamp = timeStamp;
    }

    public String getTimeStampString() {
        return Formatter.formatDateTime(getTimeStamp());
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
}
