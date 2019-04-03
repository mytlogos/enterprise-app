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

    @Override
    public String toString() {
        return "RoomExternalMediaList{" +
                "uuid='" + uuid + '\'' +
                ", externalListId=" + externalListId +
                ", name='" + name + '\'' +
                ", medium=" + medium +
                ", url='" + url + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomExternalMediaList that = (RoomExternalMediaList) o;

        if (externalListId != that.externalListId) return false;
        if (medium != that.medium) return false;
        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return url != null ? url.equals(that.url) : that.url == null;
    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = 31 * result + externalListId;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + medium;
        result = 31 * result + (url != null ? url.hashCode() : 0);
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

        @Override
        public String toString() {
            return "ExternalListMediaJoin{" +
                    "listId=" + listId +
                    ", mediumId=" + mediumId +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ExternalListMediaJoin that = (ExternalListMediaJoin) o;

            if (listId != that.listId) return false;
            return mediumId == that.mediumId;
        }

        @Override
        public int hashCode() {
            int result = listId;
            result = 31 * result + mediumId;
            return result;
        }
    }

}
