package com.mytlogos.enterprise.background.room.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.mytlogos.enterprise.model.ExternalMediaList;

@Entity(
        foreignKeys = @ForeignKey(
                entity = RoomExternalUser.class,
                parentColumns = "uuid",
                childColumns = "uuid",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index(value = "uuid"),
                @Index(value = "externalListId"),
        }
)
public class RoomExternalMediaList implements ExternalMediaList {
    public final String uuid;
    @PrimaryKey
    public final int externalListId;
    public final String name;
    public final int medium;
    public final String url;

    public RoomExternalMediaList(String uuid, int externalListId, String name, int medium, String url) {
        this.uuid = uuid;
        this.externalListId = externalListId;
        this.name = name;
        this.medium = medium;
        this.url = url;
    }

    @Entity(
            primaryKeys = {"listId", "mediumId"},
            foreignKeys = {
                    @ForeignKey(
                            entity = RoomMedium.class,
                            parentColumns = "mediumId",
                            childColumns = "mediumId",
                            onDelete = ForeignKey.CASCADE
                    ),
                    @ForeignKey(
                            entity = RoomExternalMediaList.class,
                            parentColumns = "externalListId",
                            childColumns = "listId",
                            onDelete = ForeignKey.CASCADE
                    ),
            },
            indices = {
                    @Index(value = "listId"),
                    @Index(value = "mediumId"),
            }
    )
    public static class ExternalListMediaJoin {
        public final int listId;
        public final int mediumId;

        public ExternalListMediaJoin(int listId, int mediumId) {
            this.listId = listId;
            this.mediumId = mediumId;
        }
    }

}
