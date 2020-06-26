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
import com.mytlogos.enterprise.background.room.model.RoomEditEvent;
import com.mytlogos.enterprise.background.room.model.RoomEpisode;
import com.mytlogos.enterprise.background.room.model.RoomExternalMediaList;
import com.mytlogos.enterprise.background.room.model.RoomExternalUser;
import com.mytlogos.enterprise.background.room.model.RoomFailedEpisode;
import com.mytlogos.enterprise.background.room.model.RoomMediaList;
import com.mytlogos.enterprise.background.room.model.RoomMedium;
import com.mytlogos.enterprise.background.room.model.RoomMediumInWait;
import com.mytlogos.enterprise.background.room.model.RoomMediumPart;
import com.mytlogos.enterprise.background.room.model.RoomMediumProgress;
import com.mytlogos.enterprise.background.room.model.RoomNews;
import com.mytlogos.enterprise.background.room.model.RoomNotification;
import com.mytlogos.enterprise.background.room.model.RoomPart;
import com.mytlogos.enterprise.background.room.model.RoomPartEpisode;
import com.mytlogos.enterprise.background.room.model.RoomRelease;
import com.mytlogos.enterprise.background.room.model.RoomToDownload;
import com.mytlogos.enterprise.background.room.model.RoomToc;
import com.mytlogos.enterprise.background.room.model.RoomUser;


@Database(
        entities = {
                RoomUser.class, RoomNews.class, RoomExternalUser.class, RoomMediaList.class,
                RoomMediaList.MediaListMediaJoin.class, RoomEpisode.class, RoomRelease.class,
                RoomPart.class, RoomMedium.class, RoomExternalMediaList.class,
                RoomExternalMediaList.ExternalListMediaJoin.class, RoomToDownload.class,
                RoomMediumInWait.class, RoomDanglingMedium.class, RoomFailedEpisode.class,
                RoomNotification.class, RoomMediumProgress.class, RoomMediumPart.class,
                RoomPartEpisode.class, RoomEditEvent.class, RoomToc.class
        },
        version = 14
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

    public abstract MediumProgressDao mediumProgressDao();

    public abstract DataStructureDao dataStructureDao();

    public abstract EditDao editDao();

    public abstract TocDao tocDao();

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
                },
                new Migration(9, 10) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {
                        database.execSQL("ALTER TABLE RoomEpisode ADD COLUMN combiIndex REAL NOT NULL DEFAULT 0");
                        database.execSQL("UPDATE RoomEpisode SET combiIndex=CAST((totalIndex || \".\" || COALESCE(partialIndex,0)) as decimal)");
                        database.execSQL("ALTER TABLE RoomPart ADD COLUMN combiIndex REAL NOT NULL DEFAULT 0");
                        database.execSQL("UPDATE RoomPart SET combiIndex=CAST((totalIndex || \".\" || COALESCE(partialIndex,0)) as decimal)");
                    }
                },
                new Migration(10, 11) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {
                        database.execSQL("CREATE TABLE IF NOT EXISTS RoomMediumProgress " +
                                "(mediumId INTEGER NOT NULL, currentReadIndex REAL NOT NULL, " +
                                "PRIMARY KEY(`mediumId`), " +
                                "FOREIGN KEY(`mediumId`) REFERENCES `RoomMedium`(`mediumId`) " +
                                "ON UPDATE NO ACTION ON DELETE CASCADE )"
                        );
                        database.execSQL(
                                "CREATE INDEX index_RoomMediumProgress_mediumId " +
                                        "ON RoomMediumProgress (mediumId);"
                        );
                        database.execSQL(
                                "CREATE INDEX index_RoomMediumProgress_currentReadIndex " +
                                        "ON RoomMediumProgress (currentReadIndex);"
                        );
                        database.execSQL(
                                "INSERT OR IGNORE INTO RoomMediumProgress " +
                                        "SELECT RoomMedium.mediumId, MAX(RoomEpisode.combiIndex) FROM RoomMedium " +
                                        "INNER JOIN RoomPart ON RoomMedium.mediumId=RoomPart.mediumId " +
                                        "INNER JOIN RoomEpisode ON RoomPart.partId=RoomEpisode.partId " +
                                        "WHERE progress = 1"
                        );
                    }
                },
                new Migration(11, 12) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {
                        database.execSQL("CREATE TABLE IF NOT EXISTS RoomMediumPart " +
                                "(mediumId INTEGER NOT NULL, partId INTEGER NOT NULL, " +
                                "PRIMARY KEY(`mediumId`, `partId`), " +
                                "FOREIGN KEY(`mediumId`) REFERENCES `RoomMedium`(`mediumId`) " +
                                "ON UPDATE NO ACTION ON DELETE CASCADE, " +
                                "FOREIGN KEY(`partId`) REFERENCES `RoomPart`(`partId`) " +
                                "ON UPDATE NO ACTION ON DELETE CASCADE)"
                        );
                        database.execSQL("CREATE TABLE IF NOT EXISTS RoomPartEpisode " +
                                "(episodeId INTEGER NOT NULL, partId INTEGER NOT NULL, " +
                                "PRIMARY KEY(`episodeId`, `partId`), " +
                                "FOREIGN KEY(`episodeId`) REFERENCES `RoomEpisode`(`episodeId`) " +
                                "ON UPDATE NO ACTION ON DELETE CASCADE, " +
                                "FOREIGN KEY(`partId`) REFERENCES `RoomPart`(`partId`) " +
                                "ON UPDATE NO ACTION ON DELETE CASCADE)"
                        );
                        database.execSQL(
                                "CREATE INDEX index_RoomMediumPart_mediumId " +
                                        "ON RoomMediumPart (mediumId);"
                        );
                        database.execSQL(
                                "CREATE INDEX index_RoomMediumPart_partId " +
                                        "ON RoomMediumPart (partId);"
                        );
                        database.execSQL(
                                "CREATE INDEX index_RoomPartEpisode_episodeId " +
                                        "ON RoomPartEpisode (episodeId);"
                        );
                        database.execSQL(
                                "CREATE INDEX index_RoomPartEpisode_partId " +
                                        "ON RoomPartEpisode (partId);"
                        );
                    }
                },
                new Migration(12, 13) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {
                        database.execSQL("CREATE TABLE IF NOT EXISTS RoomEditEvent " +
                                "(" +
                                "id INTEGER NOT NULL, objectType INTEGER NOT NULL, " +
                                "eventType INTEGER NOT NULL, dateTime TEXT NOT NULL, " +
                                "firstValue TEXT, secondValue TEXT," +
                                "PRIMARY KEY(`id`, `objectType`, `eventType`, `dateTime`)" +
                                ") "
                        );
                    }
                },
                new Migration(13, 14) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {
                        database.execSQL("CREATE TABLE IF NOT EXISTS RoomToc " +
                                "(" +
                                "mediumId INTEGER NOT NULL, link TEXT NOT NULL, " +
                                "PRIMARY KEY(`mediumId`, `link`), " +
                                "FOREIGN KEY(`mediumId`) REFERENCES `RoomMedium`(`mediumId`) " +
                                "ON UPDATE NO ACTION ON DELETE CASCADE " +
                                ") "
                        );
                        database.execSQL("CREATE INDEX index_RoomToc_mediumId ON RoomToc (mediumId);");
                        database.execSQL("CREATE INDEX index_RoomToc_link ON RoomToc (link);");
                    }
                }
        };
    }
}
