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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AddClientExternalUser that = (AddClientExternalUser) o;

        return getPwd() != null ? getPwd().equals(that.getPwd()) : that.getPwd() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getPwd() != null ? getPwd().hashCode() : 0);
        return result;
    }
}
