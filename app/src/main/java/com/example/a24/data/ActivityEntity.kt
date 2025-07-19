package com.example.a24.data

import androidx.room.*

@Entity(
    tableName = "activities",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ActivityEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "userId")
    val userId: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "category")
    val category: String = "today", // "today", "tomorrow", "week"

    @ColumnInfo(name = "priority")
    val priority: Int = 1, // 0=low, 1=medium, 2=high

    @ColumnInfo(name = "points")
    val points: Int = 10,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,

    @ColumnInfo(name = "due_date")
    val dueDate: Long? = null
)