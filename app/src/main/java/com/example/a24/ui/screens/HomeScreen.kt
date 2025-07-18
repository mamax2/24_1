package com.example.a24.ui.screens

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.a24.App
import com.example.a24.data.ActivityEntity
import com.example.a24.ui.composables.AppBar
import com.example.a24.ui.composables.SectionHeader
import com.example.a24.ui.theme.AppTheme
import com.example.a24.ui.theme.displayFontFamily
import com.example.a24.ui.theme.onPrimaryLight
import com.example.a24.ui.theme.onPrimaryLightMediumContrast
import com.example.a24.ui.theme.primaryContainerLightMediumContrast
import com.example.a24.ui.theme.primaryLight
import com.example.a24.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(navController: NavHostController) {
    val context = LocalContext.current
    val app = context.applicationContext

    val viewModel: HomeViewModel? = if (app is App) {
        viewModel { HomeViewModel(app.repository) }
    } else {
        null
    }

    AppTheme {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            AppBar(currentRoute = "home", navController = navController)
            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(text = "TODAY'S ACTIVITIES")

            if (viewModel != null) {
                HomeContent(navController, viewModel)
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Errore interno: App non inizializzata",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}


@Composable
fun HomeContent(navController: NavHostController, viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedActivity by remember { mutableStateOf<ActivityEntity?>(null) }

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
            // Statistiche rapide
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCard(
                    title = "Completed",
                    value = uiState.completedToday.toString(),
                    color = Color(0xFF4CAF50)
                )
                StatCard(
                    title = "Remaining",
                    value = (uiState.totalToday - uiState.completedToday).toString(),
                    color = Color(0xFFFF9800)
                )
                StatCard(
                    title = "Total",
                    value = uiState.totalToday.toString(),
                    color = primaryContainerLightMediumContrast
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar
            if (uiState.totalToday > 0) {
                Text(
                    text = "Today's Progress: ${(uiState.todayProgress * 100).toInt()}%",
                    style = TextStyle(
                        fontFamily = displayFontFamily,
                        fontSize = 16.sp,
                        color = onPrimaryLight
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { uiState.todayProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFF4CAF50),
                    trackColor = Color.Gray.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Loading state
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            // Empty state
            else if (uiState.activities.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Add,
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
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryContainerLightMediumContrast
                            )
                        ) {
                            Text("Add First Activity")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Pulsante per test semplice
                        Button(
                            onClick = {
                                viewModel.addActivity("Test Activity", "This is a test")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF9C27B0)
                            )
                        ) {
                            Text("Add Test Activity")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                viewModel.addActivity("Morning Coffee", "Start the day right")
                                viewModel.addActivity("Check emails", "Review important messages")
                                viewModel.addActivity("Plan the day", "Set priorities and goals")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            )
                        ) {
                            Text("Add Sample Data")
                        }
                    }
                }
            }
            // Activities list
            else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.activities) { activity ->
                        ActivityCard(
                            activity = activity,
                            onMarkCompleted = { viewModel.completeActivity(it.id) },
                            onDelete = { viewModel.deleteActivity(it.id) },
                            onClick = { selectedActivity = activity }
                        )
                    }
                }
            }

            // Error state
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Red.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = "Error: $error",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Red
                    )
                }
            }
        }

        // FAB per aggiungere attività
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = primaryContainerLightMediumContrast
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Activity",
                tint = onPrimaryLightMediumContrast
            )
        }
    }

    // Dialog per aggiungere attività
    if (showAddDialog) {
        AddActivityDialog(
            onDismiss = { showAddDialog = false },
            onAddActivity = { title, description ->
                viewModel.addActivity(title, description)
                showAddDialog = false
            }
        )
    }

    // Dialog dettaglio attività
    selectedActivity?.let { activity ->
        ActivityDetailDialog(
            activity = activity,
            onDismiss = { selectedActivity = null },
            onMarkCompleted = {
                viewModel.completeActivity(activity.id)
                selectedActivity = null
            },
            onDelete = {
                viewModel.deleteActivity(activity.id)
                selectedActivity = null
            }
        )
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(80.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = TextStyle(
                    fontFamily = displayFontFamily,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = displayFontFamily,
                    fontSize = 12.sp,
                    color = onPrimaryLight
                )
            )
        }
    }
}

@Composable
fun ActivityCard(
    activity: ActivityEntity,
    onMarkCompleted: (ActivityEntity) -> Unit,
    onDelete: (ActivityEntity) -> Unit,
    onClick: () -> Unit
) {
    val createdDate = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(activity.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
            // Contenuto attività
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = activity.title,
                    style = TextStyle(

                        fontFamily = displayFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (activity.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = activity.description,
                        style = TextStyle(
                            fontFamily = displayFontFamily,
                            fontSize = 14.sp,
                            color = onPrimaryLight.copy(alpha = 0.7f)
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Created: $createdDate",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    )

                    Spacer(modifier = Modifier.width(16.dp))


                }
            }

            // Azioni
            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (activity.isCompleted) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    IconButton(
                        onClick = { onMarkCompleted(activity) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Mark as completed",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { onDelete(activity) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddActivityDialog(
    onDismiss: () -> Unit,
    onAddActivity: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Activity") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddActivity(title, description)
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
fun ActivityDetailDialog(
    activity: ActivityEntity,
    onDismiss: () -> Unit,
    onMarkCompleted: () -> Unit,
    onDelete: () -> Unit
) {
    val createdDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(activity.createdAt))
    val completedDate = activity.completedAt?.let {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(it))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(activity.title) },
        text = {
            Column {
                if (activity.description.isNotBlank()) {
                    Text(
                        text = activity.description,
                        style = TextStyle(fontSize = 14.sp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Text("Created: $createdDate")
                Text("Category: ${activity.category}")
                Text("Priority: ${when(activity.priority) {
                    2 -> "High"
                    1 -> "Medium"
                    else -> "Low"
                }}")

                if (activity.isCompleted && completedDate != null) {
                    Text("Completed: $completedDate", color = Color(0xFF4CAF50))
                }
            }
        },
        confirmButton = {
            Row {
                if (!activity.isCompleted) {
                    Button(
                        onClick = onMarkCompleted,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("Complete")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Delete")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}