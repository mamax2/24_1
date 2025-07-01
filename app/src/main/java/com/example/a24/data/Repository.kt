package com.example.a24.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class Repository(
    private val userDao: UserDao,
    private val activityDao: ActivityDao,
    private val notificationDao: NotificationDao,
    private val badgeDao: BadgeDao
) {

    // ===== USER OPERATIONS =====
    suspend fun initializeUser(userId: String, name: String, email: String) {
        val existingUser = userDao.getUser(userId)
        if (existingUser == null) {
            val user = UserEntity(
                userId = userId,
                name = name,
                email = email
            )
            userDao.insertUser(user)

            // Award first login badge
            awardBadge(userId, "first_login", "First Login", "Welcome to 24+1!", "🎉")

            // Create welcome notification
            createNotification(
                userId = userId,
                type = "ACHIEVEMENT",
                title = "Welcome to 24+1! 🎉",
                message = "Start your productivity journey today!"
            )
        }
    }

    suspend fun getUser(userId: String): UserEntity? = userDao.getUser(userId)

    suspend fun updateUserProfile(userId: String, name: String, profileImageUrl: String?) {
        val user = userDao.getUser(userId)
        user?.let {
            val updatedUser = it.copy(
                name = name,
                profileImageUrl = profileImageUrl,
                lastActive = System.currentTimeMillis()
            )
            userDao.updateUser(updatedUser)
        }
    }

    // ===== ACTIVITY OPERATIONS =====
    suspend fun addActivity(
        userId: String,
        title: String,
        description: String = "",
        category: String = "today",
        priority: Int = 1,
        points: Int = 10
    ): String {
        val activityId = java.util.UUID.randomUUID().toString()
        val activity = ActivityEntity(
            id = activityId,
            userId = userId,
            title = title,
            description = description,
            category = category,
            priority = priority,
        )
        activityDao.insertActivity(activity)
        return activityId
    }

    suspend fun getTodayActivities(userId: String): List<ActivityEntity> {
        return activityDao.getTodayActivities(userId)
    }

   /* suspend fun completeActivity(activityId: String, userId: String) {
        val activity = activityDao.getPendingActivities(userId).find { it.id == activityId }
        activity?.let {
            activityDao.updateActivityStatus(activityId, true, System.currentTimeMillis())

            // Add points
            val user = userDao.getUser(userId)
            user?.let { u ->
                val newPoints = u.totalPoints + it.points
                val newLevel = calculateLevel(newPoints)
                userDao.addPoints(userId, it.points, newLevel)

                // Check for level up
                if (newLevel > u.level) {
                    createNotification(
                        userId = userId,
                        type = "ACHIEVEMENT",
                        title = "Level Up! 🆙",
                        message = "You reached level $newLevel!"
                    )
                }
            }

            // Check daily progress
            checkDailyProgress(userId)
        }
    }*/

    suspend fun getTodayProgress(userId: String): Float {
        val completed = activityDao.getCompletedTodayCount(userId)
        val total = activityDao.getTotalTodayCount(userId)
        return if (total > 0) completed.toFloat() / total.toFloat() else 0f
    }

    // ===== GAMIFICATION =====
    private suspend fun checkDailyProgress(userId: String) {
        val progress = getTodayProgress(userId)
        val completed = activityDao.getCompletedTodayCount(userId)

        when {
            progress >= 1.0f -> {
                awardBadge(userId, "perfect_day", "Perfect Day", "Completed all today's tasks!", "⭐")
            }
            completed >= 5 -> {
                awardBadge(userId, "productive", "Productive", "Completed 5+ tasks today!", "💪")
            }
        }
    }

    private suspend fun awardBadge(userId: String, badgeId: String, name: String, description: String, icon: String) {
        val existing = badgeDao.getUserBadge(userId, badgeId)
        if (existing == null) {
            val badge = UserBadgeEntity(
                userId = userId,
                badgeId = badgeId,
                badgeName = name,
                badgeDescription = description,
                badgeIcon = icon
            )
            badgeDao.insertBadge(badge)

            // Create notification
            createNotification(
                userId = userId,
                type = "ACHIEVEMENT",
                title = "New Badge Unlocked! $icon",
                message = "$name: $description"
            )
        }
    }

    suspend fun getUserBadges(userId: String): List<UserBadgeEntity> {
        return badgeDao.getUserBadges(userId)
    }

    private fun calculateLevel(points: Int): Int {
        return (points / 100) + 1 // 100 punti per livello
    }

    // ===== NOTIFICATIONS =====
    suspend fun createNotification(
        userId: String,
        type: String,
        title: String,
        message: String,
        actionText: String? = null,
        actionData: String? = null
    ) {
        val notification = NotificationEntity(
            id = java.util.UUID.randomUUID().toString(),
            userId = userId,
            type = type,
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            actionText = actionText,
            actionData = actionData
        )
        notificationDao.insertNotification(notification)
    }

    fun getNotifications(userId: String): Flow<List<NotificationEntity>> {
        return notificationDao.getAllNotifications(userId)
    }

    suspend fun markAsRead(notificationId: String) {
        notificationDao.markAsRead(notificationId)
    }

    suspend fun markAllAsRead(userId: String) {
        notificationDao.markAllAsRead(userId)
    }

    suspend fun deleteNotification(notificationId: String) {
        notificationDao.deleteNotification(notificationId)
    }

    suspend fun addNotification(
        userId: String,
        type: String,
        title: String,
        message: String,
        actionText: String? = null
    ) {
        val notification = NotificationEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            type = type,
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            actionText = actionText
        )
        notificationDao.insertNotification(notification)
    }

    suspend fun createInitialNotifications(userId: String) {
        val count = 0
        if (count == 0) {
            val now = System.currentTimeMillis()
            val initialNotifications = listOf(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = "ACHIEVEMENT",
                    title = "🎉 Welcome to 24+1!",
                    message = "Start your productivity journey today!",
                    timestamp = now - 300000,
                    isRead = false,
                    actionText = "Get Started"
                ),
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = "SYSTEM",
                    title = "Setup Complete",
                    message = "Your account has been configured successfully.",
                    timestamp = now - 600000,
                    isRead = true
                )
            )
            notificationDao.insertNotifications(initialNotifications)
        }
    }
}