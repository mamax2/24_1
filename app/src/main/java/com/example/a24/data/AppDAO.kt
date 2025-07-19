package com.example.a24.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUser(userId: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET current_streak = :streak WHERE userId = :userId")
    suspend fun updateStreak(userId: String, streak: Int)

    @Query("UPDATE users SET badges = :badges WHERE userId = :userId")
    suspend fun updateBadges(userId: String, badges: String)

    @Query("UPDATE users SET total_points = total_points + :points, level = :newLevel WHERE userId = :userId")
    suspend fun addPoints(userId: String, points: Int, newLevel: Int)

    @Query("UPDATE users SET last_active = :timestamp WHERE userId = :userId")
    suspend fun updateLastActive(userId: String, timestamp: Long)
}

@Dao
interface ActivityDao {
    // Get today's activities
    @Query("SELECT * FROM activities WHERE userId = :userId ORDER BY created_at DESC")
    suspend fun getTodayActivities(userId: String): List<ActivityEntity>

    // Get activities by category
    @Query("""
        SELECT * FROM activities 
        WHERE userId = :userId AND category = :category 
        ORDER BY priority DESC, created_at DESC
    """)
    suspend fun getActivitiesByCategory(userId: String, category: String): List<ActivityEntity>

    // Get pending activities
    @Query("""
        SELECT * FROM activities 
        WHERE userId = :userId AND is_completed = 0 
        ORDER BY priority DESC, due_date ASC, created_at ASC
    """)
    suspend fun getPendingActivities(userId: String): List<ActivityEntity>

    // Get completed activities
    @Query("""
        SELECT * FROM activities 
        WHERE userId = :userId AND is_completed = 1 
        ORDER BY completed_at DESC
    """)
    suspend fun getCompletedActivities(userId: String): List<ActivityEntity>

    // Insert activity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityEntity)

    // Update activity
    @Update
    suspend fun updateActivity(activity: ActivityEntity)

    // Mark as completed
    @Query("""
        UPDATE activities 
        SET is_completed = :isCompleted, completed_at = :completedAt 
        WHERE id = :activityId
    """)
    suspend fun updateActivityStatus(activityId: String, isCompleted: Boolean, completedAt: Long?)

    // Delete activity
    @Query("DELETE FROM activities WHERE id = :activityId")
    suspend fun deleteActivity(activityId: String)

    // Statistics queries
    @Query("""
        SELECT COUNT(*) FROM activities 
        WHERE userId = :userId 
        AND is_completed = 1 
        AND date(completed_at/1000, 'unixepoch') = date('now')
    """)
    suspend fun getCompletedTodayCount(userId: String): Int

    @Query("""
        SELECT COUNT(*) FROM activities 
        WHERE userId = :userId 
        AND date(created_at/1000, 'unixepoch') = date('now')
    """)
    suspend fun getTotalTodayCount(userId: String): Int

    @Query("""
        SELECT SUM(points) FROM activities 
        WHERE userId = :userId 
        AND is_completed = 1 
        AND date(completed_at/1000, 'unixepoch') = date('now')
    """)
    suspend fun getTodayPoints(userId: String): Int?

    @Query("""
        SELECT COUNT(*) FROM activities 
        WHERE userId = :userId 
        AND is_completed = 1 
        AND completed_at >= :weekStart
    """)
    suspend fun getWeeklyCompletedCount(userId: String, weekStart: Long): Int
}

@Dao
interface NotificationDao {
    // Get all notifications for user (ordered by timestamp DESC)
    @Query("SELECT * FROM notifications WHERE user_id = :userId ORDER BY timestamp DESC")
    fun getAllNotifications(userId: String): Flow<List<NotificationEntity>>

    // Get unread notifications
    @Query("SELECT * FROM notifications WHERE user_id = :userId AND is_read = 0 ORDER BY timestamp DESC")
    fun getUnreadNotifications(userId: String): Flow<List<NotificationEntity>>

    // Get notifications by type
    @Query("SELECT * FROM notifications WHERE user_id = :userId AND type = :type ORDER BY timestamp DESC")
    fun getNotificationsByType(userId: String, type: String): Flow<List<NotificationEntity>>

    // Count unread notifications
    @Query("SELECT COUNT(*) FROM notifications WHERE user_id = :userId AND is_read = 0")
    fun getUnreadCount(userId: String): Flow<Int>

    // Insert notification
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    // Insert multiple notifications
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    // Update notification
    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    // Mark as read
    @Query("UPDATE notifications SET is_read = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: String)

    // Mark all as read for user
    @Query("UPDATE notifications SET is_read = 1 WHERE user_id = :userId")
    suspend fun markAllAsRead(userId: String)

    // Delete notification
    @Query("DELETE FROM notifications WHERE id = :notificationId")
    suspend fun deleteNotification(notificationId: String)

    // Delete all notifications for user
    @Query("DELETE FROM notifications WHERE user_id = :userId")
    suspend fun deleteAllNotifications(userId: String)

    // Delete expired notifications
    @Query("DELETE FROM notifications WHERE expires_at IS NOT NULL AND expires_at < :currentTime")
    suspend fun deleteExpiredNotifications(currentTime: Long)

    // Get specific notification
    @Query("SELECT * FROM notifications WHERE id = :notificationId")
    suspend fun getNotificationById(notificationId: String): NotificationEntity?
}

@Dao
interface BadgeDao {
    @Query("SELECT * FROM user_badges WHERE userId = :userId ORDER BY unlockedAt DESC")
    suspend fun getUserBadges(userId: String): List<UserBadgeEntity>

    @Query("SELECT * FROM user_badges WHERE userId = :userId AND badgeId = :badgeId")
    suspend fun getUserBadge(userId: String, badgeId: String): UserBadgeEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBadge(badge: UserBadgeEntity)

    @Query("SELECT COUNT(*) FROM user_badges WHERE userId = :userId")
    suspend fun getBadgeCount(userId: String): Int
}