package com.mytlogos.enterprise.background.api.model;

public class Authentication {
    private final String uuid;
    private final String session;

    public Authentication(String uuid, String session) {
        this.uuid = uuid;
        this.session = session;
    }

    public String getSession() {
        return session;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Authentication that = (Authentication) o;

        if (getUuid() != null ? !getUuid().equals(that.getUuid()) : that.getUuid() != null)
            return false;
        return getSession() != null ? getSession().equals(that.getSession()) : that.getSession() == null;
    }

    @Override
    public int hashCode() {
        int result = getUuid() != null ? getUuid().hashCode() : 0;
        result = 31 * result + (getSession() != null ? getSession().hashCode() : 0);
        return result;
    }
}
