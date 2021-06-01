package com.mytlogos.enterprise.model;

import org.joda.time.DateTime;

public interface Release {
    DateTime getReleaseDate();

    String getTitle();

    String getUrl();

    int getEpisodeId();

    boolean isLocked();
}
