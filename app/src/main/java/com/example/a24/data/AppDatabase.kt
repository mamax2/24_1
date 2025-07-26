package com.example.a24.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserEntity::class,
        ActivityEntity::class,
        NotificationEntity::class,
        UserBadgeEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun activityDao(): ActivityDao
    abstract fun notificationDao(): NotificationDao
    abstract fun badgeDao(): BadgeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN total_points INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE users ADD COLUMN level INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE activities ADD COLUMN points INTEGER NOT NULL DEFAULT 10")
            }
        }

        // Nuova migrazione per aggiungere campi di localizzazione
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Aggiungi le nuove colonne con valori di default NULL
                database.execSQL("ALTER TABLE activities ADD COLUMN address TEXT DEFAULT NULL")
                database.execSQL("ALTER TABLE activities ADD COLUMN latitude REAL DEFAULT NULL")
                database.execSQL("ALTER TABLE activities ADD COLUMN longitude REAL DEFAULT NULL")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database_v3"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun clearDatabase(context: Context) {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
                context.deleteDatabase("app_database")
            }
        }
    }
}