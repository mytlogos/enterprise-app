package com.mytlogos.enterprise.background.room.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.mytlogos.enterprise.model.MediaList;

@Entity(
        foreignKeys = @ForeignKey(
                entity = RoomUser.class,
                childColumns = "uuid",
                parentColumns = "uuid",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index(value = "uuid"),
                @Index(value = "listId"),
        }
)
public class RoomMediaList implements MediaList {
    @PrimaryKey
    public final int listId;
    public final String uuid;
    public final String name;
    public final int medium;

    public RoomMediaList(int listId, String uuid, String name, int medium) {
        this.listId = listId;
        this.uuid = uuid;
        this.name = name;
        this.medium = medium;
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
                            entity = RoomMediaList.class,
                            parentColumns = "listId",
                            childColumns = "listId",
                            onDelete = ForeignKey.CASCADE
                    ),
            },
            indices = {
                    @Index(value = "listId"),
                    @Index(value = "mediumId"),
            }
    )
    public static class MediaListMediaJoin {
        public final int listId;
        public final int mediumId;

        public MediaListMediaJoin(int listId, int mediumId) {
            this.listId = listId;
            this.mediumId = mediumId;
        }
    }
}
