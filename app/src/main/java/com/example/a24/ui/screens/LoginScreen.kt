package com.example.a24.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.a24.data.AppDatabase
import com.example.a24.data.Repository
import com.example.a24.managers.NotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavHostController){
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            AppBar(currentRoute = "login", navController = navController)
            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(text = "Login")

            Log(navController)
        }
    }
}

@Composable
fun Log(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    // Inizializza repository e notification manager
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember {
        Repository(
            userDao = database.userDao(),
            activityDao = database.activityDao(),
            notificationDao = database.notificationDao(),
            badgeDao = database.badgeDao()
        )
    }
    val notificationManager = remember { NotificationManager(repository) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(450.dp)
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(primaryLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Titolo "Email"
            Text(
                text = "Email",
                style = TextStyle(fontFamily = displayFontFamily, fontSize = 18.sp, color = onPrimaryLight),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Campo per l'email
            TextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Enter your email") },
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Titolo "Password"
            Text(
                text = "Password",
                style = TextStyle(fontFamily = displayFontFamily, fontSize = 18.sp, color = onPrimaryLight),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Campo per la password
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Enter your password") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Pulsante "Login"
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        isLoading = true

                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading = false

                                if (task.isSuccessful) {
                                    val user = auth.currentUser
                                    user?.let { firebaseUser ->
                                        // Inizializza l'utente nel database locale
                                        CoroutineScope(Dispatchers.IO).launch {
                                            try {
                                                repository.initializeUser(
                                                    userId = firebaseUser.uid,
                                                    name = firebaseUser.displayName ?: "User",
                                                    email = firebaseUser.email ?: email
                                                )

                                                // Crea notifiche iniziali se è la prima volta
                                                repository.createInitialNotifications(firebaseUser.uid)

                                                // Invia notifica di sicurezza per nuovo login
                                                notificationManager.sendSecurityNotification(
                                                    userId = firebaseUser.uid,
                                                    deviceInfo = "Android Device"
                                                )

                                                // Aggiorna last active
                                                repository.updateUserLastActive(firebaseUser.uid)
                                            } catch (e: Exception) {
                                                // Log error
                                            }
                                        }
                                    }

                                    Toast.makeText(context, "Login effettuato con successo!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("home")
                                } else {
                                    val errorMessage = task.exception?.message ?: "Errore durante il login"
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Inserisci email e password", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
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
                    Text(text = "Login")
                }
            }

            // Pulsante "Sign up"
            Button(
                onClick = {
                    navController.navigate("signup")
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryContainerLightHighContrast,
                    contentColor = onPrimaryLightMediumContrast
                )
            ) {
                Text(text = "Sign up")
            }
        }
    }
}
