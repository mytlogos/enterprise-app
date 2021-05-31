package com.mytlogos.enterprise.background.api.model;

public class ClientSimpleRelease {
    public final String url;
    public final int id;

    public ClientSimpleRelease(String url, int id) {
        this.url = url;
        this.id = id;
    }

    @Override
    public String toString() {
        return "ClientSimpleRelease{" +
                "url='" + url + '\'' +
                ", id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientSimpleRelease that = (ClientSimpleRelease) o;

        if (id != that.id) return false;
        return url.equals(that.url);
    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + id;
        return result;
    }
}
