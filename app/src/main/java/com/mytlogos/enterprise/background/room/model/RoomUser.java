package com.mytlogos.enterprise.background.room.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity
public class RoomUser {
    @NonNull
    private final String name;

    @NonNull
    @PrimaryKey
    private final String uuid;

    @NonNull
    private final String session;

    public RoomUser(@NonNull String name, @NonNull String uuid, @NonNull String session) {
        this.name = name;
        this.uuid = uuid;
        this.session = session;
    }


    @NonNull
    public String getUuid() {
        return uuid;
    }


    @NonNull
    public String getName() {
        return name;
    }


    @NonNull
    public String getSession() {
        return session;
    }


    public String toString() {
        return super.toString() +
                "{" +
                "uuid: " + this.getUuid() +
                ", name: " + this.getName() +
                "}";
    }

    public int hashCode() {
        return this.getUuid().hashCode();
    }


    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (!(obj instanceof RoomUser)) return false;

        RoomUser user = (RoomUser) obj;
        return this.getUuid().equals(user.getUuid());
    }
}
