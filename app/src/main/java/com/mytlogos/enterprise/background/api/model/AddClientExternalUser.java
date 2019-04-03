package com.mytlogos.enterprise.background.api.model;

public class AddClientExternalUser extends ClientExternalUser {
    private String pwd;

    public AddClientExternalUser(String uuid, String identifier, int type, ClientExternalMediaList[] lists, String pwd) {
        super("", uuid, identifier, type, lists);
        this.pwd = pwd;
    }

    public String getPwd() {
        return pwd;
    }

    @Override
    public String toString() {
        return "AddClientExternalUser{" +
                "pwd='" + pwd + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AddClientExternalUser that = (AddClientExternalUser) o;

        return getPwd() != null ? getPwd().equals(that.getPwd()) : that.getPwd() == null;
    }

    @Override
    public int hashCode() {
        return getPwd() != null ? getPwd().hashCode() : 0;
    }
}
