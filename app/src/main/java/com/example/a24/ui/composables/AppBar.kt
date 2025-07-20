package com.example.a24.ui.composables

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.navigation.NavHostController
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.a24.data.AppDatabase
import com.example.a24.data.Repository
import com.example.a24.ui.theme.ColorFamily
import com.example.a24.ui.theme.displayFontFamily
import com.example.a24.ui.theme.onPrimaryLight
import com.example.a24.ui.theme.primaryContainerLight
import com.example.a24.ui.theme.primaryLight
import com.example.a24.ui.theme.secondaryContainerLight
import com.example.a24.ui.theme.secondaryLight
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.catch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    currentRoute: String? = null,
    navController: NavHostController? = null
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid

    // Inizializza repository per ottenere il conteggio delle notifiche non lette
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember {
        Repository(
            userDao = database.userDao(),
            activityDao = database.activityDao(),
            notificationDao = database.notificationDao(),
            badgeDao = database.badgeDao()
        )
    }

    // Osserva il conteggio delle notifiche non lette solo se l'utente è loggato
    val unreadCount by if (currentUserId != null) {
        repository.getUnreadCount(currentUserId)
            .catch { emit(0) } // In caso di errore, mostra 0
            .collectAsState(initial = 0)
    } else {
        remember { mutableStateOf(0) }
    }

    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = primaryLight,
            titleContentColor = onPrimaryLight,
            navigationIconContentColor = onPrimaryLight,
            actionIconContentColor = onPrimaryLight
        ),
        title = {
            Text(
                "24+1",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(fontFamily = displayFontFamily, fontSize = 42.sp)
            )
        },
        navigationIcon = {
            if (currentRoute != null && currentRoute !in listOf("login", "signup", "home")) {
                IconButton(onClick = {
                    navController?.popBackStack()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            // Mostra actions SOLO se NON sei in login o signup
            if (currentRoute != null && currentRoute !in listOf("login", "signup")) {
                // Notification button con badge
                Box {
                    IconButton(onClick = {
                        navController?.navigate("notifications")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications"
                        )
                    }

                    // Badge per notifiche non lette
                    if (unreadCount > 0) {
                        NotificationBadge(
                            count = unreadCount,
                            modifier = Modifier.align(Alignment.TopEnd)
                        )
                    }
                }

                // Profile button
                IconButton(onClick = {
                    navController?.navigate("profile")
                }) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile"
                    )
                }
            }
        }
    )
}

@Composable
fun NotificationBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    if (count > 0) {
        Box(
            modifier = modifier
                .size(18.dp)
                .offset(x = (-6).dp, y = 6.dp)
                .clip(CircleShape)
                .background(Color.Red),
            contentAlignment = Alignment.Center
        ){}
    }
}