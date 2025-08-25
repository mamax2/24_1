package com.example.a24.ui.managers

import com.example.a24.data.Repository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationManager(
    private val repository: Repository
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val auth = FirebaseAuth.getInstance()

    fun sendWelcomeNotification(userId: String) {
        scope.launch {
            repository.createNotification(
                userId = userId,
                type = "ACHIEVEMENT",
                title = "🎉 Welcome to 24+1!",
                message = "Start your productivity journey today! Explore activities and build your daily streak.",
                actionText = "Get Started"
            )
        }
    }

    fun sendBadgeUnlockedNotification(userId: String, badgeName: String, badgeIcon: String) {
        scope.launch {
            repository.createNotification(
                userId = userId,
                type = "ACHIEVEMENT",
                title = "🏆 New Badge Unlocked!",
                message = "Congratulations! You've earned the '$badgeName' badge $badgeIcon",
                actionText = "View Badges"
            )
        }
    }

    fun sendStreakNotification(userId: String, streakDays: Int) {
        val (title, message) = when {
            streakDays == 7 -> "🔥 Week Streak!" to "Amazing! You've maintained a 7-day streak!"
            streakDays == 30 -> "🔥 Month Streak!" to "Incredible! You've maintained a 30-day streak!"
            streakDays % 10 == 0 -> "🔥 ${streakDays}-Day Streak!" to "Outstanding! You've maintained a $streakDays-day streak!"
            else -> return
        }

        scope.launch {
            repository.createNotification(
                userId = userId,
                type = "ACHIEVEMENT",
                title = title,
                message = message,
                actionText = "View Profile"
            )
        }
    }

    fun sendActivityCompletedNotification(userId: String, activityTitle: String, pointsEarned: Int) {
        scope.launch {
            repository.createNotification(
                userId = userId,
                type = "ACHIEVEMENT",
                title = "✅ Activity Completed!",
                message = "Great job completing '$activityTitle'! You earned $pointsEarned points.",
                actionText = "View Progress"
            )
        }
    }

    fun sendLevelUpNotification(userId: String, newLevel: Int) {
        scope.launch {
            repository.createNotification(
                userId = userId,
                type = "ACHIEVEMENT",
                title = "🆙 Level Up!",
                message = "Congratulations! You've reached Level $newLevel!",
                actionText = "View Profile"
            )
        }
    }

    fun sendDailyReminderNotification(userId: String) {
        scope.launch {
            repository.createNotification(
                userId = userId,
                type = "REMINDER",
                title = "📅 Daily Check-in",
                message = "Don't forget your daily activities! Keep your streak alive.",
                actionText = "View Activities"
            )
        }
    }



    fun sendAppUpdateNotification(userId: String, version: String) {
        scope.launch {
            repository.createNotification(
                userId = userId,
                type = "SYSTEM",
                title = "📱 App Updated",
                message = "24+1 has been updated to version $version with new features and improvements.",
                actionText = "What's New"
            )
        }
    }



    fun sendPerfectDayNotification(userId: String) {
        scope.launch {
            repository.createNotification(
                userId = userId,
                type = "ACHIEVEMENT",
                title = "⭐ Perfect Day!",
                message = "Congratulations! You've completed all your activities for today!",
                actionText = "View Stats"
            )
        }
    }

    // Notifica per inattività (da chiamare periodicamente)
    fun sendInactivityNotification(userId: String, daysSinceLastActivity: Int) {
        if (daysSinceLastActivity < 3) return

        scope.launch {
            repository.createNotification(
                userId = userId,
                type = "REMINDER",
                title = "👋 We miss you!",
                message = "It's been $daysSinceLastActivity days since your last activity. Come back and continue your journey!",
                actionText = "Resume"
            )
        }
    }

    fun initializeNotificationsForNewUser(userId: String) {
        scope.launch {
            try {
                sendWelcomeNotification(userId)

                repository.createNotification(
                    userId = userId,
                    type = "APP",
                    title = "🏆 Collect Badges",
                    message = "Complete activities and achieve milestones to unlock special badges!",
                    actionText = "View Badges"
                )

            } catch (e: Exception) {
            }
        }
    }


}