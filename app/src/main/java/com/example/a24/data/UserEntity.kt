package com.example.a24.data

import androidx.room.*

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "profile_image_url")
    val profileImageUrl: String? = null,

    @ColumnInfo(name = "current_streak")
    val currentStreak: Int = 0,

    @ColumnInfo(name = "total_activities")
    val totalActivities: Int = 0,

    @ColumnInfo(name = "total_points")
    val totalPoints: Int = 0,

    @ColumnInfo(name = "level")
    val level: Int = 1,

    @ColumnInfo(name = "badges")
    val badges: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_active")
    val lastActive: Long = System.currentTimeMillis()
)