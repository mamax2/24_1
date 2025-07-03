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
        val activityId = UUID.randomUUID().toString()
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

    suspend fun completeActivity(activityId: String) {
        activityDao.updateActivityStatus(activityId, true, System.currentTimeMillis())
    }

    suspend fun deleteActivity(activityId: String) {
        activityDao.deleteActivity(activityId)
    }

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
            id = UUID.randomUUID().toString(),
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

    // ===== POPULATE WITH SAMPLE DATA =====
    suspend fun populateWithSampleData() {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Inizializza l'utente se non esiste
        val existingUser = userDao.getUser(userId)
        if (existingUser == null) {
            val user = UserEntity(
                userId = userId,
                name = "Sample User",
                email = "sample@example.com"
            )
            userDao.insertUser(user)
        }

        // Aggiungi attività di esempio
        val sampleActivities = listOf(
            "Morning workout" to "30 minutes at the gym",
            "Read a book" to "Finish chapter 3 of my current book",
            "Call mom" to "Weekly check-in call",
            "Grocery shopping" to "Buy ingredients for dinner",
            "Work on project" to "Complete the presentation slides",
            "Meditation" to "10 minutes mindfulness practice",
            "Cook dinner" to "Try the new pasta recipe",
            "Walk the dog" to "Evening walk in the park"
        )

        sampleActivities.forEach { (title, description) ->
            addActivity(
                userId = userId,
                title = title,
                description = description,
                category = "today",
                priority = (0..2).random() // Random priority
            )
        }

        // Completa alcune attività a caso
        val allActivities = getTodayActivities(userId)
        allActivities.take(3).forEach { activity ->
            completeActivity(activity.id)
        }
    }
}