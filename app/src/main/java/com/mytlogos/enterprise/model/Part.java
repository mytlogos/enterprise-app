package com.mytlogos.enterprise.model;

import java.util.List;

public interface Part {
    int getPartId();

    String getTitle();

    int getTotalIndex();

    int getPartialIndex();

    List<Integer> getEpisodes();
}
