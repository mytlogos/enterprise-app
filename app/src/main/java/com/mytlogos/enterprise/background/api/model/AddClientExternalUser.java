package com.mytlogos.enterprise.background.api.model;

public class AddClientExternalUser extends ClientExternalUser {
    private String pwd;

    public AddClientExternalUser(String uuid, String identifier, int type, ClientExternalMediaList[] lists, String pwd) {
        super(uuid, identifier, type, lists);
        this.pwd = pwd;
    }

    public String getPwd() {
        return pwd;
    }
}
