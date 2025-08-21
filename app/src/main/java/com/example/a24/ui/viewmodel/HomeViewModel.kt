package com.example.a24.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a24.data.ActivityEntity
import com.example.a24.data.Repository
import com.example.a24.data.UserEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val activities: List<ActivityEntity> = emptyList(),
    val user: UserEntity? = null,
    val todayProgress: Float = 0f,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val errorMessage: String? = null
)

class HomeViewModel(
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _showNotificationPopup = MutableStateFlow<com.example.a24.ui.screens.AppNotification?>(null)
    val showNotificationPopup: StateFlow<com.example.a24.ui.screens.AppNotification?> = _showNotificationPopup.asStateFlow()

    private val auth = FirebaseAuth.getInstance()

    init {
        loadHomeData()
        observeNewNotifications()
    }

    private fun observeNewNotifications() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            repository.getUnreadNotifications(userId)
                .collect { notifications ->
                    if (notifications.isNotEmpty()) {
                        val latest = notifications.first()

                        val appNotification = com.example.a24.ui.screens.AppNotification(
                            id = latest.id,
                            type = try {
                                com.example.a24.ui.screens.NotificationType.valueOf(latest.type)
                            } catch (e: Exception) {
                                com.example.a24.ui.screens.NotificationType.APP
                            },
                            title = latest.title,
                            message = latest.message,
                            timestamp = java.util.Date(latest.timestamp),
                            isRead = latest.isRead,
                            actionText = latest.actionText
                        )

                        if (!appNotification.isRead) {
                            _showNotificationPopup.value = appNotification
                        }
                    }
                }
        }
    }

    private fun loadHomeData() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "User not logged in"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Carica dati utente
                val user = repository.getUser(userId)
                println("DEBUG: User loaded: ${user?.name}")

                // Carica attività di oggi
                val todayActivities = repository.getTodayActivities(userId)
                println("DEBUG: Today activities count: ${todayActivities.size}")

                // Calcola progresso
                val progress = repository.getTodayProgress(userId)
                val completed = todayActivities.count { it.isCompleted }
                val total = todayActivities.size

                println("DEBUG: Progress: $progress, Completed: $completed, Total: $total")

                _uiState.value = HomeUiState(
                    isLoading = false,
                    activities = todayActivities,
                    user = user,
                    todayProgress = progress,
                    completedCount = completed,
                    totalCount = total
                )

            } catch (e: Exception) {
                println("DEBUG: Error loading data: ${e.message}")
                e.printStackTrace()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error loading data: ${e.message}"
                )
            }
        }
    }

    fun addActivity(
        title: String,
        description: String = "",
        address: String? = null,
        category: String = "today",
        priority: Int = 1
    ) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                // Mostra loading
                _uiState.value = _uiState.value.copy(isLoading = true)

                val activityId = repository.addActivity(
                    userId = userId,
                    title = title,
                    description = description,
                    category = category,
                    priority = priority,
                    address = address
                )

                // Debug
                println("DEBUG: Activity added with ID: $activityId")

                // Ricarica i dati
                refreshData()

            } catch (e: Exception) {
                println("DEBUG: Error adding activity: ${e.message}")
                e.printStackTrace()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error adding activity: ${e.message}"
                )
            }
        }
    }

    fun completeActivity(activityId: String) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                repository.completeActivity(activityId, userId)

                // Ricarica i dati
                refreshData()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error completing activity: ${e.message}"
                )
            }
        }
    }

    fun refreshData() {
        loadHomeData()
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun dismissNotificationPopup() {
        _showNotificationPopup.value = null
    }

    fun markNotificationAsReadAndDismiss(notificationId: String) {
        viewModelScope.launch {
            repository.markAsRead(notificationId)
            _showNotificationPopup.value = null
        }
    }


}