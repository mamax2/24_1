package com.example.a24.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.a24.data.ActivityEntity
import com.example.a24.data.Repository
import com.example.a24.ui.managers.LocationData
import com.example.a24.ui.managers.LocationManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ActivityDetailUiState(
    val isLoading: Boolean = true,
    val activity: ActivityEntity? = null,
    val currentLocation: LocationData? = null,
    val isLoadingLocation: Boolean = false,
    val distanceToActivity: Double? = null,
    val errorMessage: String? = null
)

class ActivityDetailViewModel(
    private val repository: Repository,
    private val activityId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityDetailUiState())
    val uiState: StateFlow<ActivityDetailUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()

    init {
        loadActivity()
    }

    private fun loadActivity() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                val activity = repository.getActivityById(activityId)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    activity = activity
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error loading activity: ${e.message}"
                )
            }
        }
    }

    fun requestCurrentLocation(locationManager: LocationManager) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoadingLocation = true)
                val location = locationManager.getCurrentLocation()

                var distance: Double? = null
                val activity = _uiState.value.activity

                if (location != null && activity != null) {
                    distance = when {
                        activity.latitude != null && activity.longitude != null -> {
                            locationManager.calculateDistance(
                                location.latitude, location.longitude,
                                activity.latitude, activity.longitude
                            )
                        }
                        activity.address != null -> {
                            try {
                                val (addressLocation, calculatedDistance) = locationManager.getLocationFromAddressWithDistance(
                                    activity.address, location
                                )
                                calculatedDistance
                            } catch (e: Exception) {
                                null
                            }
                        }
                        else -> null
                    }
                }

                _uiState.value = _uiState.value.copy(
                    currentLocation = location,
                    distanceToActivity = distance,
                    isLoadingLocation = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingLocation = false,
                    errorMessage = "Error getting location: ${e.message}"
                )
            }
        }
    }

    fun setCurrentLocation(location: LocationData?) {
        _uiState.value = _uiState.value.copy(
            currentLocation = location,
            isLoadingLocation = false
        )
    }

    fun calculateDistanceToActivity(locationManager: LocationManager) {
        val location = _uiState.value.currentLocation
        val activity = _uiState.value.activity

        if (location != null && activity != null) {
            viewModelScope.launch {
                try {
                    val distance = when {
                        activity.latitude != null && activity.longitude != null -> {
                            locationManager.calculateDistance(
                                location.latitude, location.longitude,
                                activity.latitude, activity.longitude
                            )
                        }
                        activity.address != null -> {
                            val (_, calculatedDistance) = locationManager.getLocationFromAddressWithDistance(
                                activity.address, location
                            )
                            calculatedDistance
                        }
                        else -> null
                    }

                    _uiState.value = _uiState.value.copy(distanceToActivity = distance)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Error calculating distance: ${e.message}"
                    )
                }
            }
        }
    }

    fun completeActivity() {
        val userId = auth.currentUser?.uid ?: return
        val activity = _uiState.value.activity ?: return

        viewModelScope.launch {
            try {
                repository.completeActivity(activity.id, userId)

                val updatedActivity = activity.copy(
                    isCompleted = true,
                    completedAt = System.currentTimeMillis()
                )
                _uiState.value = _uiState.value.copy(activity = updatedActivity)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error completing activity: ${e.message}"
                )
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

class ActivityDetailViewModelFactory(
    private val repository: Repository,
    private val activityId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActivityDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ActivityDetailViewModel(repository, activityId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}