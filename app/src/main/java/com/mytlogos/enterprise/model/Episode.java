package com.mytlogos.enterprise.model;

import org.joda.time.DateTime;

public interface Episode {
    int getEpisodeId();

    float getProgress();

    int getPartId();

    int getTotalIndex();

    int getPartialIndex();

    DateTime getReadDate();

    boolean isSaved();
}
