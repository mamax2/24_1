package com.example.a24.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "user_id")
    val userId: String, // Per associare all'utente Firebase

    @ColumnInfo(name = "type")
    val type: String, // ACHIEVEMENT, SECURITY, REMINDER, etc.

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "message")
    val message: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,

    @ColumnInfo(name = "action_text")
    val actionText: String? = null,

    @ColumnInfo(name = "action_data")
    val actionData: String? = null, // JSON per dati extra

    @ColumnInfo(name = "priority")
    val priority: Int = 0, // 0=bassa, 1=media, 2=alta

    @ColumnInfo(name = "expires_at")
    val expiresAt: Long? = null
)
