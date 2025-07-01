package com.example.a24.data

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "user_badges",
    primaryKeys = ["userId", "badgeId"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserBadgeEntity(
    val userId: String,
    val badgeId: String,
    val badgeName: String,
    val badgeDescription: String,
    val badgeIcon: String,
    val unlockedAt: Long = System.currentTimeMillis(),
    val isVisible: Boolean = true
)

// Badge definitions
data class BadgeDefinition(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val color: String,
    val condition: BadgeCondition
)

sealed class BadgeCondition {
    object FirstLogin : BadgeCondition()
    data class StreakDays(val days: Int) : BadgeCondition()
    data class CompletedTasks(val count: Int) : BadgeCondition()
    data class TotalPoints(val points: Int) : BadgeCondition()
    data class ConsecutiveWeeks(val weeks: Int) : BadgeCondition()
}