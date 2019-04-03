package com.mytlogos.enterprise.model;

import org.joda.time.DateTime;

public interface News {

    String getTimeStampString();

    String getTitle();

    int getNewsId();

    boolean isRead();

    DateTime getTimeStamp();
}
