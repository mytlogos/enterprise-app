package com.mytlogos.enterprise.background;

import com.mytlogos.enterprise.model.Toc;

import java.util.Objects;

/**
 *
 */
public class SimpleToc implements Toc {
    public final int mediumId;
    public final String link;

    public SimpleToc(int mediumId, String link) {
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
        if (!(o instanceof SimpleToc)) return false;

        SimpleToc simpleToc = (SimpleToc) o;

        if (mediumId != simpleToc.mediumId) return false;
        return Objects.equals(link, simpleToc.link);
    }

    @Override
    public String toString() {
        return "SimpleToc{" +
                "mediumId=" + mediumId +
                ", link='" + link + '\'' +
                '}';
    }
}
