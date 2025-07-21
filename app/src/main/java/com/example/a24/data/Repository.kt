package com.example.a24.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class Repository(
    private val userDao: UserDao,
    private val activityDao: ActivityDao,
    private val notificationDao: NotificationDao,
    private val badgeDao: BadgeDao
) {

    //user
    suspend fun initializeUser(userId: String, name: String, email: String) {
        val existingUser = userDao.getUser(userId)
        if (existingUser == null) {
            val user = UserEntity(
                userId = userId,
                name = name,
                email = email
            )
            userDao.insertUser(user)

            // badge primo login
            awardBadge(userId, "first_login", "First Login", "Welcome to 24+1!", "🎉")
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

    //attività
    suspend fun addActivity(
        userId: String,
        title: String,
        description: String = "",
        category: String = "today",
        priority: Int = 1
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

    suspend fun completeActivity(activityId: String, userId: String) {
        val activity = activityDao.getPendingActivities(userId).find { it.id == activityId }
        activity?.let {
            activityDao.updateActivityStatus(activityId, true, System.currentTimeMillis())

            val user = userDao.getUser(userId)
            user?.let { u ->
                val pointsToAdd = 10 // punti base per attività
                val newTotalPoints = u.totalActivities + pointsToAdd
                val newLevel = calculateLevel(newTotalPoints)

                val updatedUser = u.copy(
                    totalActivities = u.totalActivities + 1,
                    lastActive = System.currentTimeMillis()
                )
                userDao.updateUser(updatedUser)

                if (newLevel > calculateLevel(u.totalActivities)) {
                    createNotification(
                        userId = userId,
                        type = "ACHIEVEMENT",
                        title = "🆙 Level Up!",
                        message = "You reached level $newLevel!",
                        actionText = "View Profile"
                    )
                }
            }

            checkDailyProgress(userId)
        }
    }

    suspend fun getTodayProgress(userId: String): Float {
        val completed = activityDao.getCompletedTodayCount(userId)
        val total = activityDao.getTotalTodayCount(userId)
        return if (total > 0) completed.toFloat() / total.toFloat() else 0f
    }

    //gamification
    private suspend fun checkDailyProgress(userId: String) {
        val progress = getTodayProgress(userId)
        val completed = activityDao.getCompletedTodayCount(userId)

        when {
            progress >= 1.0f -> {
                awardBadge(userId, "perfect_day", "Perfect Day", "Completed all today's tasks!", "⭐")
                createNotification(
                    userId = userId,
                    type = "ACHIEVEMENT",
                    title = "⭐ Perfect Day!",
                    message = "Congratulations! You've completed all your activities for today!",
                    actionText = "View Stats"
                )
            }
            completed >= 5 -> {
                awardBadge(userId, "productive", "Productive", "Completed 5+ tasks today!", "💪")
            }
            completed >= 3 -> {
                awardBadge(userId, "good_start", "Good Start", "Completed 3+ tasks today!", "👍")
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

            // notifica per nuovo badge
            createNotification(
                userId = userId,
                type = "ACHIEVEMENT",
                title = "🏆 New Badge Unlocked!",
                message = "$name: $description",
                actionText = "View Badges"
            )
        }
    }

    suspend fun getUserBadges(userId: String): List<UserBadgeEntity> {
        return badgeDao.getUserBadges(userId)
    }

    private fun calculateLevel(totalActivities: Int): Int {
        return (totalActivities / 10) + 1 // 1 level per ogni 10 attività completate
    }

    // notifiche
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

    fun getUnreadNotifications(userId: String): Flow<List<NotificationEntity>> {
        return notificationDao.getUnreadNotifications(userId)
    }

    fun getUnreadCount(userId: String): Flow<Int> {
        return notificationDao.getUnreadCount(userId)
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
        // Controlla se l'utente ha già notifiche
        val existingNotifications = notificationDao.getAllNotifications(userId)

        // Se non ha notifiche, crea quelle iniziali
        val now = System.currentTimeMillis()
        val initialNotifications = listOf(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                type = "ACHIEVEMENT",
                title = "🎉 Welcome to 24+1!",
                message = "Start your productivity journey today! Complete activities and earn badges.",
                timestamp = now - 300000, // 5 minuti fa
                isRead = false,
                actionText = "Get Started"
            ),
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                type = "SYSTEM",
                title = "💡 Getting Started",
                message = "Tap on activities to complete them, earn points, and unlock badges!",
                timestamp = now - 600000, // 10 minuti fa
                isRead = false,
                actionText = "Learn More"
            ),
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                type = "MARKETING",
                title = "🏆 Collect Badges",
                message = "Complete activities and achieve milestones to unlock special badges!",
                timestamp = now - 900000, // 15 minuti fa
                isRead = true,
                actionText = "View Badges"
            )
        )
        notificationDao.insertNotifications(initialNotifications)
    }

    //utility
    suspend fun cleanupExpiredNotifications() {
        notificationDao.deleteExpiredNotifications(System.currentTimeMillis())
    }


    suspend fun updateUserLastActive(userId: String) {
        userDao.updateLastActive(userId, System.currentTimeMillis())
    }

}