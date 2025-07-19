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
    version = 2, // ← AUMENTATO DA 1 A 2
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

        // Migrazione dalla versione 1 alla 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Aggiungi i campi mancanti alla tabella users
                database.execSQL("ALTER TABLE users ADD COLUMN total_points INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE users ADD COLUMN level INTEGER NOT NULL DEFAULT 1")

                // Aggiungi il campo points alla tabella activities
                database.execSQL("ALTER TABLE activities ADD COLUMN points INTEGER NOT NULL DEFAULT 10")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_1_2) // ← AGGIUNGI LA MIGRAZIONE
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