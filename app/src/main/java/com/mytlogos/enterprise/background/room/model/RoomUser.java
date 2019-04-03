package com.mytlogos.enterprise.background.room.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;


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

    @Entity(
            foreignKeys = {
                    @ForeignKey(childColumns = "uuid", parentColumns = "uuid", entity = RoomUser.class, onDelete = ForeignKey.CASCADE),
                    @ForeignKey(childColumns = "id", parentColumns = "newsId", entity = RoomNews.class, onDelete = ForeignKey.CASCADE)
            },
            primaryKeys = {"uuid", "id"},
            indices = {
                    @Index(value = "uuid"),
                    @Index(value = "id"),
            }
    )
    public static class UserUnReadNewsJoin {
        @NonNull
        public final String uuid;
        public final int id;

        public UserUnReadNewsJoin(@NonNull String uuid, int id) {
            this.uuid = uuid;
            this.id = id;
        }

        @Override
        public String toString() {
            return "UserUnReadNewsJoin{" +
                    "uuid='" + uuid + '\'' +
                    ", id=" + id +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UserUnReadNewsJoin that = (UserUnReadNewsJoin) o;

            if (id != that.id) return false;
            return uuid.equals(that.uuid);
        }

        @Override
        public int hashCode() {
            int result = uuid.hashCode();
            result = 31 * result + id;
            return result;
        }
    }


    @Entity(
            foreignKeys = {
                    @ForeignKey(childColumns = "uuid", parentColumns = "uuid", entity = RoomUser.class, onDelete = ForeignKey.CASCADE),
                    @ForeignKey(childColumns = "id", parentColumns = "episodeId", entity = RoomEpisode.class, onDelete = ForeignKey.CASCADE)
            },
            primaryKeys = {"uuid", "id"},
            indices = {
                    @Index(value = "uuid"),
                    @Index(value = "id"),
            }
    )
    public static class UserUnReadChapterJoin {
        @NonNull
        public final String uuid;
        public final int id;

        public UserUnReadChapterJoin(@NonNull String uuid, int id) {
            this.uuid = uuid;
            this.id = id;
        }

        @Override
        public String toString() {
            return "UserUnReadChapterJoin{" +
                    "uuid='" + uuid + '\'' +
                    ", id=" + id +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UserUnReadChapterJoin that = (UserUnReadChapterJoin) o;

            if (id != that.id) return false;
            return uuid.equals(that.uuid);
        }

        @Override
        public int hashCode() {
            int result = uuid.hashCode();
            result = 31 * result + id;
            return result;
        }
    }


    @Entity(
            foreignKeys = {
                    @ForeignKey(childColumns = "uuid", parentColumns = "uuid", entity = RoomUser.class, onDelete = ForeignKey.CASCADE),
                    @ForeignKey(childColumns = "id", parentColumns = "mediumId", entity = RoomMedium.class, onDelete = ForeignKey.CASCADE)
            },
            primaryKeys = {"uuid", "id"},
            indices = {
                    @Index(value = "uuid"),
                    @Index(value = "id"),
            }
    )
    public static class UserReadTodayJoin {
        @NonNull
        public final String uuid;
        public final int id;

        public UserReadTodayJoin(@NonNull String uuid, int id) {
            this.uuid = uuid;
            this.id = id;
        }

        @Override
        public String toString() {
            return "UserReadTodayJoin{" +
                    "uuid='" + uuid + '\'' +
                    ", id=" + id +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UserReadTodayJoin that = (UserReadTodayJoin) o;

            if (id != that.id) return false;
            return uuid.equals(that.uuid);
        }


        @Override
        public int hashCode() {
            int result = uuid.hashCode();
            result = 31 * result + id;
            return result;
        }
    }
}
