package com.mytlogos.enterprise.model;

import org.joda.time.DateTime;

public interface Episode {
    int getEpisodeId();

    float getProgress();

    int getPartId();

    String getTitle();

    int getTotalIndex();

    int getPartialIndex();

    String getUrl();

    DateTime getReleaseDate();

    DateTime getReadDate();
}
