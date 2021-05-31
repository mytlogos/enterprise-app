package com.mytlogos.enterprise.background.api.model;

import androidx.annotation.NonNull;

import com.mytlogos.enterprise.model.Toc;

import java.util.Objects;

/**
 * API Model for FullMediumToc.
 * TODO: Missing properties:
 *  id, countryOfOrigin, languageOfOrigin, author, title, medium,
 *  artist, lang, stateOrigin, stateTL, series, universe
 */
public class ClientToc implements Toc {
    private final int mediumId;
    private final String link;

    public ClientToc(int mediumId, String link) {
        this.mediumId = mediumId;
        this.link = link;
    }

    @Override
    public int getMediumId() {
        return mediumId;
    }

    @Override
    public String getLink() {
        return link;
    }

    @Override
    public int hashCode() {
        int result = mediumId;
        result = 31 * result + (link != null ? link.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientToc)) return false;

        ClientToc clientToc = (ClientToc) o;

        if (mediumId != clientToc.mediumId) return false;
        return Objects.equals(link, clientToc.link);
    }

    @NonNull
    @Override
    public String toString() {
        return "ClientToc{" +
                "mediumId=" + mediumId +
                ", link='" + link + '\'' +
                '}';
    }
}
