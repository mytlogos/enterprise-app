package com.mytlogos.enterprise.background.api.model;

public class ClientUpdateUser {
    private final String uuid;
    private final String name;
    private final String password;
    private final String newPassword;

    public ClientUpdateUser(String uuid, String name, String password, String newPassword) {
        this.uuid = uuid;
        this.name = name;
        this.password = password;
        this.newPassword = newPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientUpdateUser that = (ClientUpdateUser) o;

        if (getUuid() != null ? !getUuid().equals(that.getUuid()) : that.getUuid() != null)
            return false;
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null)
            return false;
        if (getPassword() != null ? !getPassword().equals(that.getPassword()) : that.getPassword() != null)
            return false;
        return getNewPassword() != null ? getNewPassword().equals(that.getNewPassword()) : that.getNewPassword() == null;
    }

    @Override
    public int hashCode() {
        int result = getUuid() != null ? getUuid().hashCode() : 0;
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getPassword() != null ? getPassword().hashCode() : 0);
        result = 31 * result + (getNewPassword() != null ? getNewPassword().hashCode() : 0);
        return result;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getNewPassword() {
        return newPassword;
    }
}
