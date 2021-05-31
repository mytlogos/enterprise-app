package com.mytlogos.enterprise.background.api.model;

import java.util.Arrays;

/**
 * API Model for ListMedia.
 */
public class ClientListQuery {
    private final ClientMediaList list;
    private final ClientMedium[] media;

    public ClientListQuery(ClientMediaList list, ClientMedium[] media) {
        this.list = list;
        this.media = media;
    }

    public ClientMedium[] getMedia() {
        return media;
    }

    public ClientMediaList getList() {
        return list;
    }

    @Override
    public String toString() {
        return "ClientListQuery{" +
                "list=" + list +
                ", media=" + Arrays.toString(media) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientListQuery that = (ClientListQuery) o;

        if (getList() != null ? !getList().equals(that.getList()) : that.getList() != null)
            return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(getMedia(), that.getMedia());
    }

    @Override
    public int hashCode() {
        int result = getList() != null ? getList().hashCode() : 0;
        result = 31 * result + Arrays.hashCode(getMedia());
        return result;
    }
}
