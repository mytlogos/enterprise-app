package com.mytlogos.enterprise.background.room.modelImpl;

import androidx.annotation.NonNull;
import androidx.room.Ignore;
import androidx.room.Relation;

import com.mytlogos.enterprise.background.room.model.RoomUser;
import com.mytlogos.enterprise.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserImpl implements User {

    @NonNull
    private final String name;

    @NonNull
    private final String uuid;

    @NonNull
    private final String session;

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Relation(entityColumn = "uuid", parentColumn = "uuid")
    private List<RoomUser.UserUnReadNewsJoin> unReadNewsJoins;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Relation(entityColumn = "uuid", parentColumn = "uuid")
    private List<RoomUser.UserUnReadChapterJoin> unReadChapterJoins;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Relation(entityColumn = "uuid", parentColumn = "uuid")
    private List<RoomUser.UserReadTodayJoin> readTodayJoins;

    @Ignore
    private List<Integer> mediaList;
    @Ignore
    private List<String> externalUser;

    public UserImpl(@NonNull String name, @NonNull String uuid, @NonNull String session) {
        this.name = name;
        this.uuid = uuid;
        this.session = session;
        this.unReadNewsJoins = new ArrayList<>();
        this.unReadChapterJoins = new ArrayList<>();
        this.readTodayJoins = new ArrayList<>();
        this.mediaList = new ArrayList<>();
        this.externalUser = new ArrayList<>();
    }

    public List<RoomUser.UserUnReadNewsJoin> getUnReadNewsJoins() {
        return unReadNewsJoins;
    }

    public void setUnReadNewsJoins(List<RoomUser.UserUnReadNewsJoin> unReadNewsJoins) {
        this.unReadNewsJoins = unReadNewsJoins;
    }

    public List<RoomUser.UserUnReadChapterJoin> getUnReadChapterJoins() {
        return unReadChapterJoins;
    }

    public void setUnReadChapterJoins(List<RoomUser.UserUnReadChapterJoin> unReadChapterJoins) {
        this.unReadChapterJoins = unReadChapterJoins;
    }

    public List<RoomUser.UserReadTodayJoin> getReadTodayJoins() {
        return readTodayJoins;
    }

    public void setReadTodayJoins(List<RoomUser.UserReadTodayJoin> readTodayJoins) {
        this.readTodayJoins = readTodayJoins;
    }

    @NonNull
    @Override
    public String getUuid() {
        return this.uuid;
    }

    @NonNull
    @Override
    public String getName() {
        return this.name;
    }

    @NonNull
    @Override
    public String getSession() {
        return this.session;
    }

    @Override
    public int unreadNewsCount() {
        return this.unReadNewsJoins.size();
    }

    @Override
    public int unreadChapterCount() {
        return this.unReadChapterJoins.size();
    }

    @Override
    public int readTodayCount() {
        return this.readTodayJoins.size();
    }

    @Override
    public List<Integer> getUnReadChapter() {
        return this.unReadChapterJoins.stream().map(userUnReadNewsJoin -> userUnReadNewsJoin.id).collect(Collectors.toList());
    }

    @Override
    public List<Integer> getUnReadNews() {
        return this.unReadNewsJoins.stream().map(userUnReadNewsJoin -> userUnReadNewsJoin.id).collect(Collectors.toList());
    }

    @Override
    public List<Integer> getReadToday() {
        return this.readTodayJoins.stream().map(userUnReadNewsJoin -> userUnReadNewsJoin.id).collect(Collectors.toList());
    }

    @Override
    public List<Integer> getMediaList() {
        return this.mediaList;
    }

    @Override
    public List<String> getExternalUser() {
        return this.externalUser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserImpl user = (UserImpl) o;

        return getUuid().equals(user.getUuid());
    }

    @Override
    public int hashCode() {
        return getUuid().hashCode();
    }

    @Override
    public String toString() {
        return "UserImpl{" +
                "name='" + name + '\'' +
                ", uuid='" + uuid + '\'' +
                ", unReadNewsJoins=" + unReadNewsJoins +
                ", unReadChapterJoins=" + unReadChapterJoins +
                ", readToday=" + readTodayJoins +
                ", mediaList=" + mediaList +
                ", externalUser=" + externalUser +
                '}';
    }
}
