package com.example.a24.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.a24.data.ActivityEntity
import com.example.a24.data.AppDatabase
import com.example.a24.data.Repository
import com.example.a24.ui.composables.AppBar
import com.example.a24.ui.managers.LocationManager
import com.example.a24.ui.theme.AppTheme
import com.example.a24.ui.theme.displayFontFamily
import com.example.a24.ui.theme.onPrimaryLight
import com.example.a24.ui.theme.primaryContainerLight
import com.example.a24.ui.theme.primaryContainerLightMediumContrast
import com.example.a24.ui.theme.primaryLight
import com.example.a24.ui.theme.tertiaryLight
import com.example.a24.ui.viewmodel.ActivityDetailViewModel
import com.example.a24.ui.viewmodel.ActivityDetailViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ActivityDetailScreen(
    navController: NavHostController,
    activityId: String
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember {
        Repository(
            database.userDao(),
            database.activityDao(),
            database.notificationDao(),
            database.badgeDao()
        )
    }

    val viewModel: ActivityDetailViewModel = viewModel(
        factory = ActivityDetailViewModelFactory(repository, activityId)
    )

    AppTheme {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            AppBar(currentRoute = "activity_detail", navController = navController)

            ActivityDetailContent(
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun ActivityDetailContent(
    navController: NavHostController,
    viewModel: ActivityDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val locationManager = remember { LocationManager(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            viewModel.requestCurrentLocation(locationManager)
        }
    }

    LaunchedEffect(Unit) {
        if (locationManager.hasLocationPermission()) {
            viewModel.requestCurrentLocation(locationManager)
        }
    }

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.activity == null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Not found",
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Activity not found",
                        style = TextStyle(
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                    )
                }
            }
        }

        else -> {
            ActivityDetailBody(
                activity = uiState.activity!!,
                currentLocation = uiState.currentLocation,
                isLoadingLocation = uiState.isLoadingLocation,
                distanceToActivity = uiState.distanceToActivity,
                locationManager = locationManager,
                onRequestLocation = {
                    if (locationManager.hasLocationPermission()) {
                        viewModel.requestCurrentLocation(locationManager)
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                onCompleteActivity = {
                    viewModel.completeActivity()
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
private fun ActivityDetailBody(
    activity: ActivityEntity,
    currentLocation: com.example.a24.ui.managers.LocationData?,
    isLoadingLocation: Boolean,
    distanceToActivity: Double?,
    locationManager: LocationManager,
    onRequestLocation: () -> Unit,
    onCompleteActivity: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(primaryLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = activity.title,
                                style = TextStyle(
                                    fontFamily = displayFontFamily,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            )

                            if (activity.description.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = activity.description,
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        color = Color.DarkGray
                                    )
                                )
                            }
                        }

                        Icon(
                            imageVector = if (activity.isCompleted) Icons.Default.CheckCircle else Icons.Default.AddCircle,
                            contentDescription = if (activity.isCompleted) "Completed" else "Pending",
                            tint = if (activity.isCompleted) Color(0xFF4CAF50) else Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Activity Details",
                        style = TextStyle(
                            fontFamily = displayFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DetailRow("When", activity.category.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    })
                    DetailRow("Points", "${activity.points} pts")
                    DetailRow("Created", formatDate(activity.createdAt))

                    if (activity.isCompleted && activity.completedAt != null) {
                        DetailRow("Completed", formatDate(activity.completedAt))
                    }
                }
            }

            if (activity.address != null || activity.latitude != null) {
                Spacer(modifier = Modifier.height(16.dp))

                LocationCard(
                    activity = activity,
                    currentLocation = currentLocation,
                    isLoadingLocation = isLoadingLocation,
                    distanceToActivity = distanceToActivity,
                    locationManager = locationManager,
                    onRequestLocation = onRequestLocation
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!activity.isCompleted) {
                Button(
                    onClick = onCompleteActivity,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mark as Completed")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LocationCard(
    activity: ActivityEntity,
    currentLocation: com.example.a24.ui.managers.LocationData?,
    isLoadingLocation: Boolean,
    distanceToActivity: Double?,
    locationManager: LocationManager,
    onRequestLocation: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Location",
                style = TextStyle(
                    fontFamily = displayFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (activity.address != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Address",
                        modifier = Modifier.size(20.dp),
                        tint = primaryLight
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = activity.address,
                        style = TextStyle(fontSize = 14.sp, color = Color.DarkGray),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Current Location",
                    modifier = Modifier.size(20.dp),
                    tint = primaryContainerLightMediumContrast
                )
                Spacer(modifier = Modifier.width(8.dp))

                if (isLoadingLocation) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Getting current location...")
                } else if (currentLocation != null) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = currentLocation.address ?: "Current location",
                            style = TextStyle(fontSize = 14.sp, color = Color.DarkGray)
                        )
                        if (distanceToActivity != null) {
                            Text(
                                text = "Distance: ${formatDistance(distanceToActivity)}",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = primaryContainerLightMediumContrast,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                } else {
                    TextButton(
                        onClick = onRequestLocation
                    ) {
                        Text("Get current location")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (activity.address != null) {
                    Button(
                        onClick = {
                            openAddressInGoogleMaps(activity.address, context)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryLight
                        )
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open in Maps", fontSize = 12.sp, color = Color.White)
                    }
                }

                if (activity.latitude != null && activity.longitude != null) {
                    Button(
                        onClick = {
                            locationManager.openInMaps(
                                activity.latitude,
                                activity.longitude,
                                activity.title
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryContainerLightMediumContrast
                        )
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View Location", fontSize = 12.sp)
                    }
                }

                
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
        )
        Text(
            text = value,
            style = TextStyle(
                fontSize = 14.sp,
                color = Color.Black
            )
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

private fun formatDistance(distanceKm: Double): String {
    return when {
        distanceKm < 1.0 -> "${(distanceKm * 1000).toInt()} m"
        distanceKm < 10.0 -> "${"%.1f".format(distanceKm)} km"
        else -> "${distanceKm.toInt()} km"
    }
}

private fun openAddressInGoogleMaps(address: String, context: android.content.Context) {
    try {
        val encodedAddress = java.net.URLEncoder.encode(address, "UTF-8")
        val mapsUrl = "https://www.google.com/maps/search/?api=1&query=$encodedAddress"

        val intent = android.content.Intent(
            android.content.Intent.ACTION_VIEW,
            android.net.Uri.parse(mapsUrl)
        )
        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK

        intent.setPackage("com.google.android.apps.maps")

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            intent.setPackage(null)
            context.startActivity(intent)
        }

    } catch (e: Exception) {
        try {
            val geoUri = "geo:0,0?q=${java.net.URLEncoder.encode(address, "UTF-8")}"
            val intent = android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse(geoUri)
            )
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}