package com.example.a24.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.a24.data.NotificationEntity
import com.example.a24.data.Repository
import com.example.a24.ui.screens.NotificationType
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val repository: Repository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid ?: ""

    private val _notifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    val notifications: StateFlow<List<NotificationEntity>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        if (currentUserId.isBlank()) {
            _isLoading.value = false
            _error.value = "User not logged in"
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                repository.getNotifications(currentUserId)
                    .catch { exception ->
                        _error.value = exception.message ?: "Unknown error occurred"
                        _notifications.value = emptyList()
                        _isLoading.value = false
                    }
                    .collect { notificationList ->
                        _notifications.value = notificationList
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load notifications"
                _notifications.value = emptyList()
                _isLoading.value = false
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                repository.markAsRead(notificationId)
            } catch (e: Exception) {
                _error.value = "Failed to mark notification as read"
            }
        }
    }

    fun markAllAsRead() {
        if (currentUserId.isBlank()) return

        viewModelScope.launch {
            try {
                repository.markAllAsRead(currentUserId)
            } catch (e: Exception) {
                _error.value = "Failed to mark all notifications as read"
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                repository.deleteNotification(notificationId)
            } catch (e: Exception) {
                _error.value = "Failed to delete notification"
            }
        }
    }

    fun createNotification(
        type: NotificationType,
        title: String,
        message: String,
        actionText: String? = null
    ) {
        if (currentUserId.isBlank()) return

        viewModelScope.launch {
            try {
                repository.createNotification(
                    userId = currentUserId,
                    type = type.name,
                    title = title,
                    message = message,
                    actionText = actionText
                )
            } catch (e: Exception) {
                _error.value = "Failed to create notification"
            }
        }
    }



    fun refreshNotifications() {
        loadNotifications()
    }

    // Convert NotificationEntity to your UI model
    fun mapToUINotification(entity: NotificationEntity): com.example.a24.ui.screens.AppNotification {
        return com.example.a24.ui.screens.AppNotification(
            id = entity.id,
            type = try {
                NotificationType.valueOf(entity.type)
            } catch (e: Exception) {
                NotificationType.SYSTEM
            },
            title = entity.title,
            message = entity.message,
            timestamp = java.util.Date(entity.timestamp),
            isRead = entity.isRead,
            actionText = entity.actionText
        )
    }
}

class NotificationViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}