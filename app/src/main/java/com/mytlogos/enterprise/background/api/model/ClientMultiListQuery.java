package com.mytlogos.enterprise.background.api.model;

import java.util.Arrays;

public class ClientMultiListQuery {
    private ClientMediaList[] list;
    private ClientMedium[] media;

    public ClientMultiListQuery(ClientMediaList[] list, ClientMedium[] media) {
        this.list = list;
        this.media = media;
    }

    public ClientMedium[] getMedia() {
        return media;
    }

    public ClientMediaList[] getList() {
        return list;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientMultiListQuery that = (ClientMultiListQuery) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(getList(), that.getList())) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(getMedia(), that.getMedia());
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(getList());
        result = 31 * result + Arrays.hashCode(getMedia());
        return result;
    }

    @Override
    public String toString() {
        return "ClientMultiListQuery{" +
                "list=" + Arrays.toString(list) +
                ", media=" + Arrays.toString(media) +
                '}';
    }
}
