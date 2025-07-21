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

    @Query("UPDATE users SET total_activities = total_activities + :points WHERE userId = :userId")
    suspend fun addPoints(userId: String, points: Int)

    @Query("UPDATE users SET last_active = :timestamp WHERE userId = :userId")
    suspend fun updateLastActive(userId: String, timestamp: Long)
}

@Dao
interface ActivityDao {
    @Query("""
        SELECT * FROM activities 
        WHERE userId = :userId 
        AND date(created_at/1000, 'unixepoch') = date('now') 
        ORDER BY priority DESC, created_at ASC
    """)
    suspend fun getTodayActivities(userId: String): List<ActivityEntity>

    @Query("""
        SELECT * FROM activities 
        WHERE userId = :userId AND category = :category 
        ORDER BY priority DESC, created_at DESC
    """)
    suspend fun getActivitiesByCategory(userId: String, category: String): List<ActivityEntity>

    @Query("""
        SELECT * FROM activities 
        WHERE userId = :userId AND is_completed = 0 
        ORDER BY priority DESC, due_date ASC, created_at ASC
    """)
    suspend fun getPendingActivities(userId: String): List<ActivityEntity>

    @Query("""
        SELECT * FROM activities 
        WHERE userId = :userId AND is_completed = 1 
        ORDER BY completed_at DESC
    """)
    suspend fun getCompletedActivities(userId: String): List<ActivityEntity>

    @Query("SELECT * FROM activities WHERE id = :activityId")
    suspend fun getActivityById(activityId: String): ActivityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityEntity)

    @Update
    suspend fun updateActivity(activity: ActivityEntity)

    @Query("""
        UPDATE activities 
        SET is_completed = :isCompleted, completed_at = :completedAt 
        WHERE id = :activityId
    """)
    suspend fun updateActivityStatus(activityId: String, isCompleted: Boolean, completedAt: Long?)

    @Query("DELETE FROM activities WHERE id = :activityId")
    suspend fun deleteActivity(activityId: String)

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
        SELECT COUNT(*) FROM activities 
        WHERE userId = :userId 
        AND is_completed = 1 
        AND completed_at >= :weekStart
    """)
    suspend fun getWeeklyCompletedCount(userId: String, weekStart: Long): Int
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE user_id = :userId ORDER BY timestamp DESC")
    fun getAllNotifications(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE user_id = :userId AND is_read = 0 ORDER BY timestamp DESC")
    fun getUnreadNotifications(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE user_id = :userId AND type = :type ORDER BY timestamp DESC")
    fun getNotificationsByType(userId: String, type: String): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE user_id = :userId AND is_read = 0")
    fun getUnreadCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET is_read = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: String)

    @Query("UPDATE notifications SET is_read = 1 WHERE user_id = :userId")
    suspend fun markAllAsRead(userId: String)

    @Query("DELETE FROM notifications WHERE id = :notificationId")
    suspend fun deleteNotification(notificationId: String)

    @Query("DELETE FROM notifications WHERE user_id = :userId")
    suspend fun deleteAllNotifications(userId: String)

    @Query("DELETE FROM notifications WHERE expires_at IS NOT NULL AND expires_at < :currentTime")
    suspend fun deleteExpiredNotifications(currentTime: Long)

    @Query("SELECT * FROM notifications WHERE id = :notificationId")
    suspend fun getNotificationById(notificationId: String): NotificationEntity?

    @Query("SELECT COUNT(*) FROM notifications WHERE user_id = :userId")
    suspend fun getNotificationCount(userId: String): Int
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

    @Query("DELETE FROM user_badges WHERE userId = :userId AND badgeId = :badgeId")
    suspend fun deleteBadge(userId: String, badgeId: String)

    @Query("UPDATE user_badges SET isVisible = :isVisible WHERE userId = :userId AND badgeId = :badgeId")
    suspend fun updateBadgeVisibility(userId: String, badgeId: String, isVisible: Boolean)
}