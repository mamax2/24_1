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
    val description: String,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "priority")
    val priority: Int,

    @ColumnInfo(name = "points")
    val points: Int,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "completed_at")
    val completedAt: Long?,

    @ColumnInfo(name = "due_date")
    val dueDate: Long?,

    @ColumnInfo(name = "address")
    val address: String?,

    @ColumnInfo(name = "latitude")
    val latitude: Double?,

    @ColumnInfo(name = "longitude")
    val longitude: Double?
)