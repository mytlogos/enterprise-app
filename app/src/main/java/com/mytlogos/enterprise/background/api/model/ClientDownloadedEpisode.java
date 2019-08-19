package com.mytlogos.enterprise.background.api.model;

import java.util.Arrays;

public class ClientDownloadedEpisode {
    private final String[] content;
    private final String title;
    private final int episodeId;

    public ClientDownloadedEpisode(String[] content, String title, int episodeId) {
        this.content = content;
        this.title = title;
        this.episodeId = episodeId;
    }

    public String[] getContent() {
        return content;
    }

    public String getTitle() {
        return title;
    }

    public int getEpisodeId() {
        return episodeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientDownloadedEpisode episode = (ClientDownloadedEpisode) o;

        if (getEpisodeId() != episode.getEpisodeId()) return false;
        if (getContent() != null ? !Arrays.equals(getContent(), episode.getContent()) : episode.getContent() != null)
            return false;
        return getTitle() != null ? getTitle().equals(episode.getTitle()) : episode.getTitle() == null;
    }

    @Override
    public int hashCode() {
        int result = getContent() != null ? Arrays.hashCode(getContent()) : 0;
        result = 31 * result + (getTitle() != null ? getTitle().hashCode() : 0);
        result = 31 * result + getEpisodeId();
        return result;
    }
}
