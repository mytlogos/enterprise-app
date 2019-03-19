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
}
