package com.example.a24.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a24.data.ActivityEntity
import com.example.a24.data.Repository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val activities: List<ActivityEntity> = emptyList(),
    val completedToday: Int = 0,
    val totalToday: Int = 0,
    val todayProgress: Float = 0f,
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel(
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()

    init {
        loadTodayActivities()
        fun populateWithSampleData() {
            val userId = auth.currentUser?.uid
            val userEmail = auth.currentUser?.email ?: ""
            val userName = auth.currentUser?.displayName ?: "Sample User"

            if (userId == null) {
                println("🔴 HomeViewModel: No user logged in!")
                _uiState.value = _uiState.value.copy(error = "User not logged in")
                return
            }

            viewModelScope.launch {
                try {
                    println("🔵 HomeViewModel: Starting populate with sample data for user: $userId")

                    // PRIMA: Assicurati che l'utente esista
                    repository.initializeUser(userId, userName, userEmail)

                    // POI: Popola con dati di esempio
                    repository.populateWithSampleData()

                    println("🟢 HomeViewModel: Sample data populated successfully!")
                    loadTodayActivities() // Refresh
                } catch (e: Exception) {
                    println("🔴 HomeViewModel: Error populating data: ${e.message}")
                    e.printStackTrace()
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
            }
        }
    }

    fun loadTodayActivities() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                println("🔵 HomeViewModel: Loading activities for user: $userId")
                val activities = repository.getTodayActivities(userId)
                println("🟢 HomeViewModel: Loaded ${activities.size} activities")

                val completedCount = activities.count { it.isCompleted }
                val totalCount = activities.size
                val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

                _uiState.value = _uiState.value.copy(
                    activities = activities,
                    completedToday = completedCount,
                    totalToday = totalCount,
                    todayProgress = progress,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                println("🔴 HomeViewModel: Error loading activities: ${e.message}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun addActivity(title: String, description: String, category: String = "today") {
        val userId = auth.currentUser?.uid ?: return
        val userEmail = auth.currentUser?.email ?: ""
        val userName = auth.currentUser?.displayName ?: "User"

        viewModelScope.launch {
            try {
                println("🔵 ADDING ACTIVITY: $title for user: $userId")

                // PRIMA: Assicurati che l'utente esista nel database
                repository.initializeUser(userId, userName, userEmail)

                // POI: Aggiungi l'attività
                repository.addActivity(
                    userId = userId,
                    title = title,
                    description = description,
                    category = category
                )
                println("🟢 ACTIVITY ADDED SUCCESSFULLY")
                loadTodayActivities() // Refresh
            } catch (e: Exception) {
                println("🔴 ERROR ADDING ACTIVITY: ${e.message}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun completeActivity(activityId: String) {
        viewModelScope.launch {
            try {
                repository.completeActivity(activityId)
                loadTodayActivities() // Refresh
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteActivity(activityId: String) {
        viewModelScope.launch {
            try {
                repository.deleteActivity(activityId)
                loadTodayActivities() // Refresh
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}