package com.mytlogos.enterprise.background.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mytlogos.enterprise.background.room.model.*
import com.mytlogos.enterprise.background.room.model.RoomExternalMediaList.ExternalListMediaJoin
import com.mytlogos.enterprise.background.room.model.RoomMediaList.MediaListMediaJoin

@Database(
    entities = [RoomUser::class, RoomNews::class, RoomExternalUser::class, RoomMediaList::class, MediaListMediaJoin::class, RoomEpisode::class, RoomRelease::class, RoomPart::class, RoomMedium::class, RoomExternalMediaList::class, ExternalListMediaJoin::class, RoomToDownload::class, RoomMediumInWait::class, RoomDanglingMedium::class, RoomFailedEpisode::class, RoomNotification::class, RoomMediumProgress::class, RoomMediumPart::class, RoomPartEpisode::class, RoomEditEvent::class, RoomToc::class],
    version = 16
)
@TypeConverters(Converters::class)
abstract class AbstractDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun newsDao(): NewsDao
    abstract fun externalUserDao(): ExternalUserDao
    abstract fun externalMediaListDao(): ExternalMediaListDao
    abstract fun mediaListDao(): MediaListDao
    abstract fun mediumDao(): MediumDao
    abstract fun partDao(): PartDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun toDownloadDao(): ToDownloadDao
    abstract fun roomMediumInWaitDao(): RoomMediumInWaitDao
    abstract fun roomDanglingDao(): RoomDanglingDao
    abstract fun failedEpisodesDao(): FailedEpisodesDao
    abstract fun notificationDao(): NotificationDao
    abstract fun mediumProgressDao(): MediumProgressDao
    abstract fun dataStructureDao(): DataStructureDao
    abstract fun editDao(): EditDao
    abstract fun tocDao(): TocDao

    companion object {
        private const val DB_Name = "local_database"
        private var INSTANCE: AbstractDatabase? = null

        fun getInstance(context: Context): AbstractDatabase {
            if (INSTANCE == null || !INSTANCE!!.isOpen) {
                synchronized(AbstractDatabase::class.java) {
                    if (INSTANCE == null || !INSTANCE!!.isOpen) {
                        INSTANCE = Room
                                .databaseBuilder(
                                        context.applicationContext,
                                        AbstractDatabase::class.java,
                                        DB_Name)
                                .addMigrations(*migrations())
                                .fallbackToDestructiveMigrationOnDowngrade()
                                .fallbackToDestructiveMigration()
                                .build()
                    }
                }
            }
            return INSTANCE!!
        }

        private fun migrations(): Array<Migration> {
            return arrayOf(
                    object : Migration(7, 8) {
                        override fun migrate(database: SupportSQLiteDatabase) {
                            database.execSQL(
                                    "CREATE TABLE RoomFailedEpisode (" +
                                            "episodeId INTEGER NOT NULL, " +
                                            "failCount INTEGER NOT NULL, " +
                                            "PRIMARY KEY(episodeId), " +
                                            "FOREIGN KEY(`episodeId`) REFERENCES `RoomEpisode`(`episodeId`) ON UPDATE NO ACTION ON DELETE CASCADE" +
                                            ")"
                            )
                            database.execSQL(
                                    "CREATE TABLE RoomNotification (" +
                                            "title TEXT NOT NULL, " +
                                            "description TEXT NOT NULL, " +
                                            "dateTime TEXT NOT NULL, " +
                                            "PRIMARY KEY(title, dateTime)" +
                                            ")"
                            )
                        }
                    },
                    object : Migration(8, 9) {
                        override fun migrate(database: SupportSQLiteDatabase) {
                            database.execSQL("ALTER TABLE RoomRelease ADD COLUMN locked INTEGER NOT NULL DEFAULT 0")
                        }
                    },
                    object : Migration(9, 10) {
                        override fun migrate(database: SupportSQLiteDatabase) {
                            database.execSQL("ALTER TABLE RoomEpisode ADD COLUMN combiIndex REAL NOT NULL DEFAULT 0")
                            database.execSQL("UPDATE RoomEpisode SET combiIndex=CAST((totalIndex || \".\" || COALESCE(partialIndex,0)) as decimal)")
                            database.execSQL("ALTER TABLE RoomPart ADD COLUMN combiIndex REAL NOT NULL DEFAULT 0")
                            database.execSQL("UPDATE RoomPart SET combiIndex=CAST((totalIndex || \".\" || COALESCE(partialIndex,0)) as decimal)")
                        }
                    },
                    object : Migration(10, 11) {
                        override fun migrate(database: SupportSQLiteDatabase) {
                            database.execSQL("CREATE TABLE IF NOT EXISTS RoomMediumProgress " +
                                    "(mediumId INTEGER NOT NULL, currentReadIndex REAL NOT NULL, " +
                                    "PRIMARY KEY(`mediumId`), " +
                                    "FOREIGN KEY(`mediumId`) REFERENCES `RoomMedium`(`mediumId`) " +
                                    "ON UPDATE NO ACTION ON DELETE CASCADE )"
                            )
                            database.execSQL(
                                    "CREATE INDEX index_RoomMediumProgress_mediumId " +
                                            "ON RoomMediumProgress (mediumId);"
                            )
                            database.execSQL(
                                    "CREATE INDEX index_RoomMediumProgress_currentReadIndex " +
                                            "ON RoomMediumProgress (currentReadIndex);"
                            )
                            database.execSQL(
                                    "INSERT OR IGNORE INTO RoomMediumProgress " +
                                            "SELECT RoomMedium.mediumId, MAX(RoomEpisode.combiIndex) FROM RoomMedium " +
                                            "INNER JOIN RoomPart ON RoomMedium.mediumId=RoomPart.mediumId " +
                                            "INNER JOIN RoomEpisode ON RoomPart.partId=RoomEpisode.partId " +
                                            "WHERE progress = 1"
                            )
                        }
                    },
                    object : Migration(11, 12) {
                        override fun migrate(database: SupportSQLiteDatabase) {
                            database.execSQL("CREATE TABLE IF NOT EXISTS RoomMediumPart " +
                                    "(mediumId INTEGER NOT NULL, partId INTEGER NOT NULL, " +
                                    "PRIMARY KEY(`mediumId`, `partId`), " +
                                    "FOREIGN KEY(`mediumId`) REFERENCES `RoomMedium`(`mediumId`) " +
                                    "ON UPDATE NO ACTION ON DELETE CASCADE, " +
                                    "FOREIGN KEY(`partId`) REFERENCES `RoomPart`(`partId`) " +
                                    "ON UPDATE NO ACTION ON DELETE CASCADE)"
                            )
                            database.execSQL("CREATE TABLE IF NOT EXISTS RoomPartEpisode " +
                                    "(episodeId INTEGER NOT NULL, partId INTEGER NOT NULL, " +
                                    "PRIMARY KEY(`episodeId`, `partId`), " +
                                    "FOREIGN KEY(`episodeId`) REFERENCES `RoomEpisode`(`episodeId`) " +
                                    "ON UPDATE NO ACTION ON DELETE CASCADE, " +
                                    "FOREIGN KEY(`partId`) REFERENCES `RoomPart`(`partId`) " +
                                    "ON UPDATE NO ACTION ON DELETE CASCADE)"
                            )
                            database.execSQL(
                                    "CREATE INDEX index_RoomMediumPart_mediumId " +
                                            "ON RoomMediumPart (mediumId);"
                            )
                            database.execSQL(
                                    "CREATE INDEX index_RoomMediumPart_partId " +
                                            "ON RoomMediumPart (partId);"
                            )
                            database.execSQL(
                                    "CREATE INDEX index_RoomPartEpisode_episodeId " +
                                            "ON RoomPartEpisode (episodeId);"
                            )
                            database.execSQL(
                                    "CREATE INDEX index_RoomPartEpisode_partId " +
                                            "ON RoomPartEpisode (partId);"
                            )
                        }
                    },
                    object : Migration(12, 13) {
                        override fun migrate(database: SupportSQLiteDatabase) {
                            database.execSQL("CREATE TABLE IF NOT EXISTS RoomEditEvent " +
                                    "(" +
                                    "id INTEGER NOT NULL, objectType INTEGER NOT NULL, " +
                                    "eventType INTEGER NOT NULL, dateTime TEXT NOT NULL, " +
                                    "firstValue TEXT, secondValue TEXT," +
                                    "PRIMARY KEY(`id`, `objectType`, `eventType`, `dateTime`)" +
                                    ") "
                            )
                        }
                    },
                    object : Migration(13, 14) {
                        override fun migrate(database: SupportSQLiteDatabase) {
                            database.execSQL("CREATE TABLE IF NOT EXISTS RoomToc " +
                                    "(" +
                                    "mediumId INTEGER NOT NULL, link TEXT NOT NULL, " +
                                    "PRIMARY KEY(`mediumId`, `link`), " +
                                    "FOREIGN KEY(`mediumId`) REFERENCES `RoomMedium`(`mediumId`) " +
                                    "ON UPDATE NO ACTION ON DELETE CASCADE " +
                                    ") "
                            )
                            database.execSQL("CREATE INDEX index_RoomToc_mediumId ON RoomToc (mediumId);")
                            database.execSQL("CREATE INDEX index_RoomToc_link ON RoomToc (link);")
                        }
                    }
            )
        }
    }
}