package com.example.a24

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.a24.data.AppDatabase
import com.example.a24.data.Repository
import com.example.a24.ui.theme.AppTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var repository: Repository

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inizializza il database
        initializeDatabase()

        enableEdgeToEdge()
        setContent {
            AppTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Inizializza utente se è loggato
                    LaunchedEffect(Unit) {
                        FirebaseAuth.getInstance().currentUser?.let { user ->
                            initializeUserIfNeeded(user.uid, user.displayName ?: "User", user.email ?: "")
                        }
                    }

                    AppNavigation()
                }
            }
        }
    }

    private fun initializeDatabase() {
        val database = AppDatabase.getDatabase(this)
        repository = Repository(
            database.userDao(),
            database.activityDao(),
            database.notificationDao(),
            database.badgeDao()
        )
    }

    private fun initializeUserIfNeeded(userId: String, name: String, email: String) {
        lifecycleScope.launch {
            try {
                repository.initializeUser(userId, name, email)
                repository.createInitialNotifications(userId)

                // Crea alcune notifiche di benvenuto se è il primo accesso
                createWelcomeNotifications(userId)

            } catch (e: Exception) {
                // Log error but don't crash
                e.printStackTrace()
            }
        }
    }

    private suspend fun createWelcomeNotifications(userId: String) {
        try {
            // Controlla se esistono già notifiche per evitare duplicati
            val existingUser = repository.getUser(userId)
            existingUser?.let { user ->
                if (user.createdAt == user.lastActive) { // Primo accesso
                    repository.createNotification(
                        userId = userId,
                        type = "ACHIEVEMENT",
                        title = "🎉 Welcome to 24+1!",
                        message = "You've successfully created your account. Start your productivity journey!",
                        actionText = "Get Started"
                    )

                    repository.createNotification(
                        userId = userId,
                        type = "SYSTEM",
                        title = "📱 Setup Complete",
                        message = "Your account has been configured. You can now track your daily activities!",
                        actionText = "View Profile"
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}