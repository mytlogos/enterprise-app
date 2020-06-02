package com.mytlogos.enterprise.background.room.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Objects;

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
public class RoomMediaList {
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

    @Override
    public String toString() {
        return "RoomMediaList{" +
                "listId=" + listId +
                ", uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", medium=" + medium +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomMediaList that = (RoomMediaList) o;

        if (listId != that.listId) return false;
        if (medium != that.medium) return false;
        if (!Objects.equals(uuid, that.uuid)) return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        int result = listId;
        result = 31 * result + (uuid != null ? uuid.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + medium;
        return result;
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
    public static class MediaListMediaJoin implements ListMediaJoin {
        public final int listId;
        public final int mediumId;

        public MediaListMediaJoin(int listId, int mediumId) {
            this.listId = listId;
            this.mediumId = mediumId;
        }

        @Override
        public String toString() {
            return "MediaListMediaJoin{" +
                    "listId=" + listId +
                    ", mediumId=" + mediumId +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MediaListMediaJoin that = (MediaListMediaJoin) o;

            if (listId != that.listId) return false;
            return mediumId == that.mediumId;
        }

        @Override
        public int hashCode() {
            int result = listId;
            result = 31 * result + mediumId;
            return result;
        }

        @Override
        public int getListId() {
            return this.listId;
        }

        @Override
        public int getMediumId() {
            return this.mediumId;
        }
    }
}
