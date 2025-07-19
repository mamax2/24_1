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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.a24.data.ActivityEntity
import com.example.a24.data.AppDatabase
import com.example.a24.data.Repository
import com.example.a24.ui.composables.AppBar
import com.example.a24.ui.composables.SectionHeader
import com.example.a24.ui.theme.AppTheme
import com.example.a24.ui.theme.displayFontFamily
import com.example.a24.ui.theme.onPrimaryLight
import com.example.a24.ui.theme.onPrimaryLightMediumContrast
import com.example.a24.ui.theme.primaryContainerLightMediumContrast
import com.example.a24.ui.theme.primaryLight
import com.example.a24.ui.viewmodel.HomeViewModel
import com.example.a24.ui.viewmodel.HomeUiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(navController: NavHostController) {
    val context = LocalContext.current

    // Inizializza ViewModel con Repository
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

    AppTheme {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            AppBar(currentRoute = "home", navController = navController)
            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(text = "TODAY'S ACTIVITIES")

            HomeContent(navController = navController, viewModel = viewModel)
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

    // Gestione errori
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // Mostra errore in un toast
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
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            // Header con statistiche
            StatsSection(uiState = uiState)

            Spacer(modifier = Modifier.height(16.dp))

            // Sezione azioni rapide
            QuickActionsSection(viewModel = viewModel)

            Spacer(modifier = Modifier.height(16.dp))

            // Lista attività
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                ActivitiesSection(
                    activities = uiState.activities,
                    onActivityComplete = viewModel::completeActivity,
                    onRefresh = viewModel::refreshData
                )
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
            color = Color(0xFF4CAF50)
        )
        StatCard(
            title = "Remaining",
            value = (uiState.totalCount - uiState.completedCount).toString(),
            color = Color(0xFFFF9800)
        )
        StatCard(
            title = "Progress",
            value = "${(uiState.todayProgress * 100).toInt()}%",
            color = primaryContainerLightMediumContrast
        )
    }

    // Barra di progresso
    Spacer(modifier = Modifier.height(8.dp))
    LinearProgressIndicator(
        progress = { uiState.todayProgress },
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        color = Color(0xFF4CAF50),
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

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = { viewModel.addSampleActivities() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White
            ),
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Add Sample")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Sample")
        }
    }

    // Dialog per aggiungere attività
    if (showAddDialog) {
        AddActivityDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, description ->
                viewModel.addActivity(title, description)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ActivitiesSection(
    activities: List<ActivityEntity>,
    onActivityComplete: (String) -> Unit,
    onRefresh: () -> Unit
) {
    if (activities.isEmpty()) {
        EmptyState(onRefresh = onRefresh)
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxHeight()
        ) {
            items(activities) { activity ->
                ActivityCard(
                    activity = activity,
                    onComplete = { onActivityComplete(activity.id) }
                )
            }
        }
    }
}

@Composable
fun ActivityCard(
    activity: ActivityEntity,
    onComplete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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

                if (activity.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = activity.description,
                        style = TextStyle(
                            fontFamily = displayFontFamily,
                            fontSize = 14.sp,
                            color = onPrimaryLight.copy(alpha = 0.7f)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Priority: ${activity.priority}",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    )
                }
            }

            // Pulsante azione
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
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onRefresh,
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryContainerLightMediumContrast,
                    contentColor = onPrimaryLightMediumContrast
                )
            ) {
                Text("Add Sample Activities")
            }
        }
    }
}

@Composable
fun AddActivityDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
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
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title.trim(), description.trim())
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