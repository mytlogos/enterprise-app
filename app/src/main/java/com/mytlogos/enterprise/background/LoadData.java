package com.mytlogos.enterprise.background;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LoadData {
    private Set<Integer> media = Collections.synchronizedSet(new HashSet<>());
    private Set<Integer> part = Collections.synchronizedSet(new HashSet<>());
    private Set<Integer> episode = Collections.synchronizedSet(new HashSet<>());
    private Set<Integer> news = Collections.synchronizedSet(new HashSet<>());
    private Set<String> externalUser = Collections.synchronizedSet(new HashSet<>());
    private Set<Integer> externalMediaList = Collections.synchronizedSet(new HashSet<>());
    private Set<Integer> mediaList = Collections.synchronizedSet(new HashSet<>());

    public Set<Integer> getMedia() {
        return media;
    }

    public Set<Integer> getPart() {
        return part;
    }

    public Set<Integer> getEpisodes() {
        return episode;
    }

    public Set<Integer> getNews() {
        return news;
    }

    public Set<Integer> getExternalMediaList() {
        return externalMediaList;
    }

    public Set<String> getExternalUser() {
        return externalUser;
    }

    public Set<Integer> getMediaList() {
        return mediaList;
    }
}
