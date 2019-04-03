package com.mytlogos.enterprise.model;

public class UpdateUser {
    private final String name;
    private final String password;
    private final String newPassword;

    public UpdateUser(String name, String password, String newPassword) {
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
