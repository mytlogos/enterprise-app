package com.mytlogos.enterprise.background.room.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.mytlogos.enterprise.model.ExternalUser;

@Entity(
        foreignKeys = @ForeignKey(
                entity = RoomUser.class,
                childColumns = "userUuid",
                parentColumns = "uuid",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index(value = "uuid"),
                @Index(value = "userUuid"),
        }
)
public class RoomExternalUser implements ExternalUser {
    @NonNull
    @PrimaryKey
    public final String uuid;
    @NonNull
    public final String userUuid;
    @NonNull
    public final String identifier;
    public final int type;

    public RoomExternalUser(@NonNull String uuid, @NonNull String userUuid, @NonNull String identifier, int type) {
        this.uuid = uuid;
        this.userUuid = userUuid;
        this.identifier = identifier;
        this.type = type;
    }
}
