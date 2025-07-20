package com.example.a24.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a24.data.NotificationEntity
import com.example.a24.data.Repository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// UI State per NotificationScreen
data class NotificationUiState(
    val isLoading: Boolean = false,
    val notifications: List<NotificationEntity> = emptyList(),
    val unreadCount: Int = 0,
    val selectedFilter: String? = null,
    val errorMessage: String? = null
)

class NotificationViewModel(
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()

    init {
        loadNotifications()
    }

    /**
     * Carica tutte le notifiche dell'utente
     */
    private fun loadNotifications() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "User not logged in"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Usa Flow per osservare le notifiche in tempo reale
                repository.getNotifications(userId).collect { notifications ->
                    val unreadCount = notifications.count { !it.isRead }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        notifications = notifications,
                        unreadCount = unreadCount
                    )
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error loading notifications: ${e.message}"
                )
            }
        }
    }

    /**
     * Segna una notifica come letta
     */
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                repository.markAsRead(notificationId)
                // Le notifiche si aggiorneranno automaticamente grazie al Flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error marking notification as read: ${e.message}"
                )
            }
        }
    }

    /**
     * Segna tutte le notifiche come lette
     */
    fun markAllAsRead() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                repository.markAllAsRead(userId)
                // Le notifiche si aggiorneranno automaticamente grazie al Flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error marking all notifications as read: ${e.message}"
                )
            }
        }
    }

    /**
     * Elimina una notifica
     */
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                repository.deleteNotification(notificationId)
                // Le notifiche si aggiorneranno automaticamente grazie al Flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error deleting notification: ${e.message}"
                )
            }
        }
    }

    /**
     * Applica filtro per tipo di notifica
     */
    fun applyFilter(filterType: String?) {
        _uiState.value = _uiState.value.copy(selectedFilter = filterType)
    }

    /**
     * Ottiene le notifiche filtrate
     */
    fun getFilteredNotifications(): List<NotificationEntity> {
        val allNotifications = _uiState.value.notifications
        val filter = _uiState.value.selectedFilter

        return if (filter != null) {
            allNotifications.filter { it.type == filter }
        } else {
            allNotifications
        }
    }

    /**
     * Pulisce i messaggi di errore
     */
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Crea una notifica di test
     */
    fun createTestNotification() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val testNotifications = listOf(
                    Triple("ACHIEVEMENT", "🏆 New Achievement!", "You've completed 3 activities today!"),
                    Triple("SYSTEM", "📱 App Update", "New features are available in version 2.0"),
                    Triple("REMINDER", "⏰ Daily Reminder", "Don't forget to check in today!"),
                    Triple("SECURITY", "🔒 Security Alert", "New login detected from Android device")
                )

                val randomNotification = testNotifications.random()
                repository.createNotification(
                    userId = userId,
                    type = randomNotification.first,
                    title = randomNotification.second,
                    message = randomNotification.third
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error creating test notification: ${e.message}"
                )
            }
        }
    }

    /**
     * Gestisce l'azione di una notifica
     */
    fun handleNotificationAction(notification: NotificationEntity) {
        // Segna come letta se non lo è già
        if (!notification.isRead) {
            markAsRead(notification.id)
        }

        // Qui puoi aggiungere logica specifica per ogni tipo di notifica
        when (notification.type) {
            "ACHIEVEMENT" -> {
                // Naviga alla schermata badge/profilo
                // Questo verrà gestito nella UI
            }
            "SECURITY" -> {
                // Naviga alle impostazioni di sicurezza
            }
            "REMINDER" -> {
                // Naviga alla home per il check-in
            }
            // Altri tipi...
        }
    }
}