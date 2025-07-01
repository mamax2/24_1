package com.example.a24.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.a24.ui.theme.primaryContainerLightMediumContrast
import com.example.a24.ui.theme.primaryLight
import kotlin.math.*

// Data class per le attività
data class Activity(
    val id: String,
    val title: String,
    val description: String,
    val category: ActivityCategory,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val duration: String,
    val difficulty: ActivityDifficulty,
    val isCompleted: Boolean = false,
    val completedDate: String? = null
)

enum class ActivityCategory(val displayName: String,  val color: Color) {
    SPORT("Sport",  Color(0xFF4CAF50)),
    CULTURE("Culture",  Color(0xFF9C27B0)),
    FOOD("Food & Drink",  Color(0xFFFF5722)),
    NATURE("Nature",  Color(0xFF2E7D32)),
    SHOPPING("Shopping",  Color(0xFFE91E63)),
    ENTERTAINMENT("Entertainment",  Color(0xFF3F51B5))
}

enum class ActivityDifficulty(val displayName: String, val color: Color) {
    EASY("Easy", Color(0xFF4CAF50)),
    MEDIUM("Medium", Color(0xFFFF9800)),
    HARD("Hard", Color(0xFFFF5722))
}

@Composable
fun HomeScreen(navController: NavHostController) {
    AppTheme {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            AppBar(currentRoute = "home", navController = navController)
            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(text = "TODAY'S ACTIVITIES")

            HomeContent(navController)
        }
    }
}

@Composable
fun HomeContent(navController: NavHostController) {
    // Posizione utente fittizia (Milano centro)
    val userLatitude = 45.4642
    val userLongitude = 9.1900

    // Attività di esempio
    val activities = remember {
        listOf(
            Activity(
                id = "1",
                title = "Morning Jog in Parco Sempione",
                description = "Start your day with a refreshing run through Milan's beautiful park",
                category = ActivityCategory.SPORT,
                location = "Parco Sempione, Milano",
                latitude = 45.4727,
                longitude = 9.1889,
                duration = "45 min",
                difficulty = ActivityDifficulty.MEDIUM
            ),
            Activity(
                id = "2",
                title = "Visit Duomo Cathedral",
                description = "Explore the magnificent Gothic architecture of Milan's main cathedral",
                category = ActivityCategory.CULTURE,
                location = "Piazza del Duomo, Milano",
                latitude = 45.4641,
                longitude = 9.1919,
                duration = "2 hours",
                difficulty = ActivityDifficulty.EASY
            ),
            Activity(
                id = "3",
                title = "Aperitivo at Navigli",
                description = "Enjoy traditional Italian aperitivo along the historic canals",
                category = ActivityCategory.FOOD,
                location = "Navigli District, Milano",
                latitude = 45.4485,
                longitude = 9.1815,
                duration = "1.5 hours",
                difficulty = ActivityDifficulty.EASY,
                isCompleted = true,
                completedDate = "Yesterday"
            ),
            Activity(
                id = "4",
                title = "Climb at Rock Gym",
                description = "Challenge yourself with indoor rock climbing session",
                category = ActivityCategory.SPORT,
                location = "Urban Wall, Milano",
                latitude = 45.4408,
                longitude = 9.2123,
                duration = "2 hours",
                difficulty = ActivityDifficulty.HARD
            ),
            Activity(
                id = "5",
                title = "Shopping at Quadrilatero",
                description = "Browse luxury boutiques in Milan's fashion district",
                category = ActivityCategory.SHOPPING,
                location = "Quadrilatero della Moda, Milano",
                latitude = 45.4698,
                longitude = 9.1951,
                duration = "3 hours",
                difficulty = ActivityDifficulty.EASY
            )
        )
    }

    var selectedActivity by remember { mutableStateOf<Activity?>(null) }

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
                    value = activities.count { it.isCompleted }.toString(),
                    color = Color(0xFF4CAF50)
                )
                StatCard(
                    title = "Remaining",
                    value = activities.count { !it.isCompleted }.toString(),
                    color = Color(0xFFFF9800)
                )
                StatCard(
                    title = "Total",
                    value = activities.size.toString(),
                    color = primaryContainerLightMediumContrast
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista attività
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                items(activities) { activity ->
                    ActivityCard(
                        activity = activity,
                        userLatitude = userLatitude,
                        userLongitude = userLongitude,
                        onClick = { selectedActivity = activity }
                    )
                }
            }
        }
    }

    // Dialog dettaglio attività
    selectedActivity?.let { activity ->
        ActivityDetailDialog(
            activity = activity,
            userLatitude = userLatitude,
            userLongitude = userLongitude,
            onDismiss = { selectedActivity = null },
            onMarkCompleted = {
                // Qui implementeresti la logica per segnare come completata
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
    activity: Activity,
    userLatitude: Double,
    userLongitude: Double,
    onClick: () -> Unit
) {
    val distance = calculateDistance(userLatitude, userLongitude, activity.latitude, activity.longitude)

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


            Spacer(modifier = Modifier.width(16.dp))

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
                        color = onPrimaryLight
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = activity.location,
                    style = TextStyle(
                        fontFamily = displayFontFamily,
                        fontSize = 14.sp,
                        color = onPrimaryLight.copy(alpha = 0.7f)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Distanza
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Distance",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF2196F3)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${String.format("%.1f", distance)} km",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = Color(0xFF2196F3),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Durata
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = activity.duration,
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        )
                    }
                }
            }

            // Status e difficoltà
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
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(activity.difficulty.color.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = activity.difficulty.displayName,
                            style = TextStyle(
                                fontSize = 10.sp,
                                color = activity.difficulty.color,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityDetailDialog(
    activity: Activity,
    userLatitude: Double,
    userLongitude: Double,
    onDismiss: () -> Unit,
    onMarkCompleted: () -> Unit
) {
    val distance = calculateDistance(userLatitude, userLongitude, activity.latitude, activity.longitude)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = activity.title,
                    style = TextStyle(
                        fontFamily = displayFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        text = {
            Column {
                Text(
                    text = activity.description,
                    style = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Dettagli
                DetailRow(

                    label = "Location",
                    value = activity.location

                )

                DetailRow(

                    label = "Distance",
                    value = "${String.format("%.1f", distance)} km from you",

                )

                DetailRow(
                    label = "Duration",
                    value = activity.duration
                )

                DetailRow(
                    label = "Difficulty",
                    value = activity.difficulty.displayName,
                )

                DetailRow(
                    label = "Category",
                    value = activity.category.displayName,
                )

                if (activity.isCompleted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Completed ${activity.completedDate}",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!activity.isCompleted) {
                Button(
                    onClick = onMarkCompleted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryContainerLightMediumContrast,
                        contentColor = onPrimaryLightMediumContrast
                    )
                ) {
                    Text("Mark as Completed")
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

@Composable
fun DetailRow(

    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label:",
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = TextStyle(fontSize = 12.sp),
            modifier = Modifier.weight(1f)
        )
    }
}

// Funzione per calcolare la distanza tra due coordinate geografiche
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371.0 // Raggio della Terra in km

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadius * c
}