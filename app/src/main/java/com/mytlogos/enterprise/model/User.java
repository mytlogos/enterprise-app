package com.mytlogos.enterprise.model;

import android.support.annotation.NonNull;

import java.util.List;

public interface User {
    @NonNull
    String getUuid();

    @NonNull
    String getName();

    @NonNull
    String getSession();

    int unreadNewsCount();

    int unreadChapterCount();

    int readTodayCount();

    List<Integer> getUnReadChapter();

    List<Integer> getUnReadNews();

    List<Integer> getReadToday();

    List<Integer> getMediaList();

    List<String> getExternalUser();
}
