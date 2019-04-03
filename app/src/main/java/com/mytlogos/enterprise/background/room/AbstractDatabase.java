package com.mytlogos.enterprise.background.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import com.mytlogos.enterprise.background.room.model.RoomEpisode;
import com.mytlogos.enterprise.background.room.model.RoomExternalMediaList;
import com.mytlogos.enterprise.background.room.model.RoomExternalUser;
import com.mytlogos.enterprise.background.room.model.RoomMediaList;
import com.mytlogos.enterprise.background.room.model.RoomMedium;
import com.mytlogos.enterprise.background.room.model.RoomNews;
import com.mytlogos.enterprise.background.room.model.RoomPart;
import com.mytlogos.enterprise.background.room.model.RoomUser;


@Database(
        entities = {
                RoomUser.class, RoomNews.class, RoomUser.UserReadTodayJoin.class,
                RoomUser.UserUnReadChapterJoin.class, RoomUser.UserUnReadNewsJoin.class,
                RoomExternalUser.class, RoomMediaList.class, RoomMediaList.MediaListMediaJoin.class,
                RoomEpisode.class, RoomPart.class, RoomMedium.class, RoomExternalMediaList.class,
                RoomExternalMediaList.ExternalListMediaJoin.class,
        },
        version = 7,
        exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AbstractDatabase extends RoomDatabase {

    private static final String DB_Name = "local_database";
    private static AbstractDatabase INSTANCE;

    static AbstractDatabase getInstance(final Context context) {
        if (INSTANCE == null || !INSTANCE.isOpen()) {
            synchronized (AbstractDatabase.class) {
                if (INSTANCE == null || !INSTANCE.isOpen()) {
                    INSTANCE = Room
                            .databaseBuilder(
                                    context.getApplicationContext(),
                                    AbstractDatabase.class,
                                    DB_Name)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract UserDao userDao();

    public abstract NewsDao newsDao();

    public abstract ExternalUserDao externalUserDao();

    public abstract ExternalMediaListDao externalMediaListDao();

    public abstract MediaListDao mediaListDao();

    public abstract MediumDao mediumDao();

    public abstract PartDao partDao();

    public abstract EpisodeDao episodeDao();
}
