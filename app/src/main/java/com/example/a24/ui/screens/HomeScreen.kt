package com.example.a24.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.a24.data.ActivityEntity
import com.example.a24.data.AppDatabase
import com.example.a24.data.Repository
import com.example.a24.ui.composables.AppBar
import com.example.a24.ui.composables.NotificationToast
import com.example.a24.ui.composables.SectionHeader
import com.example.a24.ui.managers.LocationManager
import com.example.a24.ui.theme.AppTheme
import com.example.a24.ui.theme.displayFontFamily
import com.example.a24.ui.theme.onPrimaryLight
import com.example.a24.ui.theme.onPrimaryLightMediumContrast
import com.example.a24.ui.theme.primaryContainerLightMediumContrast
import com.example.a24.ui.theme.primaryLight
import com.example.a24.ui.theme.tertiaryLight
import com.example.a24.ui.viewmodel.HomeViewModel
import com.example.a24.ui.viewmodel.HomeUiState

@Composable
fun HomeScreen(navController: NavHostController) {
    val context = LocalContext.current

    val database = AppDatabase.getDatabase(context)
    val repository = Repository(
        database.userDao(),
        database.activityDao(),
        database.notificationDao(),
        database.badgeDao()
    )

    val viewModel: HomeViewModel = viewModel {
        HomeViewModel(repository)
    }

    val showNotificationPopup by viewModel.showNotificationPopup.collectAsState()

    AppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                AppBar(currentRoute = "home", navController = navController)
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(text = "TODAY'S ACTIVITIES")
                HomeContent(navController = navController, viewModel = viewModel)
            }

            NotificationToast(
                notification = showNotificationPopup,
                onDismiss = {
                    viewModel.dismissNotificationPopup()
                },
                onTap = {
                    showNotificationPopup?.let { notification ->
                        viewModel.markNotificationAsReadAndDismiss(notification.id)

                        // Naviga in base al tipo di notifica
                        when (notification.type) {
                            com.example.a24.ui.screens.NotificationType.ACHIEVEMENT -> {
                                navController.navigate("profile")
                            }

                            else -> {
                                navController.navigate("notifications")
                            }
                        }
                    }
                }
            )
        }
    }
}


@Composable
fun HomeContent(
    navController: NavHostController,
    viewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearErrorMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(primaryLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            StatsSection(uiState = uiState)

            Spacer(modifier = Modifier.height(16.dp))

            QuickActionsSection(viewModel = viewModel)

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    ActivitiesSection(
                        activities = uiState.activities,
                        onActivityComplete = viewModel::completeActivity,
                        onActivityClick = { activityId ->
                            navController.navigate("activity_detail/$activityId")
                        },
                        onRefresh = viewModel::refreshData
                    )
                }
            }
        }
    }
}

@Composable
fun StatsSection(uiState: HomeUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatCard(
            title = "Completed",
            value = uiState.completedCount.toString(),
            color = onPrimaryLight
        )
        StatCard(
            title = "Remaining",
            value = (uiState.totalCount - uiState.completedCount).toString(),
            color = onPrimaryLight
        )
        StatCard(
            title = "Daily Progress",
            value = "${(uiState.todayProgress * 100).toInt()}%",
            color = onPrimaryLight
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
    LinearProgressIndicator(
        progress = { uiState.todayProgress },
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        color = Color.White,
        trackColor = Color.Gray.copy(alpha = 0.3f)
    )
}

@Composable
fun StatCard(title: String, value: String, color: Color) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(80.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = TextStyle(
                    fontFamily = displayFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = displayFontFamily,
                    fontSize = 11.sp,
                    color = onPrimaryLight
                )
            )
        }
    }
}

@Composable
fun QuickActionsSection(viewModel: HomeViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = { showAddDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryContainerLightMediumContrast,
                contentColor = onPrimaryLightMediumContrast
            ),
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Activity")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Activity")
        }

    }

    if (showAddDialog) {
        AddActivityDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, description, address ->
                viewModel.addActivity(title, description, address)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddActivityDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var useCurrentLocation by remember { mutableStateOf(false) }
    var isLoadingLocation by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val locationManager = remember { LocationManager(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted && useCurrentLocation) {
            isLoadingLocation = true
        }
    }

    LaunchedEffect(useCurrentLocation, isLoadingLocation) {
        if (useCurrentLocation && isLoadingLocation && locationManager.hasLocationPermission()) {
            val location = locationManager.getCurrentLocation()
            isLoadingLocation = false
            location?.address?.let { addr ->
                address = addr
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Activity") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    enabled = !isLoadingLocation
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = useCurrentLocation,
                        onCheckedChange = { checked ->
                            useCurrentLocation = checked
                            if (checked) {
                                if (locationManager.hasLocationPermission()) {
                                    isLoadingLocation = true
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            }
                        }
                    )
                    Text("Use current location")

                    if (isLoadingLocation) {
                        Spacer(modifier = Modifier.width(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(
                            title.trim(),
                            description.trim(),
                            if (address.isNotBlank()) address.trim() else null
                        )
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ActivitiesSection(
    activities: List<ActivityEntity>,
    onActivityComplete: (String) -> Unit,
    onActivityClick: (String) -> Unit,
    onRefresh: () -> Unit
) {
    if (activities.isEmpty()) {
        EmptyState(onRefresh = onRefresh)
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(activities) { activity ->
                ActivityCard(
                    activity = activity,
                    onComplete = { onActivityComplete(activity.id) },
                    onItemClick = onActivityClick
                )
            }
        }
    }
}

@Composable
fun ActivityCard(
    activity: ActivityEntity,
    onComplete: () -> Unit,
    onItemClick: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(activity.id) },
        colors = CardDefaults.cardColors(
            containerColor = if (activity.isCompleted)
                Color.White.copy(alpha = 0.7f)
            else
                Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = activity.title,
                    style = TextStyle(
                        fontFamily = displayFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (activity.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = activity.description,
                        style = TextStyle(
                            fontFamily = displayFontFamily,
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (activity.address != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Location",
                            modifier = Modifier.size(12.dp),
                            tint = primaryLight
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = activity.address,
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = primaryLight
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${activity.points} pts",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Medium
                        )
                    )

                }
            }

            if (activity.isCompleted) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(32.dp)
                )
            } else {
                IconButton(
                    onClick = onComplete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Mark Complete",
                        tint = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(onRefresh: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.List,
                contentDescription = "No activities",
                modifier = Modifier.size(64.dp),
                tint = onPrimaryLight.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No activities yet",
                style = TextStyle(
                    fontFamily = displayFontFamily,
                    fontSize = 16.sp,
                    color = onPrimaryLight.copy(alpha = 0.6f)
                )
            )

        }
    }
}