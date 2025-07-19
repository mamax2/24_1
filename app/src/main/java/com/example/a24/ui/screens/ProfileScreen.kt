package com.example.a24.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.a24.ui.composables.AppBar
import com.example.a24.ui.composables.SectionHeader
import com.example.a24.ui.theme.AppTheme
import com.example.a24.ui.theme.displayFontFamily
import com.example.a24.ui.theme.onPrimaryLight
import com.example.a24.ui.theme.onPrimaryLightMediumContrast
import com.example.a24.ui.theme.primaryContainerLightHighContrast
import com.example.a24.ui.theme.primaryContainerLightMediumContrast
import com.example.a24.ui.theme.primaryLight
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.google.firebase.auth.userProfileChangeRequest
import com.example.a24.data.AppDatabase
import com.example.a24.data.Repository
import com.example.a24.data.UserEntity
import com.example.a24.data.UserBadgeEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Data class per i badge con i dati reali dal database
data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val isUnlocked: Boolean,
    val color: Color
)

@Composable
fun ProfileScreen(navController: NavHostController){
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            AppBar(currentRoute = "profile", navController = navController)

            SectionHeader(text = "Profile")

            ProfileContent(navController)
        }
    }
}

@Composable
fun ProfileContent(navController: NavHostController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val scrollState = rememberScrollState()

    // Stati per UI
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }

    // Stati per dati dal database
    var userEntity by remember { mutableStateOf<UserEntity?>(null) }
    var userBadges by remember { mutableStateOf<List<UserBadgeEntity>>(emptyList()) }
    var isLoadingData by remember { mutableStateOf(true) }

    // Inizializza repository
    val repository = remember {
        val database = AppDatabase.getDatabase(context)
        Repository(
            database.userDao(),
            database.activityDao(),
            database.notificationDao(),
            database.badgeDao()
        )
    }

    // Carica i dati dell'utente all'avvio
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            // Dati Firebase
            email = user.email ?: ""
            name = user.displayName ?: ""
            profileImageUrl = user.photoUrl?.toString()

            // Dati dal database
            try {
                val userId = user.uid
                userEntity = repository.getUser(userId)
                userBadges = repository.getUserBadges(userId)

                // Se l'utente ha un nome nel database, usa quello
                userEntity?.let { dbUser ->
                    if (dbUser.name.isNotEmpty()) {
                        name = dbUser.name
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingData = false
            }
        } ?: run {
            isLoadingData = false
        }
    }

    // Converti badge dal database in formato UI
    val badges = remember(userBadges) {
        val allBadges = listOf(
            Badge("first_login", "First Login", "Primo accesso all'app", "🎉", false, Color(0xFF4CAF50)),
            Badge("week_streak", "Week Streak", "7 giorni consecutivi", "🔥", false, Color(0xFFFF5722)),
            Badge("month_active", "Monthly Active", "30 giorni di attività", "⭐", false, Color(0xFF9E9E9E)),
            Badge("social_share", "Social Share", "Condiviso sui social", "📱", false, Color(0xFF2196F3)),
            Badge("productive", "Productive", "Completate 5+ attività", "💪", false, Color(0xFFFF9800)),
            Badge("perfect_day", "Perfect Day", "Giornata perfetta", "⭐", false, Color(0xFFFFD700))
        )

        // Marca come sbloccati i badge che l'utente ha
        val unlockedBadgeIds = userBadges.map { it.badgeId }.toSet()
        allBadges.map { badge ->
            badge.copy(isUnlocked = unlockedBadgeIds.contains(badge.id))
        }
    }

    if (isLoadingData) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // UI principale
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(primaryLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            // SEZIONE FOTO PROFILO
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(3.dp, onPrimaryLight, CircleShape)
                    .clickable { showImagePicker = true },
                contentAlignment = Alignment.Center
            ) {
                if (profileImageUrl != null) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(94.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Default Profile",
                        modifier = Modifier.size(50.dp),
                        tint = onPrimaryLight
                    )
                }

                // Icona camera sovrapposta
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(primaryContainerLightMediumContrast)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Change Photo",
                        modifier = Modifier.size(16.dp),
                        tint = onPrimaryLightMediumContrast
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SEZIONE STATISTICHE UTENTE
            userEntity?.let { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Level ${user.level}",
                            style = TextStyle(
                                fontFamily = displayFontFamily,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        )
                        Text(
                            text = "${user.totalPoints} Points",
                            style = TextStyle(
                                fontFamily = displayFontFamily,
                                fontSize = 16.sp,
                                color = onPrimaryLight
                            )
                        )
                        Text(
                            text = "${user.currentStreak} Day Streak",
                            style = TextStyle(
                                fontFamily = displayFontFamily,
                                fontSize = 14.sp,
                                color = onPrimaryLight.copy(alpha = 0.7f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // SEZIONE INFORMAZIONI PERSONALI
            Text(
                text = "Name",
                style = TextStyle(fontFamily = displayFontFamily, fontSize = 18.sp, color = onPrimaryLight),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Your name") },
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Text(
                text = "Email",
                style = TextStyle(fontFamily = displayFontFamily, fontSize = 18.sp, color = onPrimaryLight),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TextField(
                value = email,
                onValueChange = { },
                placeholder = { Text("Your email") },
                singleLine = true,
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // SEZIONE BADGE
            Text(
                text = "Achievements",
                style = TextStyle(
                    fontFamily = displayFontFamily,
                    fontSize = 20.sp,
                    color = onPrimaryLight,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(badges) { badge ->
                    BadgeCard(badge = badge)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PULSANTE SAVE PROFILE
            Button(
                onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "Il nome non può essere vuoto", Toast.LENGTH_SHORT).show()
                    } else {
                        isLoading = true

                        // Aggiorna Firebase
                        val profileUpdates = userProfileChangeRequest {
                            displayName = name
                        }

                        currentUser?.updateProfile(profileUpdates)
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Aggiorna anche il database locale
                                    currentUser.uid.let { userId ->
                                        CoroutineScope(Dispatchers.IO).launch {
                                            try {
                                                repository.updateUserProfile(userId, name, profileImageUrl)
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    isLoading = false
                                                    Toast.makeText(context, "Profilo aggiornato!", Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    isLoading = false
                                                    Toast.makeText(context, "Errore: ${e.message}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    isLoading = false
                                    Toast.makeText(context, "Errore: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryContainerLightMediumContrast,
                    contentColor = onPrimaryLightMediumContrast
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        color = onPrimaryLightMediumContrast
                    )
                } else {
                    Text(text = "Save Profile")
                }
            }

            // PULSANTE LOGOUT
            Button(
                onClick = {
                    auth.signOut()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryContainerLightHighContrast,
                    contentColor = onPrimaryLightMediumContrast
                )
            ) {
                Text(text = "Logout")
            }
        }
    }

    // Dialog per scelta foto
    if (showImagePicker) {
        AlertDialog(
            onDismissRequest = { showImagePicker = false },
            title = { Text("Choose Photo") },
            text = { Text("Come vuoi aggiungere la tua foto profilo?") },
            confirmButton = {
                Row {
                    TextButton(
                        onClick = {
                            showImagePicker = false
                            Toast.makeText(context, "Camera feature coming soon!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Camera")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showImagePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BadgeCard(badge: Badge) {
    Card(
        modifier = Modifier
            .width(80.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked)
                badge.color.copy(alpha = 0.1f)
            else
                Color.Gray.copy(alpha = 0.1f)
        ),
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
                text = badge.icon,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = badge.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (badge.isUnlocked) badge.color else Color.Gray,
                style = TextStyle(textAlign = TextAlign.Center)
            )
        }
    }
}