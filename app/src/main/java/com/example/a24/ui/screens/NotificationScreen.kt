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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.a24.ui.composables.AppBar
import com.example.a24.ui.composables.SectionHeader
import com.example.a24.ui.theme.AppTheme
import com.example.a24.ui.theme.displayFontFamily
import com.example.a24.ui.theme.onPrimaryLight
import com.example.a24.ui.theme.onPrimaryLightMediumContrast
import com.example.a24.ui.theme.primaryContainerLightHighContrast
import com.example.a24.ui.theme.primaryContainerLightMediumContrast
import com.example.a24.ui.theme.primaryLight
import java.text.SimpleDateFormat
import java.util.*

// Enum per i tipi di notifica
enum class NotificationType {
    ACHIEVEMENT,    // Badge sbloccati
    SYSTEM,        // Aggiornamenti sistema
    SECURITY,      // Login, sicurezza
    REMINDER,      // Promemoria daily login
    SOCIAL,        // Interazioni social (futuro)
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
    AppTheme {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            AppBar(currentRoute = "notifications", navController = navController)
            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(text = "Notifications")

            NotificationsContent(navController)
        }
    }
}

@Composable
fun NotificationsContent(navController: NavHostController) {

    var notifications by remember { mutableStateOf(getSampleNotifications()) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf<NotificationType?>(null) }

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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            // Header con statistiche e azioni
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
                    // Pulsante Filtro
                    IconButton(
                        onClick = { showFilterDialog = true }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.List,
                            contentDescription = "Filter",
                            tint = onPrimaryLight
                        )
                    }

                    // Pulsante Mark All Read
                    if (unreadCount > 0) {
                        IconButton(
                            onClick = {
                                notifications = notifications.map { it.copy(isRead = true) }
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
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Done,
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
                                notifications = notifications.map {
                                    if (it.id == notificationId) it.copy(isRead = true) else it
                                }
                            },
                            onDelete = { notificationId ->
                                notifications = notifications.filter { it.id != notificationId }
                            },
                            onAction = { notificationId ->
                                // Gestisci azione specifica
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

    // Dialog filtro
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
            // Icona notifica
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getNotificationColor(notification.type).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
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
                    text = formatTimestamp(notification.timestamp),
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

// Helper functions
fun getNotificationColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.ACHIEVEMENT -> Color(0xFF4CAF50)
        NotificationType.SYSTEM -> Color(0xFF2196F3)
        NotificationType.SECURITY -> Color(0xFFFF5722)
        NotificationType.REMINDER -> Color(0xFFFF9800)
        NotificationType.SOCIAL -> Color(0xFF9C27B0)
        NotificationType.MARKETING -> Color(0xFF00BCD4)
    }
}

fun getTypeDisplayName(type: NotificationType): String {
    return when (type) {
        NotificationType.ACHIEVEMENT -> "Achievements"
        NotificationType.SYSTEM -> "System"
        NotificationType.SECURITY -> "Security"
        NotificationType.REMINDER -> "Reminders"
        NotificationType.SOCIAL -> "Social"
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

// Sample data
fun getSampleNotifications(): List<AppNotification> {
    val now = Date()
    return listOf(
        AppNotification(
            id = "1",
            type = NotificationType.ACHIEVEMENT,
            title = "🎉 New Badge Unlocked!",
            message = "Congratulations! You've earned the 'Week Streak' badge for 7 consecutive days of activity.",
            timestamp = Date(now.time - 300000), // 5 minutes ago
            isRead = false,
            actionText = "View Badge",
        ),
        AppNotification(
            id = "2",
            type = NotificationType.SECURITY,
            title = "New Login Detected",
            message = "We detected a login from a new device. If this wasn't you, please check your account security.",
            timestamp = Date(now.time - 3600000), // 1 hour ago
            isRead = false,
            actionText = "Review"
        ),
        AppNotification(
            id = "3",
            type = NotificationType.REMINDER,
            title = "Daily Check-in Reminder",
            message = "Don't forget your daily check-in to maintain your streak!",
            timestamp = Date(now.time - 7200000), // 2 hours ago
            isRead = true,
        ),
        AppNotification(
            id = "4",
            type = NotificationType.SYSTEM,
            title = "App Updated",
            message = "24+1 has been updated to version 2.1.0 with new features and improvements.",
            timestamp = Date(now.time - 86400000), // 1 day ago
            isRead = true,
        ),
        AppNotification(
            id = "5",
            type = NotificationType.ACHIEVEMENT,
            title = "🏆 Level Up!",
            message = "Amazing! You've reached level 5. Keep up the great work!",
            timestamp = Date(now.time - 172800000), // 2 days ago
            isRead = true,
        ),
        AppNotification(
            id = "6",
            type = NotificationType.MARKETING,
            title = "New Feature: Photo Upload",
            message = "You can now upload custom profile pictures! Check it out in your profile settings.",
            timestamp = Date(now.time - 259200000), // 3 days ago
            isRead = true,
            actionText = "Try Now"
        )
    )
}