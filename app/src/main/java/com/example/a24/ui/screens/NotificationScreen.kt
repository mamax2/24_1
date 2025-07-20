package com.example.a24.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.a24.data.AppDatabase
import com.example.a24.data.NotificationEntity
import com.example.a24.data.Repository
import com.example.a24.ui.composables.AppBar
import com.example.a24.ui.composables.SectionHeader
import com.example.a24.ui.theme.AppTheme
import com.example.a24.ui.theme.displayFontFamily
import com.example.a24.ui.theme.onPrimaryLight
import com.example.a24.ui.theme.onPrimaryLightMediumContrast
import com.example.a24.ui.theme.primaryContainerLightHighContrast
import com.example.a24.ui.theme.primaryContainerLightMediumContrast
import com.example.a24.ui.theme.primaryLight
import com.example.a24.ui.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.*


@Composable
fun NotificationScreen(navController: NavHostController) {
    val context = LocalContext.current

    // Inizializza ViewModel con Repository
    val database = AppDatabase.getDatabase(context)
    val repository = Repository(
        database.userDao(),
        database.activityDao(),
        database.notificationDao(),
        database.badgeDao()
    )

    val viewModel: NotificationViewModel = viewModel {
        NotificationViewModel(repository)
    }

    AppTheme {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            AppBar(currentRoute = "notifications", navController = navController)
            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(text = "Notifications")

            NotificationsContent(navController = navController, viewModel = viewModel)
        }
    }
}

@Composable
fun NotificationsContent(
    navController: NavHostController,
    viewModel: NotificationViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showFilterDialog by remember { mutableStateOf(false) }

    // Gestione errori
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearErrorMessage()
        }
    }

    // Filtra notifiche in base al filtro selezionato
    val filteredNotifications = viewModel.getFilteredNotifications()

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
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            // Header con statistiche e azioni
            NotificationHeader(
                totalCount = filteredNotifications.size,
                unreadCount = uiState.unreadCount,
                selectedFilter = uiState.selectedFilter,
                onFilterClick = { showFilterDialog = true },
                onMarkAllRead = viewModel::markAllAsRead,
                onCreateTest = viewModel::createTestNotification
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lista notifiche
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredNotifications.isEmpty()) {
                EmptyNotificationsState(
                    hasFilter = uiState.selectedFilter != null,
                    onCreateTest = viewModel::createTestNotification
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredNotifications) { notification ->
                        NotificationCard(
                            notification = notification,
                            onMarkAsRead = viewModel::markAsRead,
                            onDelete = viewModel::deleteNotification,
                            onAction = {
                                viewModel.handleNotificationAction(notification)
                                // Naviga in base al tipo di notifica
                                when (notification.type) {
                                    "ACHIEVEMENT" -> navController.navigate("profile")
                                    "SECURITY" -> navController.navigate("profile")
                                    "REMINDER" -> navController.navigate("home")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog filtro
    if (showFilterDialog) {
        FilterDialog(
            currentFilter = uiState.selectedFilter,
            onFilterSelected = { filter ->
                viewModel.applyFilter(filter)
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }
}

@Composable
fun NotificationHeader(
    totalCount: Int,
    unreadCount: Int,
    selectedFilter: String?,
    onFilterClick: () -> Unit,
    onMarkAllRead: () -> Unit,
    onCreateTest: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Total: $totalCount",
                style = TextStyle(
                    fontFamily = displayFontFamily,
                    fontSize = 16.sp,
                    color = onPrimaryLight
                )
            )
            if (unreadCount > 0) {
                Text(
                    text = "$unreadCount unread",
                    style = TextStyle(
                        fontFamily = displayFontFamily,
                        fontSize = 14.sp,
                        color = Color(0xFFFF5722),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            selectedFilter?.let {
                Text(
                    text = "Filter: ${getTypeDisplayName(it)}",
                    style = TextStyle(
                        fontFamily = displayFontFamily,
                        fontSize = 12.sp,
                        color = Color(0xFF2196F3)
                    )
                )
            }
        }

        Row {
            // Pulsante Test (per sviluppo)
            IconButton(onClick = onCreateTest) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Test Notification",
                    tint = onPrimaryLight
                )
            }

            // Pulsante Filtro
            IconButton(onClick = onFilterClick) {
                Icon(
                    Icons.AutoMirrored.Filled.List,
                    contentDescription = "Filter",
                    tint = onPrimaryLight
                )
            }

            // Pulsante Mark All Read
            if (unreadCount > 0) {
                IconButton(onClick = onMarkAllRead) {
                    Icon(
                        Icons.Default.Done,
                        contentDescription = "Mark All Read",
                        tint = onPrimaryLight
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: NotificationEntity,
    onMarkAsRead: (String) -> Unit,
    onDelete: (String) -> Unit,
    onAction: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead)
                Color.White.copy(alpha = 0.7f)
            else
                primaryContainerLightMediumContrast.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icona notifica
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getNotificationColor(notification.type).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    getNotificationIcon(notification.type),
                    contentDescription = notification.type,
                    tint = getNotificationColor(notification.type),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Contenuto notifica
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.title,
                    style = TextStyle(
                        fontFamily = displayFontFamily,
                        fontSize = 16.sp,
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                        color = onPrimaryLight
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    style = TextStyle(
                        fontFamily = displayFontFamily,
                        fontSize = 14.sp,
                        color = onPrimaryLight.copy(alpha = 0.8f)
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatTimestamp(Date(notification.timestamp)),
                    style = TextStyle(
                        fontFamily = displayFontFamily,
                        fontSize = 12.sp,
                        color = onPrimaryLight.copy(alpha = 0.6f)
                    )
                )

                // Action button se presente
                notification.actionText?.let { actionText ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onAction(notification.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryContainerLightMediumContrast,
                            contentColor = onPrimaryLightMediumContrast
                        ),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = actionText,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Menu azioni
            Column {
                if (!notification.isRead) {
                    IconButton(
                        onClick = { onMarkAsRead(notification.id) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Done,
                            contentDescription = "Mark as read",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF2196F3)
                        )
                    }
                }

                IconButton(
                    onClick = { onDelete(notification.id) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp),
                        tint = onPrimaryLight.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyNotificationsState(
    hasFilter: Boolean,
    onCreateTest: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = "No notifications",
                modifier = Modifier.size(64.dp),
                tint = onPrimaryLight.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (hasFilter) "No notifications for this filter" else "No notifications yet",
                style = TextStyle(
                    fontFamily = displayFontFamily,
                    fontSize = 16.sp,
                    color = onPrimaryLight.copy(alpha = 0.6f)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onCreateTest,
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryContainerLightMediumContrast,
                    contentColor = onPrimaryLightMediumContrast
                )
            ) {
                Text("Create Test Notification")
            }
        }
    }
}

@Composable
fun FilterDialog(
    currentFilter: String?,
    onFilterSelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Notifications") },
        text = {
            Column {
                FilterOption("All", currentFilter == null) {
                    onFilterSelected(null)
                }

                val notificationTypes = listOf(
                    "ACHIEVEMENT", "SYSTEM", "SECURITY", "REMINDER", "SOCIAL", "MARKETING"
                )

                notificationTypes.forEach { type ->
                    FilterOption(
                        getTypeDisplayName(type),
                        currentFilter == type
                    ) {
                        onFilterSelected(type)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun FilterOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

// Helper functions
fun getNotificationColor(type: String): Color {
    return when (type) {
        "ACHIEVEMENT" -> Color(0xFF4CAF50)
        "SYSTEM" -> Color(0xFF2196F3)
        "SECURITY" -> Color(0xFFFF5722)
        "REMINDER" -> Color(0xFFFF9800)
        "SOCIAL" -> Color(0xFF9C27B0)
        "MARKETING" -> Color(0xFF00BCD4)
        else -> Color(0xFF757575)
    }
}

fun getNotificationIcon(type: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        "ACHIEVEMENT" -> Icons.Default.Star
        "SYSTEM" -> Icons.Default.Info
        "SECURITY" -> Icons.Default.Lock
        "REMINDER" -> Icons.Default.ThumbUp
        "SOCIAL" -> Icons.Default.Person
        "MARKETING" -> Icons.Default.ShoppingCart
        else -> Icons.Default.Notifications
    }
}

fun getTypeDisplayName(type: String): String {
    return when (type) {
        "ACHIEVEMENT" -> "Achievements"
        "SYSTEM" -> "System"
        "SECURITY" -> "Security"
        "REMINDER" -> "Reminders"
        "SOCIAL" -> "Social"
        "MARKETING" -> "Updates"
        else -> type
    }
}

fun formatTimestamp(date: Date): String {
    val now = Date()
    val diff = now.time - date.time

    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
    }
}