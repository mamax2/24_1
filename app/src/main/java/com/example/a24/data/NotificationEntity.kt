package com.example.a24.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

// Entity per le notifiche
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,

    val userId: String, // Per associare all'utente Firebase

    @ColumnInfo(name = "type")
    val type: String, // ACHIEVEMENT, SECURITY, REMINDER, etc.

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "message")
    val message: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,


    @ColumnInfo(name = "action_text")
    val actionText: String? = null,

    @ColumnInfo(name = "action_data")
    val actionData: String? = null, // JSON per dati extra

    @ColumnInfo(name = "priority")
    val priority: Int = 0, // 0=bassa, 1=media, 2=alta

    @ColumnInfo(name = "expires_at")
    val expiresAt: Long? = null // Timestamp scadenza (opzionale)
)

// Type Converters per Date
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

// DAO per le operazioni database
@Dao
interface NotificationDao {

    // Ottieni tutte le notifiche per utente (ordinate per timestamp DESC)
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllNotifications(userId: String): Flow<List<NotificationEntity>>

    // Ottieni solo notifiche non lette
    @Query("SELECT * FROM notifications WHERE userId = :userId AND is_read = 0 ORDER BY timestamp DESC")
    fun getUnreadNotifications(userId: String): Flow<List<NotificationEntity>>

    // Ottieni notifiche per tipo
    @Query("SELECT * FROM notifications WHERE userId = :userId AND type = :type ORDER BY timestamp DESC")
    fun getNotificationsByType(userId: String, type: String): Flow<List<NotificationEntity>>

    // Conta notifiche non lette
    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND is_read = 0")
    fun getUnreadCount(userId: String): Flow<Int>

    // Inserisci notifica
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    // Inserisci multiple notifiche
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    // Aggiorna notifica
    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    // Marca come letta
    @Query("UPDATE notifications SET is_read = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: String)

    // Marca tutte come lette per utente
    @Query("UPDATE notifications SET is_read = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)

    // Cancella notifica
    @Query("DELETE FROM notifications WHERE id = :notificationId")
    suspend fun deleteNotification(notificationId: String)

    // Cancella tutte le notifiche per utente
    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun deleteAllNotifications(userId: String)

    // Cancella notifiche scadute
    @Query("DELETE FROM notifications WHERE expires_at IS NOT NULL AND expires_at < :currentTime")
    suspend fun deleteExpiredNotifications(currentTime: Long)

    // Cancella notifiche vecchie (più di X giorni)
    @Query("DELETE FROM notifications WHERE userId = :userId AND timestamp < :cutoffTime")
    suspend fun deleteOldNotifications(userId: String, cutoffTime: Long)

    // Ottieni notifica specifica
    @Query("SELECT * FROM notifications WHERE id = :notificationId")
    suspend fun getNotificationById(notificationId: String): NotificationEntity?
}

// Database principale
@Database(
    entities = [NotificationEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}