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
import com.example.a24.ui.viewmodels.NotificationViewModel
import com.example.a24.ui.viewmodels.NotificationViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// Enum per i tipi di notifica
enum class NotificationType {
    ACHIEVEMENT,    // Badge sbloccati
    SYSTEM,        // Aggiornamenti sistema
    SECURITY,      // Login, sicurezza
    REMINDER,      // Promemoria daily login
    MARKETING      // Nuove funzionalità
}

// Data class per le notifiche
data class AppNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val timestamp: Date,
    val isRead: Boolean,
    val actionText: String? = null
)

@Composable
fun NotificationScreen(navController: NavHostController) {
    val context = LocalContext.current

    // Verifica se l'utente è loggato
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    if (currentUser == null) {
        AppTheme {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Please log in to view notifications")
                Button(onClick = { navController.navigate("login") }) {
                    Text("Go to Login")
                }
            }
        }
        return
    }

    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember {
        Repository(
            userDao = database.userDao(),
            activityDao = database.activityDao(),
            notificationDao = database.notificationDao(),
            badgeDao = database.badgeDao()
        )
    }

    val viewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModelFactory(repository)
    )



    AppTheme {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            AppBar(currentRoute = "notifications", navController = navController)
            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(text = "Notifications")

            NotificationsContent(navController, viewModel)
        }
    }
}

@Composable
fun NotificationsContent(
    navController: NavHostController,
    viewModel: NotificationViewModel
) {
    val notificationEntities by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf<NotificationType?>(null) }

    // Converti le entità in UI model
    val notifications = remember(notificationEntities) {
        notificationEntities.map { viewModel.mapToUINotification(it) }
    }

    // Filtra notifiche in base al filtro selezionato
    val filteredNotifications = if (selectedFilter != null) {
        notifications.filter { it.type == selectedFilter }
    } else {
        notifications
    }

    val unreadCount = notifications.count { !it.isRead }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(primaryLight)
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = onPrimaryLight
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading notifications...",
                            style = TextStyle(
                                fontFamily = displayFontFamily,
                                fontSize = 16.sp,
                                color = onPrimaryLight
                            )
                        )
                    }
                }
            }

            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Error",
                            modifier = Modifier.size(64.dp),
                            tint = Color.Red
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Error loading notifications",
                            style = TextStyle(
                                fontFamily = displayFontFamily,
                                fontSize = 16.sp,
                                color = onPrimaryLight
                            )
                        )
                        Text(
                            text = error!!,
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = onPrimaryLight.copy(alpha = 0.7f)
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.refreshNotifications() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryContainerLightMediumContrast,
                                contentColor = onPrimaryLightMediumContrast
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total: ${filteredNotifications.size}",
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
                        }

                        Row {
                            IconButton(
                                onClick = { showFilterDialog = true }
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.List,
                                    contentDescription = "Filter",
                                    tint = onPrimaryLight
                                )
                            }

                            if (unreadCount > 0) {
                                IconButton(
                                    onClick = {
                                        viewModel.markAllAsRead()
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Done,
                                        contentDescription = "Mark All Read",
                                        tint = onPrimaryLight
                                    )
                                }
                            }


                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Lista notifiche
                    if (filteredNotifications.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
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
                                    text = if (selectedFilter != null) "No notifications for this filter" else "No notifications yet",
                                    style = TextStyle(
                                        fontFamily = displayFontFamily,
                                        fontSize = 16.sp,
                                        color = onPrimaryLight.copy(alpha = 0.6f)
                                    )
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            items(filteredNotifications) { notification ->
                                NotificationCard(
                                    notification = notification,
                                    onMarkAsRead = { notificationId ->
                                        viewModel.markAsRead(notificationId)
                                    },
                                    onDelete = { notificationId ->
                                        viewModel.deleteNotification(notificationId)
                                    },
                                    onAction = { notificationId ->
                                        val notification = notifications.find { it.id == notificationId }
                                        when (notification?.type) {
                                            NotificationType.ACHIEVEMENT -> {
                                                navController.navigate("profile")
                                            }
                                            NotificationType.SECURITY -> {
                                                navController.navigate("profile")
                                            }
                                            else -> {
                                                // Default action
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterDialog) {
        FilterDialog(
            currentFilter = selectedFilter,
            onFilterSelected = { filter ->
                selectedFilter = filter
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }
}

@Composable
fun NotificationCard(
    notification: AppNotification,
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getNotificationColor(notification.type).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getNotificationIcon(notification.type),
                    contentDescription = notification.type.name,
                    tint = getNotificationColor(notification.type),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

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
                    text = formatTimestamp(notification.timestamp),
                    style = TextStyle(
                        fontFamily = displayFontFamily,
                        fontSize = 12.sp,
                        color = onPrimaryLight.copy(alpha = 0.6f)
                    )
                )

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

            Column {
                if (!notification.isRead) {
                    IconButton(
                        onClick = { onMarkAsRead(notification.id) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Done,
                            contentDescription = "Mark as read",
                            modifier = Modifier.size(12.dp),
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
fun FilterDialog(
    currentFilter: NotificationType?,
    onFilterSelected: (NotificationType?) -> Unit,
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
                NotificationType.values().forEach { type ->
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

fun getNotificationColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.ACHIEVEMENT -> Color(0xFF4CAF50)
        NotificationType.SYSTEM -> Color(0xFF2196F3)
        NotificationType.SECURITY -> Color(0xFFFF5722)
        NotificationType.REMINDER -> Color(0xFFFF9800)
        NotificationType.MARKETING -> Color(0xFF00BCD4)
    }
}

fun getNotificationIcon(type: NotificationType): ImageVector {
    return when (type) {
        NotificationType.ACHIEVEMENT -> Icons.Default.Star
        NotificationType.SYSTEM -> Icons.Default.Info
        NotificationType.SECURITY -> Icons.Default.Warning
        NotificationType.REMINDER -> Icons.Default.Notifications
        NotificationType.MARKETING -> Icons.Default.ShoppingCart
    }
}

fun getTypeDisplayName(type: NotificationType): String {
    return when (type) {
        NotificationType.ACHIEVEMENT -> "Achievements"
        NotificationType.SYSTEM -> "System"
        NotificationType.SECURITY -> "Security"
        NotificationType.REMINDER -> "Reminders"
        NotificationType.MARKETING -> "Updates"
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