package com.mytlogos.enterprise.background;

import java.util.Collection;

public class ReloadPart {
    public final Collection<Integer> loadPartEpisodes;
    public final Collection<Integer> loadPartReleases;

    public ReloadPart(Collection<Integer> loadPartEpisodes, Collection<Integer> loadPartReleases) {
        this.loadPartEpisodes = loadPartEpisodes;
        this.loadPartReleases = loadPartReleases;
    }

    @Override
    public String toString() {
        return "ReloadPart{" +
                "loadPartEpisodes=" + loadPartEpisodes +
                ", loadPartReleases=" + loadPartReleases +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReloadPart that = (ReloadPart) o;

        if (!loadPartEpisodes.equals(that.loadPartEpisodes)) return false;
        return loadPartReleases.equals(that.loadPartReleases);
    }

    @Override
    public int hashCode() {
        int result = loadPartEpisodes.hashCode();
        result = 31 * result + loadPartReleases.hashCode();
        return result;
    }
}
