package com.mytlogos.enterprise.background.room;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.mytlogos.enterprise.background.room.model.RoomDanglingMedium;
import com.mytlogos.enterprise.background.room.model.RoomEpisode;
import com.mytlogos.enterprise.background.room.model.RoomExternalMediaList;
import com.mytlogos.enterprise.background.room.model.RoomExternalUser;
import com.mytlogos.enterprise.background.room.model.RoomFailedEpisode;
import com.mytlogos.enterprise.background.room.model.RoomMediaList;
import com.mytlogos.enterprise.background.room.model.RoomMedium;
import com.mytlogos.enterprise.background.room.model.RoomMediumInWait;
import com.mytlogos.enterprise.background.room.model.RoomNews;
import com.mytlogos.enterprise.background.room.model.RoomNotification;
import com.mytlogos.enterprise.background.room.model.RoomPart;
import com.mytlogos.enterprise.background.room.model.RoomRelease;
import com.mytlogos.enterprise.background.room.model.RoomToDownload;
import com.mytlogos.enterprise.background.room.model.RoomUser;


@Database(
        entities = {
                RoomUser.class, RoomNews.class, RoomExternalUser.class, RoomMediaList.class,
                RoomMediaList.MediaListMediaJoin.class, RoomEpisode.class, RoomRelease.class,
                RoomPart.class, RoomMedium.class, RoomExternalMediaList.class,
                RoomExternalMediaList.ExternalListMediaJoin.class, RoomToDownload.class,
                RoomMediumInWait.class, RoomDanglingMedium.class, RoomFailedEpisode.class,
                RoomNotification.class
        },
        version = 9
)
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
                            .addMigrations(migrations())
                            .fallbackToDestructiveMigrationOnDowngrade()
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

    public abstract ToDownloadDao toDownloadDao();

    public abstract RoomMediumInWaitDao roomMediumInWaitDao();

    public abstract RoomDanglingDao roomDanglingDao();

    public abstract FailedEpisodesDao failedEpisodesDao();

    public abstract NotificationDao notificationDao();

    private static Migration[] migrations() {
        return new Migration[]{
                new Migration(7, 8) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {
                        database.execSQL(
                                "CREATE TABLE RoomFailedEpisode (" +
                                        "episodeId INTEGER NOT NULL, " +
                                        "failCount INTEGER NOT NULL, " +
                                        "PRIMARY KEY(episodeId), " +
                                        "FOREIGN KEY(`episodeId`) REFERENCES `RoomEpisode`(`episodeId`) ON UPDATE NO ACTION ON DELETE CASCADE" +
                                        ")"
                        );
                        database.execSQL(
                                "CREATE TABLE RoomNotification (" +
                                        "title TEXT NOT NULL, " +
                                        "description TEXT NOT NULL, " +
                                        "dateTime TEXT NOT NULL, " +
                                        "PRIMARY KEY(title, dateTime)" +
                                        ")"
                        );
                    }
                },
                new Migration(8, 9) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {
                        database.execSQL("ALTER TABLE RoomRelease ADD COLUMN locked INTEGER NOT NULL DEFAULT 0");
                    }
                }
        };
    }
}
