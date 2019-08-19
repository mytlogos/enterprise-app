package com.mytlogos.enterprise.model;

import androidx.annotation.NonNull;

public class ExternalUser {
    @NonNull
    private final String uuid;
    @NonNull
    private final String identifier;
    private final int type;

    public ExternalUser(@NonNull String uuid, @NonNull String identifier, int type) {
        this.uuid = uuid;
        this.identifier = identifier;
        this.type = type;
    }

    @NonNull
    public String getUuid() {
        return uuid;
    }

    @NonNull
    public String getIdentifier() {
        return identifier;
    }

    public int getType() {
        return type;
    }
}
