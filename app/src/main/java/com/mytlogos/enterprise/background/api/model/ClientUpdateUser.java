package com.mytlogos.enterprise.background.api.model;

public class ClientUpdateUser {
    private final String name;
    private final String password;
    private final String newPassword;

    public ClientUpdateUser(String name, String password, String newPassword) {
        this.name = name;
        this.password = password;
        this.newPassword = newPassword;
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
